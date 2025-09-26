package org.example.airesumescoring.exception;

import org.springframework.http.HttpStatus;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends CustomException {
    /**
     * 构造方法
     *
     * @param resourceName 资源名称
     * @param fieldName    字段名称
     * @param fieldValue   字段值
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                new Object[]{resourceName, fieldName, fieldValue});
    }

    /**
     * 简化构造方法
     *
     * @param resourceName 资源名称
     */
    public ResourceNotFoundException(String resourceName) {
        super(String.format("%s not found", resourceName),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                new Object[]{resourceName});
    }
}
