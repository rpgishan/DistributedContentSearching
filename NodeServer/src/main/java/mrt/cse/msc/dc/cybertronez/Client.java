package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client
{
  private static Logger logger = null;
  private Node bsServer = new BootstrapNode();
  private Node currentNode;
  private List<Node> connectedNodes = new ArrayList<>();
  private Set<Query> alreadySearchedQueries = new HashSet<>();
  private Map<String, Set<Node>> routingTable = new HashMap<>();
  private Set<String> fileNames = new HashSet<>();
  private Util util;

  public Client(final String ip, final String port, final String username)
  {
    currentNode = new Node(ip, port, username);
    logger = LogManager.getLogger(Client.class.getName() + " - " + currentNode.toString());
    util=new Util();
    populateFiles();
    openSocket();
    join();
    Thread t1 = new Thread(new HealthCheck(this));
    t1.start();
  }

  private void openSocket()
  {
    logger.info("Opening Socket");
    new Thread(() -> {
      try (DatagramSocket socket = new DatagramSocket(currentNode.getPort()))
      {
        byte[] requestBuffer;
        byte[] responseBuffer;
        boolean running = true;
        String received;
        DatagramPacket packet;
        InetAddress sendersAddress;
        int sendersPort;

        while (running)
        {
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
          if (received.equals("end"))
          {
            response = "Socket ended";
            logger.info("response: {}", () -> response);
            running = false;
          }
          else
          {
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
      }
      catch (IOException e)
      {
        logger.error("Exception", e);
      }
    }).start();
  }

  private String processSocketMessage(final String message)
  {
    logger.info("processSocketMessage message: {}", () -> message);

    final StringTokenizer st = new StringTokenizer(message, " ");
    final String length = st.nextToken();
    final String command = st.nextToken();

    if (command.equals(Messages.JOIN.getValue()))
    {
        return processJoinRequest(st);
      /*final String ip = st.nextToken();
      final String port = st.nextToken();
      final int noOfFiles = Integer.parseInt(st.nextToken());
      final List<String> fileNames = new ArrayList<>(noOfFiles);
      StringBuilder sb;
      for (int i = 0; i < noOfFiles; i++)
      {
        String fileNameSeg = st.nextToken();
        sb = new StringBuilder();
        while (!fileNameSeg.equals(","))
        {
          if (sb.length() != 0)
          {
            sb.append(" ");
          }
          sb.append(fileNameSeg);
          if (st.hasMoreElements())
          {
            fileNameSeg = st.nextToken();
            continue;
          }
          break;
        }
        fileNames.add(sb.toString());
      }

      final boolean response = incomingRequestToPairUp(ip, port, fileNames);

      logger.info("response: {}", () -> response);

      final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
      return util.generateMessage(Messages.JOINOK.getValue(), code.getValue());*/ //TODO handle errors
    }
    else if (command.equals(Messages.LEAVE.getValue()))
    {
      final String ip = st.nextToken();
      final String port = st.nextToken();
      final Node node = new Node(ip, port);
      Optional<Node> first = connectedNodes.stream().filter(node::equals).findFirst();
      final boolean response = first.isPresent() ? connectedNodes.remove(first.get()) : false;

      logger.info("response: {}", () -> response);

      final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
      return util.generateMessage(Messages.LEAVEOK.getValue(), code.getValue()); //TODO handle errors
    }
    else if (command.equals(Messages.SER.getValue()))
    {//TODO
      final String ip = st.nextToken();
      final String port = st.nextToken();
      int hops = Integer.parseInt(st.nextToken());
      hops++;

      int noOfSearchedNodes = Integer.parseInt(st.nextToken());
      Set<Node> propagatedNodes = new HashSet<>();
      for (int i = 0; i < noOfSearchedNodes; i++)
      {
        String nodeIp = st.nextToken();
        String nodePort = st.nextToken();
        Node node = new Node(nodeIp, nodePort);
        propagatedNodes.add(node);
      }

      StringBuilder fileNameBuilder = new StringBuilder();
      while (st.hasMoreElements())
      {
        if (fileNameBuilder.length() != 0)
        {
          fileNameBuilder.append(" ");
        }
        fileNameBuilder.append(st.nextToken());
      }

      return search(new Query(fileNameBuilder.toString(), new Node(ip, port)), hops, propagatedNodes);
//      return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE9999.getValue()); //TODO handle search and errors
    }
    else if (command.equals(Messages.DETAILS.getValue()))
    {
      StringBuilder sb = new StringBuilder();
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
    else
    {
      return util.generateMessage(Messages.ERROR.toString());
    }

  }

  private void populateFiles()
  {
    final Random r = new Random();
    final int minFiles = 3;
    final int maxFiles = 5;
    final int noOfFiles = minFiles + r.nextInt(maxFiles - minFiles + 1);
    while (fileNames.size() < noOfFiles)
    {
      final String fileName = FileNamesAndQueries.FILE_NAMES.get(r.nextInt(FileNamesAndQueries.FILE_NAMES.size()));
      fileNames.add(fileName);
    }
    fileNames.forEach(name -> addDataToRoutingTable(name, currentNode));
    if (logger.isInfoEnabled())
    {
      StringBuilder fileSb = new StringBuilder();
      fileNames.forEach(s -> fileSb.append(s).append(", "));
      logger.info("populateFiles fileNames: {}", fileSb::toString);
    }
  }

  private String search(final Query query, int hops, Set<Node> propagatedNodes)
  {
    logger.info("Begin search of \"{}\" in {}. Current hops: {}", () -> query, currentNode::toString, () -> hops);
    if (alreadySearchedQueries.contains(query))
    {
      logger.info("Already searched of \"{}\" in {}. Current hops: {}", () -> query, currentNode::toString, () -> hops);
      StringBuilder propagatedNodesBuilder = new StringBuilder(Integer.toString(propagatedNodes.size()));
      propagatedNodes.forEach(node -> {
        propagatedNodesBuilder.append(" ").append(node.getIp()).append(" ").append(node.getPort());
      });
      return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE9998.getValue(),
          Messages.ALREADY_SEARCHED.getValue(), Integer.toString(hops), propagatedNodesBuilder.toString());
    }
    alreadySearchedQueries.add(query);
    //TODO perform search

    String[] queryWords = query.getQuery().split(" ");
    List<String> foundFiles = new ArrayList<>();
    fileNames.forEach(fileName -> {
      String[] fileNameWords = fileName.split(" ");
      if (fileNameWords.length >= queryWords.length)
      {
        boolean valid = true;
        int j = 0;
        for (int i = 0; i < queryWords.length && valid; i++)
        {
          valid = false;
          while (j < fileNameWords.length)
          {
            if (queryWords[i].equalsIgnoreCase(fileNameWords[j]))
            {
              valid = true;
              break;
            }
            j++;
          }
        }
        if (valid)
        {
          foundFiles.add(fileName);
          logger.info("Found file \"{}\" for the query of \"{}\" in {}.", () -> fileName, () -> query,
              currentNode::toString);
        }
      }
    });

    if (foundFiles.isEmpty())
    {
      logger.info("No matching files found in {}.", currentNode::toString);
      propagatedNodes.add(currentNode);
      return propagateSearch(query, hops, propagatedNodes);
    }

    List<String> foundFileNames = foundFiles.stream().map(fileName -> fileName.replace(' ', '_'))
        .collect(Collectors.toList());
    String fileNameStrings = convertFileNameListToString(foundFileNames);

    return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE0.getValue(), Integer.toString(hops),
        getCollectionNodesToString(propagatedNodes), fileNameStrings);
    //TODO handle errors
  }

  private String convertFileNameListToString(List<String> foundFileNames)
  {
    StringBuilder sb = new StringBuilder(Integer.toString(foundFileNames.size()));
    foundFileNames.forEach(name -> {
      if (sb.length() != 0)
      {
        sb.append(" ");
      }
      sb.append(name);
    });
    return sb.toString();
  }

  private String propagateSearch(Query query, int hops, Set<Node> propagatedNodes)
  {
    //TODO propagate search to child nodes
    try (DatagramSocket socket = new DatagramSocket())
    {
      for (Node node : connectedNodes)
      {
        int finalHops = hops;
        logger.info("Propagate search of \"{}\" to {}. Current hops: {}", () -> query, node::toString, () -> finalHops);

        if (propagatedNodes.contains(node))
        {
          continue;
        }

        String propagatedNodesString = getCollectionNodesToString(propagatedNodes);

        byte[] buffer = util.generateMessage(Messages.SER.getValue(), query.getInitiatedNode().getIp(),
            Integer.toString(query.getInitiatedNode().getPort()), Integer.toString(hops),
            propagatedNodesString, query.getQuery()).getBytes();
        String response = util.sendMessage(buffer, node.getIp(), socket, node.getPort());
        StringTokenizer st = new StringTokenizer(response, " ");
        String length = st.nextToken();
        String command = st.nextToken();
        if (command.equals(Messages.SEROK.getValue()))
        {
          String code = st.nextToken();
          if (code.equals(Messages.CODE0.getValue()) || code.equals(Messages.ERROR.getValue()))
          {
            return response;
          }
          else if (code.equals(Messages.CODE9998.getValue()))
          {
            String error = st.nextToken();
            int newHops = Integer.parseInt(st.nextToken());
            hops = newHops;

            int noOfNewPropagatedNodes = Integer.parseInt(st.nextToken());

            for (int i = 0; i < noOfNewPropagatedNodes; i++)
            {
              String ip = st.nextToken();
              String port = st.nextToken();
              propagatedNodes.add(new Node(ip,port));
            }

            logger.info("Got search response error of \"{}\" - \"{}\" for \"{}\" from {}. Current hops: {}", () -> code,
                () -> error, () -> query, node::toString, () -> newHops);
            continue;
          }
        }
      }
    }
    catch (SocketException e)
    {
      logger.error("SocketException", e);
    }
    int finalHops1 = hops;
    logger.info("File not found for \"{}\" in {}. Current hops: {}", () -> query, currentNode::toString, () -> finalHops1);

    String propagatedNodesString = getCollectionNodesToString(propagatedNodes);

    return util.generateMessage(Messages.SEROK.getValue(), Messages.CODE9998.getValue(), Messages.NOT_FOUND.getValue(),
        Integer.toString(hops),propagatedNodesString);
  }

  private String getCollectionNodesToString(Collection<Node> nodeCollection)
  {
    StringBuilder propagatedNodesBuilder = new StringBuilder(Integer.toString(nodeCollection.size()));
    for (Node node : nodeCollection)
    {
      propagatedNodesBuilder.append(" ").append(node.getIp()).append(" ").append(node.getPort());
    }
    return propagatedNodesBuilder.toString();
  }

  private void addDataToRoutingTable(String fileName, Node node)
  {
    if (!routingTable.containsKey(fileName))
    {
      routingTable.put(fileName, new HashSet<>());
    }
    routingTable.get(fileName).add(node);
  }

  private boolean incomingRequestToPairUp(final String ip, final String port, final List<String> fileNames)
  {
    final Node node = new Node(ip, port);
    return incomingRequestToPairUp(node, fileNames);
  }

  private boolean incomingRequestToPairUp(final Node node, final List<String> fileNames)
  {
    logger.info("Incoming Request To Pair Up from {} with {} files.", node::toString,
        () -> Integer.toString(fileNames.size()));
    final boolean add = connectedNodes.add(node);
    fileNames.forEach(fileName -> addDataToRoutingTable(fileName, node));
    return add;//TODO handle errors
  }

  private void join()
  {
    new Thread(() -> {
      joinBS();
      outgoingRequestToPairUp();
    }).start();
  }

  private void joinBS()
  {
    //TODO Sachini
    try (DatagramSocket socket = new DatagramSocket())
    {
      final int port = bsServer.getPort();
      //unreg first then reg
      String unregisterMessage = util.generateMessage(Messages.UNREG.getValue(), currentNode.getIp(),
          Integer.toString(currentNode.getPort()), currentNode.getUsername());
      String unRegResponse = util.sendMessage(unregisterMessage.getBytes(), bsServer.getIp(), socket, port);

    if (unRegResponse.equals("9999"))
    {
      logger.info("Error in unreg message");
    }

    String joinMessage = util.generateMessage(Messages.REG.getValue(), currentNode.getIp(),
        Integer.toString(currentNode.getPort()), currentNode.getUsername());

    //send register  request to bs
    String response = util.sendMessage(joinMessage.getBytes(), bsServer.getIp(), socket, port);

    if (response.equals("9999"))
    {
      logger.info("Error in reg message");
    }
    else if (response.equals("9998"))
    {
      logger.info("Unregister first");
    }
    else if (response.equals("9997"))
    {
      logger.info("register with different ip and port");
    }
    else if (response.equals("9996"))
    {
      logger.info("cant register BS full");
    }

      logger.info("JoinBS response: {}", () -> response);
      String regResponse = util.processRegisterResponse(response, connectedNodes);
    }
    catch (SocketException e)
    {
      logger.error("SocketException", e);
    }
  }

  private void outgoingRequestToPairUp()
  {
      //call this when using hashes
      // nodes.forEach(this::forwardFileNames);
      //assign file list for each node
      /*if (!connectedNodes.isEmpty()) {
          util.selectFilesForNode(fileNames, connectedNodes);
          connectedNodes.forEach(this::forwardFileNames);
      }*/
      connectedNodes.forEach(this::outgoingRequestToPairUp);
  }

  private void outgoingRequestToPairUp(final Node node)
  {
//    send pair up request to other nodes
//    need to send hash of file names

    try (DatagramSocket socket = new DatagramSocket())
    {
      final StringBuilder files = new StringBuilder();
      fileNames.forEach(file -> {
        if (files.length() != 0)
        {
          files.append(" , ");//TODO need to change this delim
        }
        files.append(file);
      });

    final byte[] buf = util.generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
        Integer.toString(currentNode.getPort()), Integer.toString(fileNames.size()), files.toString()).getBytes();

    String response = util.sendMessage(buf, node.getIp(), socket, node.getPort());

      logger.info("outgoingRequestToPairUp response: {}", () -> response);
    }
    catch (SocketException e)
    {
      logger.error("SocketException", e);
    }
  }

    private void forwardFileNames(Node node) {
//    send pair up request to other nodes
//    need to send hash of file names
        try (DatagramSocket socket = new DatagramSocket()) {

            int fileSize = node.getFieList().toString().split(",").length;
            final byte[] buf = util.generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
                    Integer.toString(currentNode.getPort()), Integer.toString(fileSize),
                    node.getFieList().toString()).getBytes();

            String response = util.sendMessage(buf, node.getIp(), socket, node.getPort());

            logger.info("outgoingRequestToPairUp response: {}", () -> response);

        } catch (SocketException e) {
            logger.error("SocketException", e);
        }
    }

    private String processJoinRequest(StringTokenizer st) {

        final String ip = st.nextToken();
        final String port = st.nextToken();
        final List<String> fileNames = util.extractFileNames(st);
        final boolean response = incomingRequestToPairUp(ip, port, fileNames);

        logger.info("response: {}", () -> response);

        final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
        return util.generateMessage(Messages.JOINOK.getValue(), code.getValue());
    }

  public static void main(final String[] args)
  {
    if (args.length == 3)
    {
      final String ip = args[0];
      final String port = args[1];
      final String username = args[2];

      final Client client = new Client(ip, port, username);

    }
  }

  List<Node> getConnectedNodes() {

    return connectedNodes;
  }

  Node getCurrentNode() {

    return currentNode;
  }
}
