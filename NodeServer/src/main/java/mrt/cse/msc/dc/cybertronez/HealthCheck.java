package mrt.cse.msc.dc.cybertronez;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class HealthCheck implements Runnable {

  private Node currentNode;
  private List<Node> connectedNodes;
  private Logger logger;
  private Util util;

  HealthCheck(Client client) {
    util = new Util();
    currentNode = client.getCurrentNode();
    connectedNodes = client.getConnectedNodes();
    logger = LogManager.getLogger(HealthCheck.class.getName() + " - " + currentNode.toString());
  }

    public void run() {

    logger.info("Heartbeat initiated");
    while (true) {
        int length = connectedNodes.size();
        int i = 0;
        while (i < length) {
          Node node = connectedNodes.get(i);
//        for (Node node : connectedNodes) {
            String ip = node.getIp();
            int port = node.getPort();
            logger.debug("Pinging {}:{}", ip, port);
            try {
                InetAddress address = InetAddress.getByName(ip);
                boolean reachable = address.isReachable(5000);
                if (reachable && util.isSocketAvailable(node)) {
                  i++;
                  logger.debug("{}:{} is reachable!", node::getIp, node::getPort);
                }else{
                  logger.error("{}:{} is not reachable!", node::getIp, node::getPort);
                  unregisterNode(node);
                  dumpNode(node);
                  connectWithNewNode();
                  length=connectedNodes.size();
                }
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
        if (Thread.interrupted()) {
            break;
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
            Thread.currentThread().interrupt();
        }
    }
  }

  private void unregisterNode(Node node) {
    logger.info("Unregistering {}:{} since it is not reachable!", node.getIp(), node.getPort());
    String unregisterMessage = util.generateMessage(Messages.UNREG.getValue(), node.getIp(),
            Integer.toString(node.getPort()), node.getUsername());
    try (DatagramSocket socket = new DatagramSocket()) {
        String unRegResponse = util.sendMessage(unregisterMessage.getBytes(), node.getIp(), socket, node.getPort(),
            Util.DEFAULT_TIMEOUT);
        if (unRegResponse.equals("9999")) {
            logger.error("Error in the unregister message!");
        }
        } catch (SocketException e) {
            logger.error(e);
        }
    }

  private void dumpNode(Node node) {
    logger.info("Dumping {}:{} since it is not reachable!", node.getIp(), node.getPort());
    connectedNodes.remove(node);
    }

    private void connectWithNewNode() {

        // TODO: Current node should connect with a new node, since a node is unreachable.
    }
}
