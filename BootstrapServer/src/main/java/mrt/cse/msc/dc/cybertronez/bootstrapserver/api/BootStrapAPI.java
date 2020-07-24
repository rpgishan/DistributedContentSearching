package mrt.cse.msc.dc.cybertronez.bootstrapserver.api;

import mrt.cse.msc.dc.cybertronez.Node;
import mrt.cse.msc.dc.cybertronez.bootstrapserver.api.dao.NodesDAO;
import mrt.cse.msc.dc.cybertronez.mapper.NodeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.GET;
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

        return Response.status(200).entity(nodes).type(MediaType.APPLICATION_JSON).build();
    }

    public void setNodeList(List<Node> nodeList) {

        this.nodeList = nodeList;
    }
}
