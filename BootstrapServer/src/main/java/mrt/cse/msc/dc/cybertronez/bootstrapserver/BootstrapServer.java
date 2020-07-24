package mrt.cse.msc.dc.cybertronez.bootstrapserver;

import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Node;
import mrt.cse.msc.dc.cybertronez.Util;
import mrt.cse.msc.dc.cybertronez.bootstrapserver.api.BootStrapAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.msf4j.MicroservicesRunner;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class BootstrapServer {

    private static Logger LOG = LogManager.getLogger(BootstrapServer.class);
    private Util util = new Util();
    private static List<Node> nodes = new ArrayList<>();

    public static void main(final String[] args) {

        // start bootstrap server
        new Thread(() -> {
            LOG.info("Initializing bootstrap server... ");
            new BootstrapServer().startBootstrapServer();
        }).start();

        // start http server
        new Thread(() -> {
            LOG.info("Initializing HTTP API in the bootstrap server... ");
            BootStrapAPI bootStrapAPI = new BootStrapAPI();
            bootStrapAPI.setNodeList(nodes);
            new MicroservicesRunner(55556).deploy(bootStrapAPI).start();
        }).start();
    }

    @SuppressWarnings({"java:S3776", "java:S2119"})
    private void startBootstrapServer() {

        final Node bsNode = new Node("localhost", 55555);
        Logger logger = LogManager.getLogger(BootstrapServer.class.getName() + " - " + bsNode.toString());
        String s;
        final Random r = new Random();

        try (final DatagramSocket sock = new DatagramSocket(bsNode.getPort())) {
            logger.info("Bootstrap Server created at {}. Waiting for incoming data...", bsNode::getPort);

            while (true) {
                final byte[] buffer = new byte[65536];
                final DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                final byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());

                //echo the details of incoming data - client ip : client port - client message
                final String finalS = s;
                logger.info("{} : {} - {}", incoming.getAddress()::getHostAddress, incoming::getPort, () -> finalS);

                final StringTokenizer st = new StringTokenizer(s, " ");

                final String length = st.nextToken();
                final String command = st.nextToken();

                logger.info("Length: {}", () -> length);
                logger.info("Command: {}", () -> command);

                if (command.equals(Messages.REG.getValue())) {
                    final StringBuilder replyBuilder = new StringBuilder(Messages.REGOK.getValue());

                    final String ip = st.nextToken();
                    final int port = Integer.parseInt(st.nextToken());
                    final String username = st.nextToken();
                    if (nodes.isEmpty()) {
                        replyBuilder.append(" ").append(Messages.CODE0);
                        nodes.add(new Node(ip, port, username));
                    } else {
                        boolean isOkay = true;
                        for (final Node node : nodes) {
                            if (node.getPort() == port) {
                                if (node.getUsername().equals(username)) {
                                    replyBuilder.append(Messages.CODE9998);
                                } else {
                                    replyBuilder.append(Messages.CODE9997);
                                }
                                isOkay = false;
                            }
                        }
                        if (isOkay) {
                            if (nodes.size() == 1) {
                                replyBuilder.append(" 1 ").append(nodes.get(0).getIp()).append(" ").append(nodes.get(0).getPort());
                            } else if (nodes.size() == 2) {
                                replyBuilder.append(" 2 ").append(nodes.get(0).getIp()).append(" ").append(nodes.get(0).getPort())
                                        .append(" ").append(nodes.get(1).getIp()).append(" ").append(nodes.get(1).getPort());
                            } else {
                                final int Low = 0;
                                final int High = nodes.size();
                                final int random1 = r.nextInt(High - Low) + Low;
                                int random2 = r.nextInt(High - Low) + Low;
                                while (random1 == random2) {
                                    random2 = r.nextInt(High - Low) + Low;
                                }

                                final int finalRandom_ = random2;
                                logger.info("{} {}", () -> random1, () -> finalRandom_);

                                replyBuilder.append(" 2 ").append(nodes.get(random1).getIp()).append(" ")
                                        .append(nodes.get(random1).getPort()).append(" ").append(nodes.get(random2).getIp()).append(" ")
                                        .append(nodes.get(random2).getPort());
                            }
                            nodes.add(new Node(ip, port, username));
                        }
                    }

                    final String reply = util.generateMessage(replyBuilder.toString());

                    logger.info("Reply: {}", () -> reply);

                    final DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                } else if (command.equals(Messages.UNREG.getValue())) {
                    final String ip = st.nextToken();
                    final int port = Integer.parseInt(st.nextToken());
                    final String username = st.nextToken();
                    final String reply = util.generateMessage(Messages.UNROK.getValue(), Messages.CODE0.getValue());
                    for (int i = 0; i < nodes.size(); i++) {
                        if (nodes.get(i).getPort() == port) {
                            nodes.remove(i);
                            logger.info("Reply: {}", () -> reply);
                            break;
                        }
                    }
                    final DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                } else if (command.equals(Messages.ECHO.getValue())) {
                    for (final Node node : nodes) {
                        logger.info("{}:{} - {}", node::getIp, node::getPort, node::getUsername);
                    }
                    final String reply = util.generateMessage(Messages.ECHOK.getValue(), Messages.CODE0.getValue());
                    logger.info("Reply: {}", () -> reply);
                    final DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length,
                            incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                }

            }
        } catch (IOException e) {
            logger.error("IOException", e);
        }
    }

}
