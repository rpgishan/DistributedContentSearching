package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

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

  public Client(final String ip, final String port, final String username)
  {
    currentNode = new Node(ip, port, username);
    logger = LogManager.getLogger(Client.class.getName() + " - " + currentNode.toString());

      populateFiles();
      openSocket();
      join();
  }

  private void openSocket()
  {
    new Thread(() -> {
      try (DatagramSocket socket = new DatagramSocket(currentNode.getPort()))
      {
        byte[] requestBuffer = new byte[65536];
        byte[] responseBuffer;
        boolean running = true;

        while (running)
        {
          DatagramPacket packet = new DatagramPacket(requestBuffer, requestBuffer.length);
          socket.receive(packet);
          String received = new String(packet.getData(), packet.getOffset(), packet.getLength());

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

          responseBuffer = response.getBytes();
          InetAddress address = packet.getAddress();
          int port = packet.getPort();
          packet = new DatagramPacket(responseBuffer, 0, responseBuffer.length, address, port);
          socket.send(packet);

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
    final String processSocketMessage = "processSocketMessage";
    logger.info(processSocketMessage + " message: {}", () -> message);

    final StringTokenizer st = new StringTokenizer(message, " ");
    final String length = st.nextToken();
    final String command = st.nextToken();

    if (command.equals(Messages.JOIN.getValue()))
    {
      return procesJoinRequest(st); //TODO handle errors
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
      return Util.generateMessage(Messages.LEAVEOK.getValue(), code.getValue()); //TODO handle errors
    }
    else if (command.equals(Messages.SER.getValue()))
    {//TODO
      final String ip = st.nextToken();
      final String port = st.nextToken();
      final String fileName = st.nextToken();
      final String hops = st.nextToken();
      return Util.generateMessage(Messages.SEROK.getValue(), Messages.CODE9999.getValue()); //TODO handle search and errors
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
      return Util.generateMessage(Messages.DETAILS.getValue(), sb.toString());
    }
    else
    {
      return Util.generateMessage(Messages.ERROR.toString());
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

  public void search(final Query query)
  {
    alreadySearchedQueries.add(query);
    //TODO perform search
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
      outgoingRequestToPairUp(connectedNodes);
    }).start();
  }

  private void joinBS()
  {
    try (DatagramSocket socket = new DatagramSocket())
    {
      final int port = bsServer.getPort();
      //unreg first then reg
      String unregisterMessage = Util.generateMessage(Messages.UNREG.getValue(), currentNode.getIp(),
          Integer.toString(currentNode.getPort()), currentNode.getUsername());
      String unRegResponse = Util.sendMessage(unregisterMessage.getBytes(), bsServer.getIp(), socket, port);

    if (unRegResponse.equals("9999"))
    {
      logger.info("Error in unreg message");
    }

    String joinMessage = Util.generateMessage(Messages.REG.getValue(), currentNode.getIp(),
        Integer.toString(currentNode.getPort()), currentNode.getUsername());

    //send register  request to bs
    String response = Util.sendMessage(joinMessage.getBytes(), bsServer.getIp(), socket, port);

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
      String regResponse = Util.processRegisterResponse(response, connectedNodes);
    }
    catch (SocketException e)
    {
      logger.error("SocketException", e);
    }
  }

  private void outgoingRequestToPairUp(final List<Node> nodes)
  {
      //call this when using hashes
      // nodes.forEach(this::forwardFileNames);
    nodes.forEach(this::outgoingRequestToPairUp);
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

      //select the node to send each file

    final byte[] buf = Util.generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
        Integer.toString(currentNode.getPort()), Integer.toString(fileNames.size()), files.toString()).getBytes();

    String response = Util.sendMessage(buf, node.getIp(), socket, node.getPort());

      logger.info("outgoingRequestToPairUp response: {}", () -> response);
    }
    catch (SocketException e)
    {
      logger.error("SocketException", e);
    }
  }

    private void forwardFileNames() {
//    send pair up request to other nodes
//    need to send hash of file names
        List<Node> nodeListWithHashes = Util.setNodeHashes(connectedNodes);

        try (DatagramSocket socket = new DatagramSocket()) {
            Iterator<String> it = fileNames.iterator();
            while (it.hasNext()) {
                String fileName = it.next();
                //select the node to send each file
                Node nodeToSendFile = Util.selectNode(fileName, nodeListWithHashes);
                final byte[] buf = Util.generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
                        Integer.toString(currentNode.getPort()), Integer.toString(fileNames.size()), fileName).getBytes();
                String response = Util.sendMessage(buf, nodeToSendFile.getIp(), socket, nodeToSendFile.getPort());
                logger.info("outgoingRequestToPairUp response: {}", () -> response);
            }
        } catch (SocketException e) {
            logger.error("SocketException", e);
        }
    }

    private String procesJoinRequest(StringTokenizer st) {

        final String ip = st.nextToken();
        final String port = st.nextToken();
        final List<String> fileNames = Util.extractFileNames(st);
        final boolean response = incomingRequestToPairUp(ip, port, fileNames);

        logger.info("response: {}", () -> response);

        final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
        return Util.generateMessage(Messages.JOINOK.getValue(), code.getValue());
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
}
