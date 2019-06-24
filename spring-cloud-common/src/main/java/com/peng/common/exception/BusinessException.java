package com.peng.common.exception;


/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1144969267587138347L;

    int code = -1;

    String message;

    Exception cause;

    public BusinessException() {
        super();
    }

    public BusinessException(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public BusinessException(int code, String message, Exception cause) {
        super();
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
    
    public BusinessException(EnumErrCode e){
    	 this.code = e.getCode();
         this.message = e.getDesc();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getCause() {
        return cause;
    }

    public void setCause(Exception cause) {
        this.cause = cause;
    }

}