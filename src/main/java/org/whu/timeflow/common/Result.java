package org.whu.timeflow.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200 成功, 500 失败
    private String msg;   // "同步成功"
    private T data;       // 具体数据

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }
}