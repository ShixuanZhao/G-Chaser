package com.laioffer.jupiter.external;
//���е��쳣��throw�����Լ�������쳣������֪�����ǲ����쳣
//���ҵ�ϵͳ���Ĳ��ֳ����쳣����Ӧ��Ӧ��tag���͵��쳣
public class TwitchException extends RuntimeException {
    public TwitchException(String errorMessage) {
        super(errorMessage);
    }
}

