package com.dili.ss.exception;

import com.dili.ss.constant.ResultCode;

/**
 * 内部异常
 *
 * @author WangMi
 * Created by asiamaster on 2017/7/31 0031.
 */
public class InternalException extends RuntimeException {
	private static final long serialVersionUID = -613311234553268165L;
	private static final String DEFAULT_MESSAGE = "程序内部错误!";

	/**
	 * 异常编码
	 * 默认500
	 */
	protected String code = ResultCode.INTERNAL_SERVER_ERROR;

	/**
	 * 异常消息
	 */
	protected String message = DEFAULT_MESSAGE;


	public InternalException(String message) {
		super(message);
		this.message = message;
	}

	public InternalException(String code, String message) {
		super(String.format("code:%s, message:%s", new Object[]{code, message}));
		this.code = code;
		this.message = message;
	}

	public InternalException(Throwable cause) {
		super(DEFAULT_MESSAGE, cause);
	}

	public InternalException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
