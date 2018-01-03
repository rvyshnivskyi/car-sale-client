package com.playtika.sales.client.web;

import com.playtika.sales.client.exception.NotPossibleToDownloadFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ExceptionHandler(NotPossibleToDownloadFileException.class)
    @ResponseStatus(NOT_FOUND)
    public String handleNotPossibleToDownloadFileException(NotPossibleToDownloadFileException e) {
        return processException("Can't download file from the request url: %s.", e);
    }

    private String processException(String messageFormat, Exception e) {
        String errorMessage = format(messageFormat, e.getMessage());
        log.error(errorMessage, e);
        return errorMessage;
    }
}
