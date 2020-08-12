package com.dili.ss.exception;

/**
 * 业务异常
 * Created by Administrator on 2016/10/11.
 */
public class BusinessException extends InternalException {

    private static final String DEFAULT_MESSAGE = "业务异常!";

    public BusinessException() {
        super(DEFAULT_MESSAGE);
    }

    public BusinessException(String code, String message) {
        super(code, message);
    }

}

