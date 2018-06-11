package com.ding.basic.bean;

/**
 * @author lijun Lee
 * @desc response data result
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/12/4 14:15
 */

public class Result<T> {

    private String respCode;
    private String message;
    private T data;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "respCode='" + respCode + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
