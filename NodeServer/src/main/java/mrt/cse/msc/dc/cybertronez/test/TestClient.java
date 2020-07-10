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

public class TestClient {

    public static void main(final String[] args) throws IOException {

        Util util = new Util();
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
        int port = 8082;
        //Search

        buf = util.generateMessage(Messages.SER.getValue(), "localhost", Integer.toString(port), "0", "0",
                FileNamesAndQueries.QUERIES.get(new Random().nextInt(FileNamesAndQueries.QUERIES.size()))).getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(resbuf, resbuf.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength());

        Long end = Calendar.getInstance().getTimeInMillis();
        System.out.println(received);
        System.out.println("Time diff: " + (end - start));
        System.out.println();

        buf = util.generateMessage("DETAILS").getBytes();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(resbuf, resbuf.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength());
        System.out.println(received);
        socket.close();

    }
}
