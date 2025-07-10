# [항해99 백엔드 플러스 9기] 1-1. TDD

## 프로젝트 소개
- API 로직 개발, TDD 방법론을 익히기 위한 API 프로젝트 입니다.
- 작업 내용 : 유저 포인트 관련 API 로직 개발, 테스트 코드 작성

## API 문서
| Method | Endpoint              | Description                     | Policy                                                                                  |
|--------|-----------------------|---------------------------------|-----------------------------------------------------------------------------------------
| GET    | /point/{id}           | 특정 유저의 포인트를 조회             | 1. 유저 id는 long 타입의 숫자로만 이루어져있다. 해당 타입의 유저는 모두 존재하고, 최초 포인트는 0으로 초기화 되어있다.               |
| GET    | /point/{id}/histories | 특정 유저의 포인트 충전/이용 내역을 조회  |                                                                                         |
| PATCH  | /point/{id}/charge    | 특정 유저의 포인트를 충전              | 1. 충전 금액은 0보다 큰 양수여야 한다.<br/>2. 포인트 최대 잔고는 100000 이며, 충전 시 최대 잔고가 넘는 경우 최대 10000 까지만 저장된다. |
| PATCH  | /point/{id}/use       | 특정 유저의 포인트를 사용              | 1. 사용 금액은 0보다 큰 양수여야 한다.<br/>2. 사용 금액만큼 잔액이 있어야 한다. |

## Test 구분
hhplus-tdd-java/<br/>
├── src/<br/>
│ ├── test/<br/>
│ │ ├── java/<br/>
│ │ │ ├── io/hhplus/tdd/point<br/>
└──────└──PointControllerTest.java : 통합 테스트
└──────└──PointServiceUnitTest.java : 포인트 단위 테스트
└──────└──UserPointHistoryTest.java : 포인트 이력 단위 테스트
└──────└──UserPointTest.java : UserPoint 객체 단위 테스트