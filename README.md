# 포인트 관리 시스템 API

## 프로젝트 개요

**프로젝트명**: 포인트 관리 시스템 API (with TDD)

**기술 스택**:
- Java 17
- Spring Boot 3.2.0
- Gradle (Kotlin DSL)
- JUnit 5, AssertJ
- Lombok

---

## 시스템 아키텍처

### 패키지 구조
```
io.hhplus.tdd/
├── point/                          # 포인트 도메인
│   ├── PointController.java       # REST API 컨트롤러
│   ├── PointService.java          # 비즈니스 로직 서비스
│   ├── UserPoint.java             # 사용자 포인트 도메인 모델
│   ├── PointHistory.java          # 포인트 이력 도메인 모델
│   └── TransactionType.java       # 거래 타입 (CHARGE/USE)
│
├── exception/                      # 예외 처리
│   ├── HanghaeException.java      # 커스텀 예외 클래스
│   └── ErrorCode.java             # 에러 코드 enum
│
├── database/                       # 데이터 저장소 (수정 불가)
│   ├── UserPointTable.java        # 사용자 포인트 저장소
│   └── PointHistoryTable.java     # 포인트 이력 저장소
│
├── ApiControllerAdvice.java       # 전역 예외 핸들러
└── ErrorResponse.java             # 에러 응답 모델
```

---

## API 명세

### Base URL
```
http://localhost:8080
```

### API 엔드포인트 목록

| Method | URI | 설명 | 비즈니스 규칙 |
|--------|-----|------|---------------|
| GET | `/point/{id}` | 특정 사용자의 현재 포인트 조회 | - |
| GET | `/point/{id}/histories` | 특정 사용자의 포인트 충전/사용 이력 조회 | - |
| PATCH | `/point/{id}/charge` | 특정 사용자의 포인트 충전 | • 충전 금액은 100 이상<br>• 충전 후 포인트는 음수 불가 |
| PATCH | `/point/{id}/use` | 특정 사용자의 포인트 사용 | • 사용 금액은 100 이상<br>• 보유 포인트보다 많이 사용 불가<br>• 사용 후 포인트는 음수 불가 |

---

## 에러 코드 명세

### 포인트 관련 에러 (H00xx)

| 코드 | 메시지 | 설명 |
|------|--------|------|
| H0001 | 충전 금액은 100 이상이어야 합니다. | 충전 금액이 100 미만인 경우 |
| H0002 | 사용 금액은 100 이상이어야 합니다. | 사용 금액이 100 미만인 경우 |
| H0003 | 포인트 잔액이 부족합니다. | 보유 포인트보다 많이 사용하려는 경우 |

### 도메인 검증 에러 (H10xx)

| 코드 | 메시지 | 설명 |
|------|--------|------|
| H1001 | id는 음수일 수 없습니다. | ID가 음수인 경우 |
| H1002 | point는 음수일 수 없습니다. | 포인트가 음수인 경우 |
| H1003 | userId는 음수일 수 없습니다. | 사용자 ID가 음수인 경우 |
| H1004 | amount는 음수일 수 없습니다. | 거래 금액이 음수인 경우 |

### 시스템 에러 (H99xx)

| 코드 | 메시지 | 설명 |
|------|--------|------|
| H9999 | 서버 에러가 발생했습니다. | 예상치 못한 서버 에러 |

---

# 동시성 제어 분석 보고서

## 1. 목적 
- 동일 사용자의 포인트 충전(chargePoint) 및 사용(usePoint) 시 Lost Update, 잔액 꼬임 문제 방지
- 다수 사용자가 동시에 접근해도 각 사용자 포인트가 정확히 반영되도록 보장
## 2. 문제점
- 기존 로직
```
// 포인트 충전
UserPoint currentPoint = userPointTable.selectById(userId);
long newAmount = currentPoint.point() + amount;

UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);
pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updatedPoint.updateMillis());

// 포인트 사용
UserPoint currentPoint = userPointTable.selectById(userId);
long newAmount = currentPoint.point() - amount;

UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);
pointHistoryTable.insert(userId, amount, TransactionType.USE, updatedPoint.updateMillis());
```
- 발생 가능 문제
  - 여러 스레드가 동시에 `SELECT` 후 계산 -> 덮어쓰기 발생
  - 동일 userId에서 충전/사용 순서가 꼬이면 잔액 손실 가능

## 3. 동시성 제어 방식 분류
| 구분        | 설명 | 대표 방식 / 기술                                                                        | 특징 |
|-----------|------|-----------------------------------------------------------------------------------|------|
| **JVM**   | JVM 내부 스레드 간 임계 구역 보호 | `synchronized`, `ReentrantLock`, `ReadWriteLock`, `Atomic*`, `ConcurrentHashMap` 등 | 단일 서버 환경에서 메모리 자원 안전, 빠름, 단일 변수 원자 연산 가능, 여러 변수 동시 업데이트 시 락 필요 |
| **DB**    | DB 레코드/테이블 단위 동시성 제어 | 비관적 락(`@Lock`), 낙관적 락(`@Version`), 이름가진 락(Named Lock)                             | 여러 서버/클라이언트 동시 접근 안전, DB 의존, 성능 영향 가능, 단일/멀티 서버 환경에서 모두 사용 가능 |
| **분산 환경** | 여러 서버 환경에서 동일 자원 동시 접근 방지 | Redis, Zookeeper, etcd 등 분산 락                                                     | 분산 환경에서 안전, 서버 간 직렬화 가능, 별도 인프라 필요, 구현 복잡 |

## 4. 적용한 동시성 제어 방식
### 4-1. ReentrantLock + CuncurrentHashMap 동적 락 관리
- ConcurrentHashMap을 사용하여 userId 별로 하나의 락 객체를 생성/공유
- lock 획득, 제거 범위 지정
```
    ReentrantLock lock = userLockMap.computeIfAbsent(userId, k -> new ReentrantLock());
    lock.lock();
    
    try {       
        // 포인트 충전/사용 로직
        return updatedPoint;         
    } finally {
        lock.unlock();
        log.debug("락 해제 - userId: {}", userId);
    }
```
### 4-2. 특징
- 애플리케이션 레벨 동시성 제어
- 동일 userId 별 독립적, 동적 관리
- 여러 작업을 하나로 묶어 락 처리 
### 4-3. 장점
- synchronized와 비교했을 때 논블로킹 락 처리, 타임아웃, 인터럽트 처리 등의 유연한 설정이 가능
- DB 연동 없이 자바 코드 레벨에서 안전하게 처리 가능

## 5. 결론
- userId 단위로 ReentrantLock 락 적용 → 동일 userId 동시 접근 안전
- 간단하지만 효과적인 자바 레벨 동시성 제어