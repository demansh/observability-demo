package com.dem.obs.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "server error")
public class QueueException extends RuntimeException {
    public QueueException(Throwable cause) {
        super(cause);
    }
}
