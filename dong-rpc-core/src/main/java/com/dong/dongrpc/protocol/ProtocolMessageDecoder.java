package com.dong.dongrpc.protocol;

import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 解码器
 */
public class ProtocolMessageDecoder {

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        ProtocolMessage.Header header = new ProtocolMessage.Header();

        // 依次读出buffer
        // 读魔数
        byte magic = buffer.getByte(0);
        // 校验
        if (magic != ProtocolConstant.PROTOCOL_MAGIC){
            throw new RuntimeException("magic error");
        }
        //依次将头信息放入header中
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        // 数据长度
        int bodyLength = buffer.getInt(13);
        header.setBodyLength(bodyLength);
        // 为了解决粘包的问题，我们这里往后读指定长度的数据
        byte[] bodyBytes = buffer.getBytes(17, 17 + bodyLength);

        // 反序列化
        // 拿到对应的序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByCode(header.getSerializer());
        if (serializerEnum == null){
            throw new RuntimeException("序列化器不存在");
        }
        DongSerializer serializer = SerializerFactory.getInstance(serializerEnum.getText());
        // 判断消息类型进行解析
        ProtocolMessageTypeEnum typeEnum = ProtocolMessageTypeEnum.getByCode(header.getType());
        if (typeEnum == null){
            throw new RuntimeException("消息类型不存在");
        }
        switch (typeEnum){
            case REQUEST:
                // 请求消息
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                // 响应消息
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            case HEAT_BEAT:
            case OTHER:
            default:
                throw new RuntimeException("消息类型不存在");
        }
    }


}
