package com.trading.rdbs.common;

/**
 * 【職責】表示依 id 查詢不到資源時的領域例外。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
