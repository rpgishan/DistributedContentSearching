package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TestClient {

    private static final Logger LOGGER = LogManager.getLogger(TestClient.class);

    public static void main(final String[] args) throws IOException {

        final Util util = new Util();
        final DatagramSocket socket;
        final InetAddress address;
        DatagramPacket packet;
        String received;

        byte[] buf;
        final byte[] resbuf = new byte[65000];

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        int port = 8081;
        int endport = 8090;

//        LOGGER.info("------------------------------------------------------------------------------------------------------------------------------------------------------");

//        buf = util.generateMessage(Messages.PING.getValue()).getBytes();
//        packet = new DatagramPacket(buf, buf.length, address, port);
//        socket.send(packet);
//        packet = new DatagramPacket(resbuf, resbuf.length);
//        socket.receive(packet);
//        received = new String(packet.getData(), 0, packet.getLength());
//        String finalReceived = received;
//        LOGGER.info("received PING {}",()-> finalReceived);
//        System.out.println(received);
//        System.out.println();

//        buf = util.generateMessage(Messages.SEND_LEAVE.getValue()).getBytes();
//        packet = new DatagramPacket(buf, buf.length, address, 8086);
//        socket.send(packet);
//        packet = new DatagramPacket(resbuf, resbuf.length);
//        socket.receive(packet);
//        received = new String(packet.getData(), 0, packet.getLength());
//        String finalReceived3 = received;
//        LOGGER.info("received SEND_LEAVE {}",()-> finalReceived3);

        while (port <= endport) {
//            buf = util.generateMessage(Messages.DETAILS.getValue()).getBytes();
//            packet = new DatagramPacket(buf, buf.length, address, port);
//            socket.send(packet);
//            packet = new DatagramPacket(resbuf, resbuf.length);
//            socket.receive(packet);
//            received = new String(packet.getData(), 0, packet.getLength());
//            String finalReceived1 = received;
//            LOGGER.info("received DETAILS {}",()-> finalReceived1);

            buf = util.generateMessage(Messages.SEARCHED_QUERIES.getValue()).getBytes();
            packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
            packet = new DatagramPacket(resbuf, resbuf.length);
            socket.receive(packet);
            received = new String(packet.getData(), 0, packet.getLength());
            String finalReceived2 = received;
            LOGGER.info("received SEARCHED_QUERIES {}", () -> finalReceived2);


            port++;
        }

        socket.close();

    }
}
