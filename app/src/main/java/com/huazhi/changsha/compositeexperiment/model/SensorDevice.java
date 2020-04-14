package com.huazhi.changsha.compositeexperiment.model;

/**
 *
 * 传感器设备
 *
 * **/
public class SensorDevice {
    private String type;//设备类型
    private String number;//设备编号
    private String srcData;//从服务器返回的数据

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSrcData() {
        return srcData;
    }

    public void setSrcData(String srcData) {
        this.srcData = srcData;
    }
}
