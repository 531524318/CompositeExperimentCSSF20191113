package com.huazhi.changsha.compositeexperiment.model;

/**
 * evenbus消息
 *
 * **/
public class EventMsgModel {
    private String msg;
    private String param;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public EventMsgModel(String msg){
        this.msg = msg;
    }

    public EventMsgModel(String msg,String param){
        this.msg = msg;
        this.param = param;
    }

}
