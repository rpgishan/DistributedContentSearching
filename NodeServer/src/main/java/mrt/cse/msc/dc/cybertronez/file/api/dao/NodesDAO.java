package mrt.cse.msc.dc.cybertronez.file.api.dao;

import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;

import java.util.List;

public class NodesDAO {

    private List<NodeDAO> nodes;

    public List<NodeDAO> getNodes() {

        return nodes;
    }

    public void setNodes(List<NodeDAO> nodes) {

        this.nodes = nodes;
    }
}
