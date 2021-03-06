package com.semicolon.spring.error;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.semicolon.spring.error.exception.BusinessException;
import com.semicolon.spring.error.exception.ErrorCode;
import com.semicolon.spring.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import javax.validation.UnexpectedTypeException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.warn("HandleMethodArgumentNotValidException : " + e);
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.BAD_REQUEST.getStatus(), ErrorCode.BAD_REQUEST.getMessage()),
                HttpStatus.valueOf(ErrorCode.BAD_REQUEST.getStatus()));
    }

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e){
        final var errorCode = e.getErrorCode();
        log.warn("BusinessException : " + e);

        return new ResponseEntity<>(new ErrorResponse(errorCode.getStatus(), errorCode.getMessage()),
                HttpStatus.valueOf(errorCode.getStatus()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    protected  ResponseEntity<ErrorResponse> handleServletException(InvalidTokenException e){
        log.warn("InvalidTokenException : " + e);
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.INVALID_TOKEN.getStatus(), ErrorCode.INVALID_TOKEN.getMessage()),
                HttpStatus.valueOf(ErrorCode.INVALID_TOKEN.getStatus()));
    }

    @ExceptionHandler(FirebaseMessagingException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(FirebaseMessagingException e){
        log.warn("FirebaseMessagingException : " + e);

        return new ResponseEntity<>(new ErrorResponse(404, e.getMessage()),
                HttpStatus.valueOf(404));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(ConstraintViolationException e) {
        log.warn("ConstraintViolation : " + e);
        String message = e.getMessage();
        return new ResponseEntity<>(new ErrorResponse(400, message.split("interpolatedMessage='")[1].split("'")[0]),
                HttpStatus.valueOf(400));
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(UnexpectedTypeException e) {
        log.warn("UnexpectedType : " + e);
        String message = e.getMessage();
        return new ResponseEntity<>(new ErrorResponse(400, message),
                HttpStatus.valueOf(400));
    }

    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(NullPointerException e) {
        log.warn("NullPointer : " + e);
        String message = e.getMessage();
        return new ResponseEntity<>(new ErrorResponse(400, message),
                HttpStatus.valueOf(400));
    }

}
