package com.dong.dongrpc.protocol;

import lombok.Getter;

@Getter
public enum ProtocolMessageStatusEnum {

    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;

    private final int code;

    ProtocolMessageStatusEnum(String text, int code) {
        this.text = text;
        this.code = code;
    }

    /**
     * 根据CODE获取枚举
     */
    public static ProtocolMessageStatusEnum getByCode(int code) {
        for (ProtocolMessageStatusEnum statusEnum : ProtocolMessageStatusEnum.values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }
}
