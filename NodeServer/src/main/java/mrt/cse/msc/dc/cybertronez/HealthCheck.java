package mrt.cse.msc.dc.cybertronez;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

class HealthCheck implements Runnable {

  private Node currentNode;
  private List<Node> connectedNodes;
  private Logger logger;

  HealthCheck(Client client) {

    currentNode = client.getCurrentNode();
    connectedNodes = client.getConnectedNodes();
    logger = LogManager.getLogger(HealthCheck.class.getName() + " - " + currentNode.toString());
  }

    public void run() {

    logger.info("Heartbeat initiated");
    while (true) {
        for (Node node : connectedNodes) {
            String ip = node.getIp();
            int port = node.getPort();
            logger.debug("Pinging {}:{}", ip, port);
            try {
                InetAddress address = InetAddress.getByName(ip);
                boolean reachable = address.isReachable(1000);
                if (!reachable) {
                    logger.error("{}:{} is not reachable!", ip, port);
                    unregisterNode(node);
                    connectWithNewNode();
                }
                logger.debug("{}:{} is reachable!", ip, port);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        if (Thread.interrupted()) {
            break;
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
  }

  private void unregisterNode(Node node) {
    logger.info("Unregistering {}:{} since it is not reachable!", node.getIp(), node.getPort());
    Util util = new Util();
    String unregisterMessage = util.generateMessage(Messages.UNREG.getValue(), node.getIp(),
            Integer.toString(node.getPort()), node.getUsername());
    try (DatagramSocket socket = new DatagramSocket()) {
        String unRegResponse = util.sendMessage(unregisterMessage.getBytes(), node.getIp(), socket, node.getPort());
        if (unRegResponse.equals("9999")) {
            logger.error("Error in the unregister message!");
        }
        } catch (SocketException e) {
            logger.error(e);
        }
    }

    private void connectWithNewNode() {

        // TODO: Current node should connect with a new node, since a node is unreachable.
    }
}
