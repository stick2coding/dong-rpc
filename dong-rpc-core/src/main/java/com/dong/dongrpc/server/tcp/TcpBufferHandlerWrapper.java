package com.dong.dongrpc.server.tcp;

import com.dong.dongrpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 对原有方法进行增强
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    /**
     * 这里希望通过recordParser来解决粘包问题
     */
    private final RecordParser recordParser;

    // 这里的目的是要对 handler<buffer>进行增强，那在构造的时候应该要传入一个handler<buffer>
    // 后续这里是要对我们的自定义的handler<buffer>进行增强
    public TcpBufferHandlerWrapper(Handler<Buffer> handler) {
        recordParser = initRecordParser(handler);
    }

    private RecordParser initRecordParser(Handler<Buffer> customHandler) {
        // 构造一个recordRarser，并设置好handler方法
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 接收数据（如何去一次读完整的数据）
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (-1 == size){
                    // 第一次读，先读数据长度
                    size = buffer.getInt(13);
                    // 修正下次读的长度
                    parser.fixedSizeMode(size);
                    // 这里就变成头的数据
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 第二次读就读到了实际数据
                    resultBuffer.appendBuffer(buffer);
                    // 这里已经解决了粘包问题，拿到了正确的数据，然后就需要调用原来的handler方法进行后续处理
                    customHandler.handle(resultBuffer);
                    // 读完之后，重置下次读数据的长度（就是请求头的长度）
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    // 重置缓冲区
                    resultBuffer = Buffer.buffer();
                }
            }
        });

        // 把这个实例化后的recordParser返回出去
        return parser;
    }


    /**
     * 这里是接管原来的入口，收到数据后，先从这里进行处理，具体的方法定义在init里面
     * @param buffer
     */
    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
