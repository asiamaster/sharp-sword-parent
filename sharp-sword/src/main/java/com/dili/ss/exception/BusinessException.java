package com.dili.ss.exception;

/**
 * Created by Administrator on 2016/10/11.
 */
public class BusinessException extends RuntimeException {
    private String errorCode;
    private String errorMsg;

    public BusinessException(String errorCode, String errorMsg) {
        super(String.format("BusinessException{errorCode:%s, errorMsg:%s}", new Object[]{errorCode, errorMsg}));
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }
}

