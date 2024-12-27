package com.dong.dongrpc.protocol;

import lombok.Getter;


/**
 * 自定义协议消息类型枚举
 */
@Getter
public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEAT_BEAT(2),
    OTHER(3);

    private final int typeCode;

    ProtocolMessageTypeEnum(int typeCode) {
        this.typeCode = typeCode;
    }


    /**
     * 根据code获取枚举
     */
    public static ProtocolMessageTypeEnum getByCode(int typeCode) {
        for (ProtocolMessageTypeEnum typeEnum : values()) {
            if (typeEnum.getTypeCode() == typeCode) {
                return typeEnum;
            }
        }
        return null;
    }


}
