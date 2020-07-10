package mrt.cse.msc.dc.cybertronez;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {

    private static final Logger LOGGER = LogManager.getLogger(Util.class);
    public static final int BUFFER_SIZE = 10000;
    public static final int DEFAULT_TIMEOUT = 30000;
    private HashGenerator hashGenerator = new HashGenerator();
    public String sendMessage(byte[] message, String hostName, DatagramSocket socket, int port, int timeout) {

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

        byte[] responseBuffer = new byte[BUFFER_SIZE];
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
                String finalReceived = received;
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

    boolean isSocketAvailable(Node node)
    {
        try (DatagramSocket socket = new DatagramSocket())
        {
            byte[] pingMsg = generateMessage(Messages.PING.getValue()).getBytes();
            String response = sendMessage(pingMsg, node.getIp(), socket, node.getPort(), 5000);
            return response.contains(Messages.PING_OK.getValue());
        }
        catch (SocketException e)
        {
            LOGGER.error("SocketException", e);
        }
        return false;
    }

    public String processRegisterResponse(String response, List<Node> connectedNodes) {

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

    public List<String> extractFileNames(StringTokenizer st)
    {
        final int noOfFiles = Integer.parseInt(st.nextToken());
        final List<String> fileNames = new ArrayList<>(noOfFiles);
        String fileNameSeg = st.nextToken();
        StringTokenizer fileNameTokenizer = new StringTokenizer(fileNameSeg, ",");

        for (int i = 0; i < noOfFiles; i++)
        {
            fileNames.add(fileNameTokenizer.nextToken());
        }

        return fileNames;
    }

    public Node selectNode(String fileName, List<Node> nodeList) {

        String fileHash = hashGenerator.getHash(fileName);
        List<Integer> diffList = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            Node currentNode = nodeList.get(i);
            int diff = hashGenerator.getDifference(fileHash.getBytes(), currentNode.getUserNameHash().getBytes());
            diffList.add(diff);
        }
        int nodeIndex = Collections.min(diffList);
        return nodeList.get(nodeIndex);
    }

    public List<Node> selectFilesForNode(Set<String> fileList, List<Node> nodeList) {

        for (String fileName : fileList) {
            int currentDiff = 0;
            int leastDiffIndex = 0;
            String fileHash = hashGenerator.getHash(fileName);
            for (int i = 0; i < nodeList.size(); i++) {
                int diff = hashGenerator.getDifference(fileHash.getBytes(),
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
            Node npde = nodeList.get(leastDiffIndex);
            nodeList.get(leastDiffIndex).addFileToList(fileName);
            LOGGER.info("Selected file list: {}", () -> npde.getFieList());
        }
        return nodeList;
    }

}
