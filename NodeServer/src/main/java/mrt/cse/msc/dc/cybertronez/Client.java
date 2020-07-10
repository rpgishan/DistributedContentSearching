package mrt.cse.msc.dc.cybertronez;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Client {

    private static Logger logger = null;
    private Node bsServer;
    private Node currentNode;
    private List<Node> connectedNodes = new ArrayList<>();
    private Set<Query> alreadySearchedQueries = new HashSet<>();
    private Map<String, Set<Node>> routingTable = new HashMap<>();
    private Set<String> fileNames = new HashSet<>();
    private Util util;

    public static void main(final String[] args) {

        if (args.length == 4) {
            final String ip = args[0];
            final String port = args[1];
            final String bsIp = args[2];
            final String bsPort = args[3];

            new Client(ip, port, bsIp, bsPort);
        }
    }

    public Client(final String clientIp, final String clientPort, final String bsIp, final String bsPort) {

        bsServer = new Node(bsIp, bsPort);
        currentNode = new Node(clientIp, clientPort);
        logger = LogManager.getLogger(Client.class.getName() + " - " + currentNode.toString());
        util = new Util();
        populateFiles();
        openSocket();
        join();
        startHealthCheck();
    }

    private void startHealthCheck() {

        final Thread t1 = new Thread(new HealthCheck(this));
        t1.start();
    }

    private void openSocket() {

        logger.info("Opening Socket");
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(currentNode.getPort())) {
                byte[] requestBuffer;
                byte[] responseBuffer;
                boolean running = true;
                String received;
                DatagramPacket packet;
                InetAddress sendersAddress;
                int sendersPort;

                while (running) {
                    logger.info("Open Socket waiting in {}:{}", currentNode::getIp, currentNode::getPort);
                    requestBuffer = new byte[Util.BUFFER_SIZE];
                    packet = new DatagramPacket(requestBuffer, requestBuffer.length);
                    socket.receive(packet);
                    received = new String(packet.getData(), packet.getOffset(), packet.getLength());

                    DatagramPacket finalPacket = packet;
                    logger.info("openSocket msg from {}:{} to current {}:{}", finalPacket::getAddress, finalPacket::getPort,
                            currentNode::getIp, currentNode::getPort);
                    String finalReceived = received;
                    logger.info("openSocket received {}", () -> finalReceived);

                    String response;
                    if (received.equals("end")) {
                        response = "Socket ended";
                        logger.info("response: {}", () -> response);
                        running = false;
                    } else {
                        response = processSocketMessage(received);
                    }

                    logger.info("openSocket response {}", () -> response);

                    responseBuffer = response.getBytes();
                    sendersAddress = packet.getAddress();
                    sendersPort = packet.getPort();
                    packet = new DatagramPacket(responseBuffer, 0, responseBuffer.length, sendersAddress, sendersPort);
                    socket.send(packet);
                    logger.info("openSocket response is sent to {}:{} from {}:{}", packet::getAddress, packet::getPort,
                            currentNode::getIp, currentNode::getPort);

                }
            } catch (IOException e) {
                logger.error("Exception", e);
            }
        }).start();
    }

    private String processSocketMessage(final String message) {

        logger.info("processSocketMessage message: {}", () -> message);

        final StringTokenizer st = new StringTokenizer(message, " ");
        final String length = st.nextToken();
        final String command = st.nextToken();

        if (command.equals(Messages.JOIN.getValue())) {
            return processJoinRequest(st);
        } else if (command.equals(Messages.LEAVE.getValue())) {
            return processLeaveRequest(st);
        } else if (command.equals(Messages.SER.getValue())) {
            return processSearchRequest(st);
        } else if (command.equals(Messages.DETAILS.getValue())) {
            return processDetailsRequest();
        } else if (command.equals(Messages.PING.getValue())) {
            return util.generateMessage(Messages.PING_OK.getValue());
        } else {
            return util.generateMessage(Messages.ERROR.toString());
        }
    }

    private void populateFiles() {

        final Random r = new Random();
        final int minFiles = 3;
        final int maxFiles = 5;
        final int noOfFiles = minFiles + r.nextInt(maxFiles - minFiles + 1);
        while (fileNames.size() < noOfFiles) {
            String fileName = FileNamesAndQueries.FILE_NAMES.get(r.nextInt(FileNamesAndQueries.FILE_NAMES.size()));
            fileName = fileName.replace(' ', '_');
            fileNames.add(fileName);
        }
        fileNames.forEach(name -> addDataToRoutingTable(name, currentNode));
        if (logger.isInfoEnabled()) {
            final StringBuilder fileSb = new StringBuilder();
            fileNames.forEach(s -> fileSb.append(s).append(", "));
            logger.info("populateFiles fileNames: {}", fileSb::toString);
        }
    }

    private String processDetailsRequest() {

        final StringBuilder sb = new StringBuilder();
        sb.append("Node - ").append(currentNode.toString());
        sb.append("\n*****ROUTING_TABLE*****");
        routingTable.forEach((s, nodes) -> {
            sb.append("\n").append(s).append(" - ");
            nodes.forEach(node -> sb.append(node).append(" , "));
        });
        sb.append("\n*****FILE_NAMES*****").append("\n");
        fileNames.forEach(s -> {
            sb.append(s).append(" , ");
        });
        sb.append("\n*****CONNECTED_NODES*****").append("\n");
        connectedNodes.forEach(s -> {
            sb.append(s).append(" , ");
        });
        return util.generateMessage(Messages.DETAILS.getValue(), sb.toString());
    }

    private String processLeaveRequest(final StringTokenizer st) {

        final String ip = st.nextToken();
        final String port = st.nextToken();
        final Node node = new Node(ip, port);
        final Optional<Node> first = connectedNodes.stream().filter(node::equals).findFirst();
        final boolean response = first.isPresent() ? connectedNodes.remove(first.get()) : false;

        logger.info("response: {}", () -> response);

        final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
        return util.generateMessage(Messages.LEAVEOK.getValue(), code.getValue());
    }

    private String processSearchRequest(final StringTokenizer st) {

        final String ip = st.nextToken();
        final String port = st.nextToken();
        int hops = Integer.parseInt(st.nextToken());

        final Set<Node> propagatedNodes = new HashSet<>();
        for (int i = 0; i < hops; i++) {
            final String nodeIp = st.nextToken();
            final String nodePort = st.nextToken();
            final Node node = new Node(nodeIp, nodePort);
            propagatedNodes.add(node);
        }

        hops++;

        final StringBuilder fileNameBuilder = new StringBuilder();
        while (st.hasMoreElements()) {
            if (fileNameBuilder.length() != 0) {
                fileNameBuilder.append("_");
            }
            fileNameBuilder.append(st.nextToken());
        }

        return search(new Query(fileNameBuilder.toString(), new Node(ip, port)), hops, propagatedNodes);
    }

    private String search(final Query query, final int hops, final Set<Node> propagatedNodes) {

        logger.info("Begin search of \"{}\" in {}. Current hops: {}", () -> query, currentNode::toString, () -> hops);
        if (alreadySearchedQueries.contains(query)) {
            logger.info("Already searched of \"{}\" in {}. Current hops: {}", () -> query, currentNode::toString, () -> hops);
            final StringBuilder propagatedNodesBuilder = new StringBuilder(Integer.toString(propagatedNodes.size()));
            propagatedNodes.forEach(node -> {
                propagatedNodesBuilder.append(" ").append(node.getIp()).append(" ").append(node.getPort());
            });
            return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE9998.getValue(),
                    Messages.ALREADY_SEARCHED.getValue(), Integer.toString(hops), propagatedNodesBuilder.toString());
        }
        alreadySearchedQueries.add(query);
        propagatedNodes.add(currentNode);

        final String[] queryWords = query.getQuery().split("_");
        final List<String> foundFiles = new ArrayList<>();
        fileNames.forEach(fileName -> {
            final String[] fileNameWords = fileName.split("_");
            if (fileNameWords.length >= queryWords.length) {
                boolean valid = true;
                int j = 0;
                for (int i = 0; i < queryWords.length && valid; i++) {
                    valid = false;
                    while (j < fileNameWords.length) {
                        if (queryWords[i].equalsIgnoreCase(fileNameWords[j])) {
                            valid = true;
                            break;
                        }
                        j++;
                    }
                }
                if (valid) {
                    foundFiles.add(fileName);
                    logger.info("Found file \"{}\" for the query of \"{}\" in {}.", () -> fileName, () -> query,
                            currentNode::toString);
                }
            }
        });

        if (foundFiles.isEmpty()) {
            logger.info("No matching files found in {}.", currentNode::toString);
            return propagateSearch(query, hops, propagatedNodes);
        }

        final List<String> foundFileNames = foundFiles.stream().map(fileName -> fileName.replace(' ', '_'))
                .collect(Collectors.toList());// TODO change
        final String fileNameStrings = convertFileNameListToString(foundFileNames);

        return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE0.getValue(), Integer.toString(hops),
                convertPropagatedNodesToString(propagatedNodes), fileNameStrings, currentNode.toString());
    }

    private String convertFileNameListToString(final List<String> foundFileNames) {

        final StringBuilder sb = new StringBuilder(Integer.toString(foundFileNames.size()));
        foundFileNames.forEach(name -> {
            if (sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(name);
        });
        return sb.toString();
    }

    private String propagateSearch(final Query query, int hops, final Set<Node> propagatedNodes) {

        try (final DatagramSocket socket = new DatagramSocket()) {
            for (final Node node : connectedNodes) {
                final int finalHops = hops;
                logger.info("Propagate search of \"{}\" to {}. Current hops: {}", () -> query, node::toString, () -> finalHops);

                if (propagatedNodes.contains(node)) {
                    continue;
                }

                final String propagatedNodesString = convertPropagatedNodesToString(propagatedNodes);

                final byte[] buffer = util.generateMessage(Messages.SER.getValue(), query.getInitiatedNode().getIp(),
                        Integer.toString(query.getInitiatedNode().getPort()), Integer.toString(hops),
                        propagatedNodesString, query.getQuery()).getBytes();
                final String response = util.sendMessage(buffer, node.getIp(), socket, node.getPort(), Util.DEFAULT_TIMEOUT);
                final StringTokenizer st = new StringTokenizer(response, " ");
                final String length = st.nextToken();
                final String command = st.nextToken();
                if (command.equals(Messages.SEROK.getValue())) {
                    final String code = st.nextToken();
                    if (code.equals(Messages.CODE0.getValue()) || code.equals(Messages.ERROR.getValue())) {
                        return response;
                    } else if (code.equals(Messages.CODE9998.getValue())) {
                        final String error = st.nextToken();
                        final int newHops = Integer.parseInt(st.nextToken());
                        hops = newHops;

                        for (int i = 0; i < hops; i++) {
                            final String ip = st.nextToken();
                            final String port = st.nextToken();
                            propagatedNodes.add(new Node(ip, port));
                        }

                        logger.info("Got search response error of \"{}\" - \"{}\" for \"{}\" from {}. Current hops: {}", () -> code,
                                () -> error, () -> query, node::toString, () -> newHops);
                        continue;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("SocketException", e);
        }

        final int finalHops1 = hops;
        logger.info("File not found for \"{}\" in {}. Current hops: {}", () -> query, currentNode::toString, () -> finalHops1);

        final String propagatedNodesString = convertPropagatedNodesToString(propagatedNodes);

        return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE9998.getValue(), Messages.NOT_FOUND.getValue(),
                Integer.toString(hops), propagatedNodesString);
    }

    private String convertPropagatedNodesToString(final Set<Node> nodeCollection) {

        final StringBuilder propagatedNodesBuilder = new StringBuilder(nodeCollection.size());
        for (final Node node : nodeCollection) {
            if (propagatedNodesBuilder.length() != 0) {
                propagatedNodesBuilder.append(" ");
            }
            propagatedNodesBuilder.append(node.getIp()).append(" ").append(node.getPort());
        }

        return propagatedNodesBuilder.toString();
    }

    private void addDataToRoutingTable(final String fileName, final Node node) {

        if (!routingTable.containsKey(fileName)) {
            routingTable.put(fileName, new HashSet<>());
        }
        routingTable.get(fileName).add(node);
    }

    private boolean incomingRequestToPairUp(final String ip, final String port, final List<String> fileNames) {

        final Node node = new Node(ip, port);
        return incomingRequestToPairUp(node, fileNames);
    }

    private boolean incomingRequestToPairUp(final Node node, final List<String> fileNames) {

        logger.info("Incoming Request To Pair Up from {} with {} files.", node::toString,
                () -> Integer.toString(fileNames.size()));
        final boolean add = connectedNodes.add(node);
        fileNames.forEach(fileName -> addDataToRoutingTable(fileName, node));
        return add;//TODO handle errors
    }

    private void join() {

        new Thread(() -> {
            joinBS();
            outgoingRequestToPairUp();
        }).start();
    }

    private void joinBS() {

        try (final DatagramSocket socket = new DatagramSocket()) {
            final int port = bsServer.getPort();
            //unreg first then reg
            final String unregisterMessage = util.generateMessage(Messages.UNREG.getValue(), currentNode.getIp(),
                    Integer.toString(currentNode.getPort()), currentNode.getUsername());
            final String unRegResponse = util.sendMessage(unregisterMessage.getBytes(), bsServer.getIp(), socket, port, Util.DEFAULT_TIMEOUT);

            if (unRegResponse.equals(Messages.CODE9999.getValue())) {
                logger.info("Error in unreg message");
            }

            final String joinMessage = util.generateMessage(Messages.REG.getValue(), currentNode.getIp(),
                    Integer.toString(currentNode.getPort()), currentNode.getUsername());

            //send register  request to bs
            final String response = util.sendMessage(joinMessage.getBytes(), bsServer.getIp(), socket, port, Util.DEFAULT_TIMEOUT);

            if (response.equals(Messages.CODE9999.getValue())) {
                logger.info("Error in reg message");
            } else if (response.equals(Messages.CODE9998.getValue())) {
                logger.info("Unregister first");
            } else if (response.equals(Messages.CODE9997.getValue())) {
                logger.info("register with different ip and port");
            } else if (response.equals(Messages.CODE9996.getValue())) {
                logger.info("cant register BS full");
            }

            logger.info("JoinBS response: {}", () -> response);
            final String regResponse = util.processRegisterResponse(response, connectedNodes);
        } catch (SocketException e) {
            logger.error("SocketException", e);
        }
    }

    private void outgoingRequestToPairUp() {
        //call this when using hashes
        // nodes.forEach(this::forwardFileNames);
        //assign file list for each node
      /*if (!connectedNodes.isEmpty()) {
          util.selectFilesForNode(fileNames, connectedNodes);
          connectedNodes.forEach(this::forwardFileNames);
      }*/
        connectedNodes.forEach(this::outgoingRequestToPairUp);
    }

    private void outgoingRequestToPairUp(final Node node) {
//    send pair up request to other nodes
//    need to send hash of file names

        try (final DatagramSocket socket = new DatagramSocket()) {
            final StringBuilder files = new StringBuilder();
            fileNames.forEach(file -> {
                if (files.length() != 0) {
                    files.append(",");//TODO need to change this delim
                }
                files.append(file);
            });

            final byte[] buf = util.generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
                    Integer.toString(currentNode.getPort()), Integer.toString(fileNames.size()), files.toString()).getBytes();

            final String response = util.sendMessage(buf, node.getIp(), socket, node.getPort(), Util.DEFAULT_TIMEOUT);

            logger.info("outgoingRequestToPairUp response: {}", () -> response);
        } catch (SocketException e) {
            logger.error("SocketException", e);
        }
    }

    private void forwardFileNames(final Node node) {
//    send pair up request to other nodes
//    need to send hash of file names
        try (final DatagramSocket socket = new DatagramSocket()) {

            final int fileSize = node.getFieList().toString().split(",").length;
            final byte[] buf = util.generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
                    Integer.toString(currentNode.getPort()), Integer.toString(fileSize),
                    node.getFieList().toString()).getBytes();

            final String response = util.sendMessage(buf, node.getIp(), socket, node.getPort(), Util.DEFAULT_TIMEOUT);

            logger.info("outgoingRequestToPairUp response: {}", () -> response);

        } catch (SocketException e) {
            logger.error("SocketException", e);
        }
    }

    private String processJoinRequest(final StringTokenizer st) {

        final String ip = st.nextToken();
        final String port = st.nextToken();
        final List<String> fileNames = util.extractFileNames(st);
        final boolean response = incomingRequestToPairUp(ip, port, fileNames);

        logger.info("response: {}", () -> response);

        final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
        return util.generateMessage(Messages.JOINOK.getValue(), code.getValue());
    }

    List<Node> getConnectedNodes() {

        return connectedNodes;
    }

    Node getCurrentNode() {

        return currentNode;
    }
}
