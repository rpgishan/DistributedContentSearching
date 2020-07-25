package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.FileNamesAndQueries;
import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;

public class TestSearch {

    public static void main(final String[] args) throws IOException {

        final Util util = new Util();
        //Test
        final DatagramSocket socket;
        final InetAddress address;
        DatagramPacket packet;
        String received;

        byte[] buf;
        final byte[] resbuf = new byte[65000];

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");

        final Long start = Calendar.getInstance().getTimeInMillis();
        Long end;
        final int startPort = 8081;
        final int noOfClients = 10;
        final int endPort = startPort + noOfClients;
//    for (int i = 0; i < 10; i++)
        {
            int port = startPort;
            while (port < endPort) {
                //Search
                for (final String fileName : FileNamesAndQueries.QUERIES) {
                    final String generateMessage = util
                            .generateMessage(Messages.SER.getValue(), "localhost", Integer.toString(port), "0", fileName);
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

        socket.close();

    }
}
