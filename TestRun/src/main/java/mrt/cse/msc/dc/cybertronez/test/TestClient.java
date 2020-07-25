package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TestClient {

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
        final int port = 8082;

        buf = util.generateMessage(Messages.PING.getValue()).getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(resbuf, resbuf.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength());
        System.out.println(received);
        System.out.println();

        buf = util.generateMessage(Messages.DETAILS.getValue()).getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(resbuf, resbuf.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength());
        System.out.println(received);
        socket.close();

    }
}
