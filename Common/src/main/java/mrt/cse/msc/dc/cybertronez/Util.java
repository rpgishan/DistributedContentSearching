package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util
{

  private static final Logger LOGGER = LogManager.getLogger(Util.class);

  public static String sendMessage(String message, String hostName, DatagramSocket socket, int port)
  {

    InetAddress address = null;
    String received = "";
    try
    {
      address = InetAddress.getByName(hostName);

    }
    catch (UnknownHostException e)
    {
      LOGGER.error("Error while retrieving InetAddress ", e);
    }

    byte[] buf = message.getBytes();
    byte[] responseBuffer = new byte[65536];
    if (address != null)
    {
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
      try
      {
        socket.send(packet);
        packet = new DatagramPacket(responseBuffer, responseBuffer.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength());
      }
      catch (IOException e)
      {
        LOGGER.error("Error while sending packer", e);
      }

    }
    return received;

  }

  public static String generateMessage(final String... args)
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

    LOGGER.info("generateMessage: {}", sb::toString);

    return sb.toString();
  }
}
