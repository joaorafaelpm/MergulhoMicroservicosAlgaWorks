package com.algaworks.algadelivery.delivery.tracking.infrastructure.http.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class GatewayTimeoutException extends RuntimeException {
    public GatewayTimeoutException(String message) {
        super(message);
    }

    public GatewayTimeoutException() {
    }

    public GatewayTimeoutException(Throwable cause) {
        super(cause);
    }
}
