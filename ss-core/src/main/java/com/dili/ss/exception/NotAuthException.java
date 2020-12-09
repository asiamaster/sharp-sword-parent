/*
 * Copyright (c) 2014 www.diligrp.com All rights reserved.
 * 本软件源代码版权归----所有,未经许可不得任意复制与传播.
 */
package com.dili.ss.exception;

import com.dili.ss.constant.ResultCode;

/**
 * NotAuthException
 * @author asiamastor
 * @since 2020-08-12
 */
public class NotAuthException extends AppException{
	private static final long serialVersionUID = 987123460090011111L;
	public NotAuthException() {
		super();
		this.message = "未授权错误!";
		this.code = ResultCode.NOT_AUTH_ERROR;
	}
	
	public NotAuthException(String message) {
		super(message);
		this.code = ResultCode.NOT_AUTH_ERROR;
	}
	
	public NotAuthException(String message, Throwable cause) {
		super(message, cause);
		this.code = ResultCode.NOT_AUTH_ERROR;
	}
	
	public NotAuthException(Throwable cause) {
		super(cause);
		this.code = ResultCode.NOT_AUTH_ERROR;
	}
	
    public NotAuthException(String code, String errorData, String message) {
        super(code,errorData,message);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}
