package com.playtika.sales.client.exception;

import java.io.IOException;

import static java.lang.String.format;

public class NotPossibleToDownloadFileException extends IOException {
    public NotPossibleToDownloadFileException(String url, Throwable ex) {
        super(format("Troubles with downloading csv-file from url=[%s]. Please check the url and retry. Root cause: [%s]", url, ex));
    }
}
