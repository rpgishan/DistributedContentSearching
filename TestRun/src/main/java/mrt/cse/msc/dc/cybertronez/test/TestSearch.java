package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.FileNamesAndQueries;
import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;

public class TestSearch {

    private static final Logger LOGGER = LogManager.getLogger(TestSearch.class);

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

        Long start = Calendar.getInstance().getTimeInMillis();
        Long end;
        final int startPort = 8081;
        final int noOfClients = 10;
        final int endPort = startPort + noOfClients;
//    for (int i = 0; i < 10; i++)
        {
            LOGGER.info("------------------------------------------------------------------------------------------------------------------------------------------------------");
            int port = 9 + startPort;
//            while (port < endPort) {
                //Search
                for (final String fileName : FileNamesAndQueries.QUERIES) {
                    start = Calendar.getInstance().getTimeInMillis();
                    final String generateMessage = util
                            .generateMessage(Messages.SER.getValue(), "localhost", Integer.toString(port), "0", fileName);
//                    System.out.println("generateMessage");
//                    System.out.println(generateMessage);
                    LOGGER.info("generateMessage {}", () -> generateMessage);
                    buf = generateMessage.getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                    packet = new DatagramPacket(resbuf, resbuf.length);
                    socket.receive(packet);
                    received = new String(packet.getData(), 0, packet.getLength());

//                    System.out.println(received);
                    String finalReceived = received;
                    LOGGER.info("received {}", () -> finalReceived);

                    end = Calendar.getInstance().getTimeInMillis();
//                    System.out.println("Time diff up to now: " + (end - start));
                    Long finalEnd = end;
                    Long finalStart = start;
                    LOGGER.info("Time diff: {}\n\n", () -> (finalEnd - finalStart));
//                    System.out.println();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                port++;
//            }
//      try
//      {
//        Thread.sleep(1000);
//      }
//      catch (InterruptedException e)
//      {
//        e.printStackTrace();
//      }
        }

//        end = Calendar.getInstance().getTimeInMillis();
////        System.out.println("Time diff: " + (end - start));
//        Long finalEnd1 = end;
//        LOGGER.info("Time diff: {}", () -> (finalEnd1 - start));
//        System.out.println();

        socket.close();

    }
}
