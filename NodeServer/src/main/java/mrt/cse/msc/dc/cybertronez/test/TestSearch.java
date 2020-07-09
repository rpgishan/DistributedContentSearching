package mrt.cse.msc.dc.cybertronez.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Random;

import mrt.cse.msc.dc.cybertronez.FileNamesAndQueries;
import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Util;

public class TestSearch
{
  public static void main(final String[] args) throws IOException
  {
    Util util = new Util();
    //Test
    final DatagramSocket socket;
    final InetAddress address;
    DatagramPacket packet;
    String received;

    byte[] buf;
    byte[] resbuf = new byte[65000];
    final Random r = new Random();

    socket = new DatagramSocket();
    address = InetAddress.getByName("localhost");
    final int min = 32;
    final int max = 126;
//    for (int i = 0; i < 10; i++)
//    {
//      final int size = r.nextInt(10);
//      final StringBuilder msg = new StringBuilder("Start -- " + size + " : --> ");
//      for (int j = 0; j < size; j++)
//      {
//        msg.append((j != 0 && j % 1024 == 0) ? '\n' : (char) (min + r.nextInt(max - min)));
//      }
//      msg.append("\n\n");
//      buf = msg.toString().getBytes();
//      buf = "0027 JOIN 64.12.123.190 432 3 file1.txt file2.txt file3.txt".getBytes();
//      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8082);
//      socket.send(packet);
//      packet = new DatagramPacket(buf, buf.length);
//      socket.receive(packet);
//      String received = new String(
//          packet.getData(), 0, packet.getLength());
//      System.out.println(received);

//    }
    Long start = Calendar.getInstance().getTimeInMillis();
    Long end;
    int startPort = 8081;
    int noOfClients = 12;
    int endPort = startPort + noOfClients;
    for (int i = 0; i < 10; i++)
    {
      int port = startPort;
      while (port < endPort)
      {
        //Search
        for (String fileName : FileNamesAndQueries.QUERIES)
        {
          String generateMessage = util
              .generateMessage(Messages.SER.getValue(), "localhost", Integer.toString(port), "0", "0", fileName);
          System.out.println("generateMessage");
          System.out.println(generateMessage);
          buf = generateMessage.getBytes();
          packet = new DatagramPacket(buf, buf.length, address, port);
          socket.send(packet);
          packet = new DatagramPacket(resbuf, resbuf.length);
          socket.receive(packet);
          received = new String(packet.getData(), 0, packet.getLength());

          System.out.println(received);
          end = Calendar.getInstance().getTimeInMillis();
          System.out.println("Time diff up to now: " + (end - start));
          System.out.println();
//
        }
        port++;
      }
//      try
//      {
//        Thread.sleep(1000);
//      }
//      catch (InterruptedException e)
//      {
//        e.printStackTrace();
//      }
    }

    end = Calendar.getInstance().getTimeInMillis();
    System.out.println("Time diff: " + (end - start));
    System.out.println();

//    buf = Util.generateMessage("DETAILS").getBytes();
//    packet = new DatagramPacket(buf, buf.length, address, port);
//    socket.send(packet);
//    packet = new DatagramPacket(resbuf, resbuf.length);
//    socket.receive(packet);
//    received = new String(packet.getData(), 0, packet.getLength());
//    System.out.println(received);
    socket.close();

  }
}
