package mrt.cse.msc.dc.cybertronez.file.api;

import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;
import mrt.cse.msc.dc.cybertronez.file.FileGenerator;
import mrt.cse.msc.dc.cybertronez.file.api.dao.FileListDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/fileAPI")
public class FileAPI {
    private static Logger LOG = LogManager.getLogger(FileAPI.class);

    @GET
    @Path("/retrieveFile/{file_name}")
    public Response retrieveFile(@PathParam("file_name") String fileName) {

        // generate random file size
        int fileSize = 1024 * 1024 * (2 + new Random().nextInt(8));
        LOG.info("Creating file with name " + fileName + " and with size " + fileSize);
        File responseFIle = FileGenerator.generate(fileSize, fileName);
        String contentDispositionHeader = "attachment; filename=\"" + fileName + "\"";
        return Response.status(200).entity(responseFIle).type("text/plain").header("Content-Disposition", contentDispositionHeader).build();
    }

    @GET
    @Path("/searchFile/{file_name}")
    public Response searchFile(@PathParam("file_name") String fileName) {
        // initiate UDP file search and return the details of node with file
        LOG.info("File search initiated for file " + fileName);

        // TODO: get real values for host and port
        String host = "localhost";
        int port = 0;
        // create response object
        NodeDAO node = new NodeDAO();
        node.setHost(host);
        node.setPort(port);

        return Response.status(200).entity(node).type(MediaType.APPLICATION_JSON).build();
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
        node.setPort(0);
        node.setHost("localhost");

        List<String> fileList = new ArrayList<>();

        // TODO: populate file list with real data

        fileListDAO.setNode(node);
        fileListDAO.setFiles(fileList);

        return Response.status(200).entity(fileListDAO).type(MediaType.APPLICATION_JSON).build();
    }
}
