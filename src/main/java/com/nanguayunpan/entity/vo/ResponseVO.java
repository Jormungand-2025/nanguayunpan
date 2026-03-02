package com.nanguayunpan.entity.vo;

public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> result = new ResponseVO<>();
        result.setStatus("success");
        result.setCode(200);
        result.setData(data);
        return result;
    }

    public static <T> ResponseVO<T> success(String info, T data) {
        ResponseVO<T> result = new ResponseVO<>();
        result.setStatus("success");
        result.setCode(200);
        result.setInfo(info);
        result.setData(data);
        return result;
    }

    public static <T> ResponseVO<T> success() {
        ResponseVO<T> result = new ResponseVO<>();
        result.setStatus("success");
        result.setCode(200);
        return result;
    }

    public static <T> ResponseVO<T> success(String info) {
        ResponseVO<T> result = new ResponseVO<>();
        result.setStatus("success");
        result.setCode(200);
        result.setInfo(info);
        return result;
    }

    public static <T> ResponseVO<T> error(String info) {
        ResponseVO<T> result = new ResponseVO<>();
        result.setStatus("error");
        result.setCode(500);
        result.setInfo(info);
        return result;
    }

    public static <T> ResponseVO<T> error(Integer code, String info) {
        ResponseVO<T> result = new ResponseVO<>();
        result.setStatus("error");
        result.setCode(code);
        result.setInfo(info);
        return result;
    }

    // getter和setter方法
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}