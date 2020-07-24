package mrt.cse.msc.dc.cybertronez.bootstrapserver.api;

import mrt.cse.msc.dc.cybertronez.Node;
import mrt.cse.msc.dc.cybertronez.bootstrapserver.api.dao.NodesDAO;
import mrt.cse.msc.dc.cybertronez.mapper.NodeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bootstrap")
public class BootStrapAPI {

    private List<Node> nodeList;
    private NodeMapper nodeMapper = new NodeMapper();

    @GET
    @Path("/getAllNodes")
    public Response getAllNodes() {

        NodesDAO nodes = new NodesDAO();
        nodes.setNodes(Optional.ofNullable(nodeList).map(nodeMapper::convertToRest).orElse(new ArrayList<>()));

        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(nodes).type(MediaType.APPLICATION_JSON).build();
    }

    public void setNodeList(List<Node> nodeList) {

        this.nodeList = nodeList;
    }

    @OPTIONS
    @Path("/getAllNodes")
    public Response getAllNodesOptions() {
           return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                .build();
    }
}
