package mrt.cse.msc.dc.cybertronez;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class Util {

    private static final Logger LOGGER = LogManager.getLogger(Util.class);
    public static final int BUFFER_SIZE = 10000;
    public static final int DEFAULT_TIMEOUT = 30000;
    private HashGenerator hashGenerator = new HashGenerator();

    public String sendMessage(final byte[] message, final String hostName, final DatagramSocket socket, final int port, final int timeout) {

        InetAddress address = null;
        String received = "";

        try {
            socket.setSoTimeout(timeout);
            address = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            LOGGER.error("Error while retrieving InetAddress ", e);
        } catch (SocketException e) {
            LOGGER.error("Error while setting socket timeout ", e);
        }

        final byte[] responseBuffer = new byte[BUFFER_SIZE];
        if (address != null) {
            DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
            try {
                socket.send(packet);

                LOGGER.debug("sendSocketMessage sent to {}:{} from {}:{}:{}", () -> hostName, () -> port,
                        socket::getLocalAddress, socket::getPort, socket::getLocalPort);
                LOGGER.debug("sendSocketMessage sent: {}", () -> new String(message));

                packet = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(packet);
                received = new String(packet.getData(), 0, packet.getLength());
                final String finalReceived = received;
                LOGGER.debug("sendSocketMessage received: {}", () -> finalReceived);
            } catch (IOException e) {
                LOGGER.error("Error while sending packet", e);
            }
        }

        return received;
    }

    public String generateMessage(final String... args) {

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

    boolean isSocketAvailable(final Node node) {

        try (final DatagramSocket socket = new DatagramSocket()) {
            final byte[] pingMsg = generateMessage(Messages.PING.getValue()).getBytes();
            final String response = sendMessage(pingMsg, node.getIp(), socket, node.getPort(), 5000);
            return response.contains(Messages.PING_OK.getValue());
        } catch (SocketException e) {
            LOGGER.error("SocketException", e);
        }
        return false;
    }

    public String processRegisterResponse(final String response, final List<Node> connectedNodes) {

        final StringTokenizer st = new StringTokenizer(response, " ");
        final String length = st.nextToken();
        final String command = st.nextToken();
        final int noOfNodes = Integer.parseInt(st.nextToken());

        for (int i = 0; i < noOfNodes; i++) {
            final String ip = st.nextToken();
            final String port = st.nextToken();
            connectedNodes.add(new Node(ip, port));
        }

        final StringBuilder stringBuilder = new StringBuilder();
        connectedNodes.forEach(s -> stringBuilder.append(s).append(" "));
        LOGGER.debug("joinBS connectedNodes: {}", stringBuilder::toString);
        LOGGER.debug("JoinBS Successful");

        return response;

    }

    public List<String> extractFileNames(final StringTokenizer st) {

        final int noOfFiles = Integer.parseInt(st.nextToken());
        final List<String> fileNames = new ArrayList<>(noOfFiles);
        final String fileNameSeg = st.nextToken();
        final StringTokenizer fileNameTokenizer = new StringTokenizer(fileNameSeg, ",");

        for (int i = 0; i < noOfFiles; i++) {
            fileNames.add(fileNameTokenizer.nextToken());
        }

        return fileNames;
    }

    public Node selectNode(final String fileName, final List<Node> nodeList) {

        final String fileHash = hashGenerator.getHash(fileName);
        final List<Integer> diffList = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            final Node currentNode = nodeList.get(i);
            final int diff = hashGenerator.getDifference(fileHash.getBytes(), currentNode.getUserNameHash().getBytes());
            diffList.add(diff);
        }
        final int nodeIndex = Collections.min(diffList);
        return nodeList.get(nodeIndex);
    }

    public List<Node> selectFilesForNode(final Set<String> fileList, final List<Node> nodeList) {

        for (final String fileName : fileList) {
            int currentDiff = 0;
            int leastDiffIndex = 0;
            final String fileHash = hashGenerator.getHash(fileName);
            for (int i = 0; i < nodeList.size(); i++) {
                final int diff = hashGenerator.getDifference(fileHash.getBytes(),
                        nodeList.get(i).getUserNameHash().getBytes());
                if (currentDiff == 0) {
                    currentDiff = diff;
                    leastDiffIndex = i;
                }
                if (diff < currentDiff) {
                    currentDiff = diff;
                    leastDiffIndex = i;
                }
            }
            final Node npde = nodeList.get(leastDiffIndex);
            nodeList.get(leastDiffIndex).addFileToList(fileName);
            LOGGER.info("Selected file list: {}", () -> npde.getFieList());
        }
        return nodeList;
    }

}
