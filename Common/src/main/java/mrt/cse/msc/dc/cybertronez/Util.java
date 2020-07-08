package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {

    private static final Logger LOGGER = LogManager.getLogger(Util.class);

    public static String sendMessage(byte[] message, String hostName, DatagramSocket socket, int port) {

        InetAddress address = null;
        String received = "";

        try {
            socket.setSoTimeout(60000);

            address = InetAddress.getByName(hostName);

        } catch (UnknownHostException e) {
            LOGGER.error("Error while retrieving InetAddress ", e);
        } catch (SocketException e) {
            LOGGER.error("Error while setting socket timeout ", e);
        }

        byte[] responseBuffer = new byte[65536];
        if (address != null) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
            try {
                socket.send(packet);
                packet = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(packet);
                received = new String(packet.getData(), 0, packet.getLength());
                String finalReceived = received;
                LOGGER.info("sendSocketMessage received: {}" ,()-> finalReceived);
            } catch (IOException e) {
                LOGGER.error("Error while sending packet", e);

            }
        }
        return received;

    }

    public static String generateMessage(final String... args) {

        final StringBuilder sb = new StringBuilder("####");

        for (final String word : args) {
            if (sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(word);
        }
        final String length = String.format("%04d", sb.length());
        sb.replace(0, 4, length);

    LOGGER.debug("generateMessage: {}", sb::toString);

        return sb.toString();
    }

    public static String processRegisterResponse(String response, List<Node> connectedNodes) {

        StringTokenizer st = new StringTokenizer(response, " ");
        String length = st.nextToken();
        String command = st.nextToken();
        int noOfNodes = Integer.parseInt(st.nextToken());

        for (int i = 0; i < noOfNodes; i++) {
            String ip = st.nextToken();
            String port = st.nextToken();
            connectedNodes.add(new Node(ip, port));
        }

    StringBuilder stringBuilder = new StringBuilder();
    connectedNodes.forEach(s -> stringBuilder.append(s).append(" "));
    LOGGER.debug("joinBS connectedNodes: {}", stringBuilder::toString);
    LOGGER.debug("JoinBS Successful");

        return response;

    }

}
