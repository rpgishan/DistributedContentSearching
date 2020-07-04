package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class TestClient
{
  public static void main(String[] args) throws IOException
  {
    DatagramSocket socket;
    InetAddress address;

    byte[] buf;
    Random r = new Random();

    socket = new DatagramSocket();
    address = InetAddress.getByName("localhost");
    final int min = 32;
    final int max = 126;
    for (int i = 0; i < 10; i++)
    {
      int size = r.nextInt(10);
      StringBuilder msg = new StringBuilder("Start -- "+size+" : --> ");
      for (int j = 0; j < size; j++)
      {
        msg.append((j != 0 && j % 1024 == 0) ? '\n' : (char) (min + r.nextInt(max - min)));
      }
      msg.append("\n\n");
      buf = msg.toString().getBytes();
      DatagramPacket packet
          = new DatagramPacket(buf, buf.length, address, 8082);
      socket.send(packet);
      packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      String received = new String(
          packet.getData(), 0, packet.getLength());
      System.out.println(received);

    }

    buf = "end".getBytes();
    DatagramPacket packet
        = new DatagramPacket(buf, buf.length, address, 8082);
    socket.send(packet);
    packet = new DatagramPacket(buf, buf.length);
    socket.receive(packet);
    String received = new String(
        packet.getData(), 0, packet.getLength());
    System.out.println(received);
    socket.close();

  }
}
