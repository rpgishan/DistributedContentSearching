package mrt.cse.msc.dc.cybertronez.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Util;

public class TestClient
{
  public static void main(final String[] args) throws IOException
  {
    Util util = new Util();
    final DatagramSocket socket;
    final InetAddress address;
    DatagramPacket packet;
    String received;

    byte[] buf;
    byte[] resbuf = new byte[65000];

    socket = new DatagramSocket();
    address = InetAddress.getByName("localhost");
    int port = 8082;

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
