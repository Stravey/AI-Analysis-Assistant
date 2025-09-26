package org.example.airesumescoring.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义异常基类
 */
@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final Object[] args;

    /**
     * 构造方法
     *
     * @param message    异常消息
     * @param httpStatus HTTP状态码
     */
    public CustomException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = null;
        this.args = null;
    }

    /**
     * 构造方法
     *
     * @param message    异常消息
     * @param httpStatus HTTP状态码
     * @param errorCode  错误代码
     */
    public CustomException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * 构造方法
     *
     * @param message    异常消息
     * @param httpStatus HTTP状态码
     * @param errorCode  错误代码
     * @param args       消息参数
     */
    public CustomException(String message, HttpStatus httpStatus, String errorCode, Object[] args) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.args = args;
    }

}