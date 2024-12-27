package com.dong.dongrpc.protocol;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum ProtocolMessageSerializerEnum {

    JDK(0, "jdk"),
    KRYO(1, "kryo"),
    HESSIAN(2, "hessian"),
    JSON(3, "json");

    private final int code;
    private final String text;

    ProtocolMessageSerializerEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    /**
     * 获取所有text列表
     *
     */
    public static List<String> getTextList() {
        List<String> textList = new ArrayList<>();
        for (ProtocolMessageSerializerEnum serializerEnum : ProtocolMessageSerializerEnum.values()) {
            textList.add(serializerEnum.getText());
        }
        return textList;
    }


    /**
     *
     * @param code
     * @return
     */
    public static ProtocolMessageSerializerEnum getByCode(int code) {
        for (ProtocolMessageSerializerEnum serializerEnum : ProtocolMessageSerializerEnum.values()) {
            if(serializerEnum.getCode() == code) {
                return serializerEnum;
            }
        }
        return null;
    }

    /**
     * 根据text获取枚举
     */
    public static ProtocolMessageSerializerEnum getByText(String text) {
        for (ProtocolMessageSerializerEnum serializerEnum : ProtocolMessageSerializerEnum.values()) {
            if(serializerEnum.getText().equals(text)) {
                return serializerEnum;
            }
        }
        return null;
    }

}
