package com.laioffer.jupiter.external;
//所有的异常都throw我们自己定义的异常，方便知道是那部分异常
//在我的系统里哪部分出现异常，对应相应的tag类型的异常
public class TwitchException extends RuntimeException {
    public TwitchException(String errorMessage) {
        super(errorMessage);
    }
}

