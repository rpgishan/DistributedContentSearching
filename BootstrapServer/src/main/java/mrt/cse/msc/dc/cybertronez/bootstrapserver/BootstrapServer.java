package mrt.cse.msc.dc.cybertronez.bootstrapserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import mrt.cse.msc.dc.cybertronez.BootstrapNode;
import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Node;

public class BootstrapServer
{
  public static void main(final String[] args)
  {
    final Node bsNode = new BootstrapNode();
    final DatagramSocket sock;
    String s;
    final List<Node> nodes = new ArrayList<>();

    try
    {
      sock = new DatagramSocket(bsNode.getPort());

      echo("Bootstrap Server created at " + bsNode.getPort() + ". Waiting for incoming data...");

      while (true)
      {
        final byte[] buffer = new byte[65536];
        final DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        sock.receive(incoming);

        final byte[] data = incoming.getData();
        s = new String(data, 0, incoming.getLength());

        //echo the details of incoming data - client ip : client port - client message
        echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

        final StringTokenizer st = new StringTokenizer(s, " ");

        final String length = st.nextToken();
        final String command = st.nextToken();

        if (command.equals(Messages.REG.toString()))
        {
          String reply = Messages.REGOK.toString();

          final String ip = st.nextToken();
          final int port = Integer.parseInt(st.nextToken());
          final String username = st.nextToken();
          if (nodes.isEmpty())
          {
            reply += " 0";
            nodes.add(new Node(ip, port, username));
          }
          else
          {
            boolean isOkay = true;
            for (final Node node : nodes)
            {
              if (node.getPort() == port)
              {
                if (node.getUsername().equals(username))
                {
                  reply += "9998";
                }
                else
                {
                  reply += "9997";
                }
                isOkay = false;
              }
            }
            if (isOkay)
            {
              if (nodes.size() == 1)
              {
                reply += " 1 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort();
              }
              else if (nodes.size() == 2)
              {
                reply += " 2 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " +
                    nodes.get(1).getPort();
              }
              else
              {
                final Random r = new Random();
                final int Low = 0;
                final int High = nodes.size();
                final int random_1 = r.nextInt(High - Low) + Low;
                int random_2 = r.nextInt(High - Low) + Low;
                while (random_1 == random_2)
                {
                  random_2 = r.nextInt(High - Low) + Low;
                }
                echo(random_1 + " " + random_2);
                reply += " 2 " + nodes.get(random_1).getIp() + " " + nodes.get(random_1).getPort() + " " +
                    nodes.get(random_2).getIp() + " " + nodes.get(random_2).getPort();
              }
              nodes.add(new Node(ip, port, username));
            }
          }

          reply = String.format("%04d", reply.length() + 5) + " " + reply;

          final DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
              incoming.getAddress(), incoming.getPort());
          sock.send(dpReply);
        }
        else if (command.equals(Messages.UNREG))
        {
          final String ip = st.nextToken();
          final int port = Integer.parseInt(st.nextToken());
          final String username = st.nextToken();
          for (int i = 0; i < nodes.size(); i++)
          {
            if (nodes.get(i).getPort() == port)
            {
              nodes.remove(i);
              final String reply = "0012 UNROK 0";
              final DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                  incoming.getAddress(), incoming.getPort());
              sock.send(dpReply);
            }
          }
        }
        else if (command.equals(Messages.ECHO))
        {
          for (final Node node : nodes)
          {
            echo(node.getIp() + " " + node.getPort() + " " + node.getUsername());
          }
          final String reply = "0012 ECHOK 0";
          final DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
              incoming.getAddress(), incoming.getPort());
          sock.send(dpReply);
        }

      }
    }

    catch (IOException e)
    {
      System.err.println("IOException " + e);
    }
  }

  //simple function to echo data to terminal
  public static void echo(final String msg)
  {
    System.out.println(msg);
  }
}
