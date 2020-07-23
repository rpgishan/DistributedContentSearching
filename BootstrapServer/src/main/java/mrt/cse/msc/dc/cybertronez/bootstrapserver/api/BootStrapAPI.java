package mrt.cse.msc.dc.cybertronez.bootstrapserver.api;

import mrt.cse.msc.dc.cybertronez.bootstrapserver.api.dao.NodesDAO;
import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bootstrap")
public class BootStrapAPI {

    @GET
    @Path("/getAllNodes")
    public Response getAllNodes() {

        NodesDAO nodes =  new NodesDAO();
        List<NodeDAO> nodeList = new ArrayList<>();
        nodes.setNodes(nodeList);
        // TODO: get all available nodes and populate nodeList
        return Response.status(200).entity(nodes).type(MediaType.APPLICATION_JSON).build();
    }

}
