package com.zhangyu.sca.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构体
 *
 * @author zhangyu
 * @date 2024/9/24 22:46
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;

    private String msg;

    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail() {
        return fail(ResultCode.SYSTEM_EXECUTION_ERROR.getMsg());
    }

    public static <T> Result<T> fail(String msg) {
        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> fail(IResultCode resultCode) {
        return result(resultCode, null);
    }

    public static <T> Result<T> fail(IResultCode resultCode, String msg) {
        return result(resultCode.getCode(), msg, null);
    }

    /**
     * 判断是否成功，成功返回true，失败返回false
     *
     * @param result
     * @return
     */
    public static boolean isSuccess(Result<?> result) {
        return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
    }

    /**
     * 判断是否成功，成功返回成功，失败返回失败
     *
     * @param status
     * @return
     */
    public static <T> Result<T> judge(boolean status) {
        return status ? success() : fail();
    }

    // ======= private ========

    private static <T> Result<T> result(IResultCode resultCode, T data) {
        return result(resultCode.getCode(), resultCode.getMsg(), data);
    }

    private static <T> Result<T> result(String code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

}
