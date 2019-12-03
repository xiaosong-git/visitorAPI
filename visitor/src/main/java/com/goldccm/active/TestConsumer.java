package com.goldccm.active;

/**
 * @program: goldccm
 * @description:
 * @author: cwf
 * @create: 2019-12-02 23:14
 **/
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

//import org.apache.activemq.transport.stomp.StompConnection;
//import org.apache.activemq.transport.stomp.StompFrame;

public class TestConsumer {
    public static void main(String[] args) throws Exception {
        // 建立连接
//        StompConnection con = new StompConnection();
//        Socket so = new Socket("localhost", 61613);
//        con.open(so);
//        con.setVersion("1.2");
//        con.connect("admin", "admin");
//
//        String ack = "client";
//        con.subscribe("/test", "client");
//        // 接受消息（使用循环进行）
//        for (;;) {
//            StompFrame frame = null;
//            try {
//                // 注意，如果没有接收到消息，
//                // 这个消费者线程会停在这里，直到本次等待超时
//                frame = con.receive();
//            } catch (SocketTimeoutException e) {
//                continue;
//            }
//
//            // 打印本次接收到的消息
//            System.out.println("frame.getAction() = " + frame.getAction());
//            Map<String, String> headers = frame.getHeaders();
//            String meesage_id = headers.get("message-id");
//            System.out.println("frame.getBody() = " + frame.getBody());
//            System.out.println("frame.getCommandId() = " + frame.getCommandId());
//
//            // 在ack是client标记的情况下，确认消息
//            if ("client".equals(ack)) {
//                con.ack(meesage_id);
//            }
//        }
    }
}
