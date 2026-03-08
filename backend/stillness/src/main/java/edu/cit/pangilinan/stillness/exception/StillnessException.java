package edu.cit.pangilinan.stillness.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StillnessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public StillnessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
