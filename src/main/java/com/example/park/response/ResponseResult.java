package com.example.park.response;

public class ResponseResult {

    public int code;
    public String message;
    private boolean success;
    private Object data;

    ResponseResult(ResponseState responseState){
        this.code=responseState.getCode();
        this.message=responseState.getMessage();
    }

    public static ResponseResult SUCCESS(){
        return new ResponseResult(ResponseState.SUCCESS);
    }

    public static ResponseResult SUCCESS(String suc){
        ResponseResult responseResult = new ResponseResult(ResponseState.SUCCESS);
        responseResult.setMessage(suc);
        return responseResult;
    }

    public static ResponseResult LOGIN_DEFAULT(){
        return new ResponseResult(ResponseState.DEFAULT);
    }

    public static ResponseResult DEFAULT(){
        return new ResponseResult(ResponseState.DEFAULT);
    }

    public static ResponseResult DEFAULT(String def){
        ResponseResult responseResult = new ResponseResult(ResponseState.DEFAULT);
        responseResult.setMessage(def);
        return responseResult;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public ResponseResult setData(Object data) {
        this.data = data;
        return this;
    }
}
