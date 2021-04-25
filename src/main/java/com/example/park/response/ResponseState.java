package com.example.park.response;

public enum ResponseState {
    SUCCESS(200,"查询成功",true),
    DEFAULT(400,"操作失败",false),
    LOGIN_DEFAULT(499,"操作失败",false);

    private Integer code;
    private String message;
    private Boolean success;

    ResponseState(Integer code,String message,Boolean success){
        this.code=code;
        this.message=message;
        this.success=success;
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
}
