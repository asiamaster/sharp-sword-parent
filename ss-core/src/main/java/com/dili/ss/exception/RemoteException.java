package com.dili.ss.exception;

import com.dili.ss.constant.ResultCode;

/**
 * 远程调用异常
 * @author asiamastor
 * @since 2020-12-9
 */
public class RemoteException extends AppException{
	private static final long serialVersionUID = 7890234578980L;

	public RemoteException() {
		super();
		this.message = "远程调用错误!";
		this.code = ResultCode.REMOTE_ERROR;
	}

	public RemoteException(String message) {
		super(message);
		this.code = ResultCode.REMOTE_ERROR;
	}

	public RemoteException(String message, Throwable cause) {
		super(message, cause);
		this.code = ResultCode.REMOTE_ERROR;
	}

	public RemoteException(Throwable cause) {
		super(cause);
		this.code = ResultCode.REMOTE_ERROR;
	}

    public RemoteException(String code, String message) {
	        super(code,message);
	}

    public RemoteException(String code, String errorData, String message) {
        super(code,errorData,message);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}
