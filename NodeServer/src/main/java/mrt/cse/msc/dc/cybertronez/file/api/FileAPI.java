package mrt.cse.msc.dc.cybertronez.file.api;

import mrt.cse.msc.dc.cybertronez.Messages;
import mrt.cse.msc.dc.cybertronez.Node;
import mrt.cse.msc.dc.cybertronez.Util;
import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;
import mrt.cse.msc.dc.cybertronez.file.FileGenerator;
import mrt.cse.msc.dc.cybertronez.file.api.dao.ErrorResponse;
import mrt.cse.msc.dc.cybertronez.file.api.dao.FileListDAO;
import mrt.cse.msc.dc.cybertronez.file.api.dao.NodesDAO;
import mrt.cse.msc.dc.cybertronez.file.api.dao.ResultsetNodeDAO;
import mrt.cse.msc.dc.cybertronez.mapper.NodeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.StringTokenizer;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/fileAPI")
public class FileAPI {

    private static Logger LOG = LogManager.getLogger(FileAPI.class);

    private Node nodeInfo;
    private NodeMapper nodeMapper = new NodeMapper();
    private List<Node> connectedNodes;

    public void setNodeInfo(Node nodeInfo) {

        this.nodeInfo = nodeInfo;
    }

    public void setConnectedNodes(List<Node> connectedNodes) {

        this.connectedNodes = connectedNodes;
    }

    Util util = new Util();

    @GET
    @Path("/retrieveConnectedNodes")
    public Response getConnectedNodes() {
        LOG.info("Returning details of all the connected nodes.. ");

        NodesDAO nodes = new NodesDAO();
        nodes.setNodes(Optional.ofNullable(connectedNodes).map(nodeMapper::convertToRest).orElse(new ArrayList<>()));

        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(nodes).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/retrieveFile/{file_name}")
    public Response retrieveFile(@PathParam("file_name") String fileName) {

        // generate random file size
        int fileSize = 1024 * 1024 * (2 + new Random().nextInt(8));
        LOG.info("Creating file with name " + fileName + " and with size " + fileSize);
        File responseFIle = FileGenerator.generate(fileSize, fileName);
        String contentDispositionHeader = "attachment; filename=\"" + fileName + "\"";
        return Response.status(200)
                .entity(responseFIle).type("text/plain")
                .header("Content-Disposition", contentDispositionHeader)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .build();
    }

    @OPTIONS
    @Path("/retrieveFile/{file_name}")
    public Response retrieveFileOptions() {

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                .build();
    }

    @GET
    @Path("/searchFile/{file_name}")
    public Response searchFile(@PathParam("file_name") String fileName) {
        // initiate UDP file search and return the details of node with file
        LOG.info("File search initiated for file " + fileName);
        ErrorResponse error = new ErrorResponse();
        String foundIp = "";
        String foundPort = "";
        String foundFile = "";
        try (final DatagramSocket socket = new DatagramSocket()) {

            //Search
            String port = Integer.toString(this.nodeInfo.getPort());
            String nodeIP = this.nodeInfo.getIp();

            //initiate search request
            final String generateMessage = util.generateMessage(Messages.SER.getValue(), nodeIP, port, "0",
                    fileName);
            String received = util.sendMessage(generateMessage.getBytes(), nodeIP, socket, this.nodeInfo.getPort(),
                    Util.DEFAULT_TIMEOUT);

            final StringTokenizer st = new StringTokenizer(received, " ");
            final String length = st.nextToken();
            final String command = st.nextToken();
            final String code = st.nextToken();

            if (command.equals(Messages.SEROK.getValue())) {
                if (code.equals(Messages.CODE0.getValue())) {
                    String[] host = received.split(" ");
                    String hops = host[3];
                    foundIp = host[host.length - 1].split(":")[0];
                    foundPort = host[host.length - 1].split(":")[1];
                    foundFile = host[host.length - 2];

                    // create response object
                    ResultsetNodeDAO node = new ResultsetNodeDAO();
                    node.setHost(foundIp);
                    node.setPort(Integer.parseInt(foundPort.replace("\n", "").replace("\r", "")));
                    node.setFileName(foundFile);
                    node.setHops(hops);

                    return Response.status(200).entity(node)
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET")
                            .type(MediaType.APPLICATION_JSON).build();

                } else if (code.equals(Messages.CODE9998.getValue())) {

                    error.setError("File Could not be found.");
                    return Response.status(404)
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET")
                            .entity(error).type(MediaType.APPLICATION_JSON).build();
                }
                LOG.info("Response received for file search api call {}", received);
            }
        } catch (SocketException e) {
            LOG.error("SocketException ", e);
        }

        error.setError("Internal server error.");
        return Response.status(500)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .entity(error).type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/searchFile/{file_name}")
    public Response searchFileOptions() {

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                .build();
    }

    @GET
    @Path("/retrieveAllFiles")
    public Response retrieveAllFiles() {
        // make UDP call to get all available file in the node and return
        LOG.info("Returning all existing files in node.. ");
        FileListDAO fileListDAO = new FileListDAO();
        // set node information (own udp host and port)
        NodeDAO node = new NodeDAO();

        // TODO: set real node information
        node.setPort(this.nodeInfo.getPort());
        node.setHost(this.nodeInfo.getIp());

        List<String> fileList = Arrays.asList(this.nodeInfo.getFieList().split(","));

        // TODO: populate file list with real data

        fileListDAO.setNode(node);
        fileListDAO.setFiles(fileList);

        return Response.status(200).entity(fileListDAO)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/retrieveAllFiles")
    public Response retrieveAllFilesOptions() {

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                .build();
    }
}
