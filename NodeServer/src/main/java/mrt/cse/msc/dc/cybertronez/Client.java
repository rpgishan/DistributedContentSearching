package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Client
{
  private Node bsServer = new BootstrapNode();
  private Node currentNode;
  private List<Node> incomingConnectedNodes = new ArrayList<>();
  private List<Node> outgoingConnectedNodes = new ArrayList<>();
  private List<Query> alreadySearchedQueries = new ArrayList<>();
  private Map<String, Node> routingTable = new HashMap<>();
  private Set<String> fileNames = new HashSet<>();

  public Client(String ip, int port, String username)
  {
    currentNode = new Node(ip, port, username);
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

        byte[] receiveBuffer = new byte[65536];
        byte[] sendBuffer;

        while (true)
        {
          DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
          socket.receive(packet);
          String received = new String(packet.getData(), packet.getOffset(), packet.getLength());

          if (received.equals("end"))
          {
            System.out.println("Socket ended");
            break;
          }
          processSocketMessage(received);

          String msg = "ACK";
          sendBuffer = msg.getBytes();
          InetAddress address = packet.getAddress();
          int port = packet.getPort();
          packet = new DatagramPacket(sendBuffer, 0, sendBuffer.length, address, port);
          socket.send(packet);

        }
        socket.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }).start();
  }

  private void processSocketMessage(String message)
  {
    System.out.println(message);
    //TODO
  }

  private void populateFiles()
  {
    Random r = new Random();
    int minFiles = 3;
    int maxFiles = 5;
    int noOfFiles = minFiles + r.nextInt(maxFiles - minFiles + 1);
    while (fileNames.size() < noOfFiles)
    {
      String fileName = FileNamesAndQueries.FILE_NAMES.get(r.nextInt(FileNamesAndQueries.FILE_NAMES.size()));
      fileNames.add(fileName);
    }
    fileNames.forEach(name -> routingTable.put(name, currentNode));
  }

  public void search(Query query)
  {
    alreadySearchedQueries.add(query);
    //perform search
  }

  public void incomingRequestToPairUp(Node node, List<String> fileHash)
  {
    incomingConnectedNodes.add(node);
    fileHash.forEach(hash -> routingTable.put(hash, node));
  }

  public void join()
  {
    joinBS();
    outgoingConnectedNodes.forEach(this::outgoingRequestToPairUp);
  }

  private void joinBS()
  {
    //TODO Sachini
    int port = bsServer.getPort();
    //send join request to bs
    ArrayList<Node> nodesToBeConnected = new ArrayList<>();//receive list of nodes to connect
    outgoingConnectedNodes.addAll(nodesToBeConnected);
    System.out.println("JoinBS");
  }

  private void outgoingRequestToPairUp(Node node)
  {
    //send pair up request to other nodes
//    need to send hash of file names
  }

  public static void main(String[] args)
  {
    if (args.length == 3)
    {
      String ip = args[0];
      int port = Integer.parseInt(args[1]);
      String username = args[2];
      Client client = new Client(ip, port, username);
    }
  }
}
