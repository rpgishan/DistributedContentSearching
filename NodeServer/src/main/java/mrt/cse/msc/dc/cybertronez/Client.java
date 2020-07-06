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


    public Client(final String ip, final int port, final String username) throws SocketException {
    currentNode = new Node(ip, port, username);
    socket = new DatagramSocket();

    populateFiles();
    join();
    openSocket();
  }


  private void openSocket()
  {
    new Thread(() -> {
      try
      {
        DatagramSocket socket = new DatagramSocket(currentNode.getPort());

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
            System.out.println(response);
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
        socket.close();
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
    LOGGER.info(processSocketMessage, "message", message);

    final StringTokenizer st = new StringTokenizer(message, " ");
    final String length = st.nextToken();
    final String command = st.nextToken();

    LOGGER.info(processSocketMessage, "length", length);
    LOGGER.info(processSocketMessage, "command", command);

    if (command.equals(Messages.JOIN.getValue()))
    {
      final String ip = st.nextToken();
      final String port = st.nextToken();
      final int noOfFiles = Integer.parseInt(st.nextToken());
      final List<String> fileNames = new ArrayList<>(noOfFiles);

      for (int i = 0; i < noOfFiles; i++)
      {
        fileNames.add(st.nextToken());
      }

      LOGGER.info(processSocketMessage, "ip", ip);
      LOGGER.info(processSocketMessage, "port", port);
      LOGGER.info(processSocketMessage, "noOfFiles", noOfFiles);
      LOGGER.info(processSocketMessage, "fileNames", fileNames);

      final boolean response = incomingRequestToPairUp(ip, port, fileNames);
      final Messages code = response ? Messages.CODE0 : Messages.CODE9999;
      return generateMessage(Messages.JOINOK.getValue(), code.getValue()); //TODO handle errors
    }
    else if (command.equals(Messages.LEAVE.getValue()))
    {
      final String ip = st.nextToken();
      final String port = st.nextToken();
      final Node node = new Node(ip, port);
      final boolean response = connectedNodes.remove(connectedNodes.stream().filter(node::equals).findFirst().get());

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
      final List<Node> nodes = new ArrayList<>();
      for (int i = 0; i < Integer.parseInt(noOfNodes); i++)
      {
        final String ip = st.nextToken();
        final String port = st.nextToken();
        nodes.add(new Node(ip, port));
      }

      connectedNodes.addAll(nodes);
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
    LOGGER.info("populateFiles", "fileNames", fileNames);
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
    fileNames.forEach(hash -> routingTable.put(hash, node));
    return add;//TODO handle errors
  }

  private void join()
  {
    joinBS();
    connectedNodes.forEach(this::outgoingRequestToPairUp);
  }

  private void joinBS()
  {
    //TODO Sachini
    final int port = bsServer.getPort();
    String joinMessage = "";
    //send join request to bs
    String response = Util.sendMessage(joinMessage,bsServer.getIp(), socket, port);

    final ArrayList<Node> nodesToBeConnected = new ArrayList<>();//receive list of nodes to connect
    connectedNodes.addAll(nodesToBeConnected);
    LOGGER.info("JoinBS");
  }

  private void outgoingRequestToPairUp(final Node node)
  {
    //send pair up request to other nodes
//    need to send hash of file names
    try
    {
      final DatagramSocket socket = new DatagramSocket();

      final InetAddress address = InetAddress.getByName(node.getIp());

      final StringBuilder files = new StringBuilder();
      fileNames.forEach(file -> {
        if (files.length() != 0)
        {
          files.append(" ");
        }
        files.append(file);
      });

      final byte[] buf = generateMessage(Messages.JOIN.getValue(), currentNode.getIp(),
          Integer.toString(currentNode.getPort()), Integer.toString(fileNames.size()), files.toString()).getBytes();
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, node.getPort());
      socket.send(packet);
      packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      final String received = new String(packet.getData(), 0, packet.getLength());
      LOGGER.info("outgoingRequestToPairUp", received);
    }
    catch (IOException e)
    {
      LOGGER.error("Exception", e);
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
    final String lenght = String.format("%04d", sb.length());
    sb.replace(0, 4, lenght);

    return sb.toString();
  }

  public static void main(final String[] args)
  {
    if (args.length == 3)
    {
      final String ip = args[0];
      final int port = Integer.parseInt(args[1]);
      final String username = args[2];
        try {
            final Client client = new Client(ip, port, username);
        } catch (SocketException e) {
            LOGGER.error("Error while creating socket instance. ", e);
        }
    }
  }
}
