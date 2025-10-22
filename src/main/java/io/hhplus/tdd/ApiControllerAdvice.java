package io.hhplus.tdd;

import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.exception.HanghaeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ErrorCode.SERVER_ERROR.getCode(), ErrorCode.SERVER_ERROR.getMessage()));
    }

    @ExceptionHandler(HanghaeException.class)
    public ResponseEntity<ErrorResponse> handleHanghaeException(HanghaeException e) {
        ErrorResponse response = new ErrorResponse(
                e.getErrorCodeValue(),
                e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }
}
