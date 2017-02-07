package com.zhihuitech.ccgljyb.util;

//import org.apache.activemq.transport.stomp.StompConnection;
//import org.fusesource.stomp.jms.StompJmsConnectionFactory;
//import org.fusesource.stomp.jms.StompJmsDestination;
//
//import javax.jms.*;

/**
 * Created by Administrator on 2016/7/21.
 */
public class StompUtil {

//    public static StompConnection initConfiguration() {
//        StompConnection conn;
//        try {
//            conn = new StompConnection();
//            conn.open("v.wx91go.com", 61613);
//            conn.connect("admin", "P2ssw0rd!");
//            conn.subscribe("/queue/cat");
//            return conn;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static void send(String data) {
//        try {
//            String user = env("ACTIVEMQ_USER", "admin");
//            String password = env("ACTIVEMQ_PASSWORD", "P2ssw0rd!");
//            String host = env("ACTIVEMQ_HOST", "v.wx91go.com");
//            int port = Integer.parseInt(env("ACTIVEMQ_PORT", "61613"));
//            String destination = arg(null, 0, "/queue/cat");
//            StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
//            factory.setBrokerURI("tcp://" + host + ":" + port);
////            Connection connection = factory.createConnection(user, password);
////            connection.start();
////            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
////            Destination dest = new StompJmsDestination(destination);
////            MessageProducer producer = session.createProducer(dest);
////            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
////            TextMessage msg = session.createTextMessage(data);
////            producer.send(msg);
////            connection.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String env(String key, String defaultValue) {
//        String rc = System.getenv(key);
//        if( rc== null ) {
//            return defaultValue;
//        }
//        return rc;
//    }
//
//    private static String arg(String []args, int index, String defaultValue) {
//        if( index < args.length ) {
//            return args[index];
//        } else {
//            return defaultValue;
//        }
//    }

}
