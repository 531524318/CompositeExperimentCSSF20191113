package com.huazhi.changsha.compositeexperiment.model;

/**
 *
 * 返回结果数据实体
 *
 * **/
public class ResultModel<T> {
    private String code;//状态码：大于或等于0为成功
    private String msg;//消息描述
    private T result;//返回结果

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
