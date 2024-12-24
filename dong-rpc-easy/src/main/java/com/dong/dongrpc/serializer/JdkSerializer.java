package com.dong.dongrpc.serializer;

import java.io.*;

/**
 * 使用java自带的序列化器来实现序列化接口
 */
public class JdkSerializer implements Serializer{

    /**
     * 序列化
     * @param obj
     * @return
     * @param <T>
     * @throws IOException
     */
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        // 定义一个字节数组输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 基于字节输出流创建一个对象输出流
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        // 将参数对象写入输出流中
        objectOutputStream.writeObject(obj);
        // 关闭流
        objectOutputStream.close();
        // 返回字节流的数组
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        // 定义一个字节输入流，并输入参数数组
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        // 基于字节输入流，定义一个对象输入流
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        try {
            // 将对象输入流转换为对象
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭流
            objectInputStream.close();
        }
    }
}
