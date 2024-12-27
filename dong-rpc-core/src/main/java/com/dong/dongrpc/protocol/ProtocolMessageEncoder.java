package com.dong.dongrpc.protocol;

import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 编码器，将java对象转成buffer类型
 */
public class ProtocolMessageEncoder {

    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        // 先校验
        if (protocolMessage == null || protocolMessage.getHeader() == null){
            return Buffer.buffer();
        }

        // 依次向缓存冲写入数据
        // 魔数、版本、序列化方式、消息类型、状态码、请求id、数据长度、数据
        Buffer buffer = Buffer.buffer();
        ProtocolMessage.Header header = protocolMessage.getHeader();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        // 开始放入数据、首先先进行序列化
        //获取指定的序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByCode(header.getSerializer());
        if (serializerEnum == null){
            throw new RuntimeException("序列化器不存在");
        }
        DongSerializer serializer = SerializerFactory.getInstance(serializerEnum.getText());
        byte[] data = serializer.serialize(protocolMessage.getBody());

        // 写入数据长度和数据
        buffer.appendInt(data.length);
        buffer.appendBytes(data);
        return buffer;

    }


}
