package mrt.cse.msc.dc.cybertronez;

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
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client
{
  private static final Logger LOGGER = LogManager.getLogger(Client.class);
  private Node bsServer = new BootstrapNode();
  private Node currentNode;
  private List<Node> connectedNodes = new ArrayList<>();
  private List<Query> alreadySearchedQueries = new ArrayList<>();
  private Map<String, Node> routingTable = new HashMap<>();
  private Set<String> fileNames = new HashSet<>();
  DatagramSocket socket = null;

  public Client(final String ip, final String port, final String username) throws SocketException
  {
    currentNode = new Node(ip, port, username);
    socket = new DatagramSocket();

    populateFiles();
    join();
    openSocket();
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
            LOGGER.info("response: " + response);
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
        LOGGER.error("Exception", e);
      }
    }).start();
  }

  private String processSocketMessage(final String message)
  {
    final String processSocketMessage = "processSocketMessage";
    LOGGER.info(processSocketMessage + " message: " + message);

    final StringTokenizer st = new StringTokenizer(message, " ");
    final String length = st.nextToken();
    final String command = st.nextToken();

    if (command.equals(Messages.JOIN.getValue()))
    {
      final String ip = st.nextToken();
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

      LOGGER.info("response: " + response);

      final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
      return generateMessage(Messages.JOINOK.getValue(), code.getValue()); //TODO handle errors
    }
    else if (command.equals(Messages.LEAVE.getValue()))
    {
      final String ip = st.nextToken();
      final String port = st.nextToken();
      final Node node = new Node(ip, port);
      final boolean response = connectedNodes.remove(connectedNodes.stream().filter(node::equals).findFirst().get());

      LOGGER.info("response: " + response);

      final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
      return generateMessage(Messages.LEAVEOK.getValue(), code.getValue()); //TODO handle errors
    }
    else if (command.equals(Messages.SER.getValue()))
    {//TODO
      final String ip = st.nextToken();
      final String port = st.nextToken();
      final String fileName = st.nextToken();
      final String hops = st.nextToken();
      return generateMessage(Messages.SEROK.getValue(), Messages.CODE9999.getValue()); //TODO handle search and errors
    }
    else if (command.equals("ADD_NODES")) // only for testing
    {//TODO remove this else if
      final String noOfNodes = st.nextToken();
      for (int i = 0; i < Integer.parseInt(noOfNodes); i++)
      {
        final String ip = st.nextToken();
        final String port = st.nextToken();
        Node node = new Node(ip, port);
        connectedNodes.add(node);
        LOGGER.info("ADD_NODES Node: " + node);
      }

      LOGGER.info("ADD_NODES connectedNodes length: " + connectedNodes.size());

      join();
      return generateMessage("ADD_NODES_OK", Messages.CODE9999.getValue()); //TODO handle search and errors
    }
    else
    {
      return generateMessage(Messages.ERROR.toString());
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
    fileNames.forEach(name -> routingTable.put(name, currentNode));
    if (LOGGER.isInfoEnabled())
    {
      StringBuilder fileSb = new StringBuilder();
      fileNames.forEach(s -> fileSb.append(s).append(", "));
      LOGGER.info("populateFiles fileNames: " + fileSb.toString());
    }
  }

  public void search(final Query query)
  {
    alreadySearchedQueries.add(query);
    //TODO perform search
  }

  private boolean incomingRequestToPairUp(final String ip, final String port, final List<String> fileNames)
  {
    final Node node = new Node(ip, port);
    return incomingRequestToPairUp(node, fileNames);
  }

  private boolean incomingRequestToPairUp(final Node node, final List<String> fileNames)
  {
    final boolean add = connectedNodes.add(node);
    fileNames.forEach(fileName -> routingTable.put(fileName, node));
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
    //TODO Sachini
    //unreg first then reg
    final int port = bsServer.getPort();
    String regMessage = generateMessage(Messages.REG.getValue(), currentNode.getIp(),
        Integer.toString(currentNode.getPort()), currentNode.getUsername());
    //send join request to bs

//    final ArrayList<Node> nodesToBeConnected = new ArrayList<>();//receive list of nodes to connect
    String response = Util.sendMessage(regMessage.getBytes(), bsServer.getIp(), socket, port);


    Util.processRegisterResponse(response, connectedNodes);
    LOGGER.info("JoinBS response: " + response);

    StringTokenizer st = new StringTokenizer(response, " ");
    String length = st.nextToken();
    String command = st.nextToken();
    int noOfNodes = Integer.parseInt(st.nextToken());
    for (int i = 0; i < noOfNodes; i++)
    {
      String ip = st.nextToken();
      String porttt = st.nextToken();
      connectedNodes.add(new Node(ip, porttt));
    }

    if (LOGGER.isInfoEnabled())
    {
      StringBuilder stringBuilder = new StringBuilder();
      connectedNodes.forEach(s -> stringBuilder.append(s).append(" "));
      LOGGER.info("joinBS connectedNodes: " + stringBuilder.toString());
    }

    LOGGER.info("JoinBS");
  }

  private void outgoingRequestToPairUp(final List<Node> nodes)
  {
    nodes.forEach(this::outgoingRequestToPairUp);
  }

  private void outgoingRequestToPairUp(final Node node)
  {
//    send pair up request to other nodes
//    need to send hash of file names

    final StringBuilder files = new StringBuilder();
    fileNames.forEach(file -> {
      if (files.length() != 0)
      {
        files.append(" , ");//TODO need to change this delim
      }
      files.append(file);
    });

    final byte[] buf = generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
        Integer.toString(currentNode.getPort()), Integer.toString(fileNames.size()), files.toString()).getBytes();

    String response = Util.sendMessage(buf,node.getIp(),socket, node.getPort());

    LOGGER.info("outgoingRequestToPairUp response: " + response);

  }

  private String sendSocketMessage(String ip, int port, byte[] buffer)
  {
    try (final DatagramSocket socket = new DatagramSocket())
    {
      socket.setSoTimeout(60000);
      final InetAddress address = InetAddress.getByName(ip);
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
      socket.send(packet);
      packet = new DatagramPacket(buffer, buffer.length);
      socket.receive(packet);//TODO handle timeout
      final String received = new String(packet.getData(), 0, packet.getLength());
      LOGGER.info("sendSocketMessage received: " + received);
      return received;
    }
    catch (IOException e)
    {
      LOGGER.error("Exception", e);
      return "";
    }
  }

  private String generateMessage(final String... args)
  {
    final StringBuilder sb = new StringBuilder("####");

    for (final String word : args)
    {
      if (sb.length() != 0)
      {
        sb.append(" ");
      }
      sb.append(word);
    }
    final String length = String.format("%04d", sb.length());
    sb.replace(0, 4, length);

    if (LOGGER.isInfoEnabled())
    {
      LOGGER.info("generateMessage: " + sb.toString());
    }

    return sb.toString();
  }

  public static void main(final String[] args)
  {
    if (args.length == 3)
    {
      final String ip = args[0];
      final String port = args[1];
      final String username = args[2];
      try
      {
        final Client client = new Client(ip, port, username);
      }
      catch (SocketException e)
      {
        LOGGER.error("Error while creating socket instance.", e);
      }
    }
  }
}
