package mrt.cse.msc.dc.cybertronez.mapper;

import mrt.cse.msc.dc.cybertronez.Node;
import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;

import java.util.List;
import java.util.stream.Collectors;

public class NodeMapper {

    public NodeDAO convertToRest(Node node) {

        NodeDAO dao = new NodeDAO();
        dao.setHost(node.getIp());
        dao.setPort(node.getPort());
        return dao;
    }

    public List<NodeDAO> convertToRest(List<Node> nodes) {

        return nodes.stream().map(this::convertToRest).collect(Collectors.toList());
    }

    public Node convertToServer(NodeDAO dao) {

        return new Node(dao.getHost(), dao.getPort());
    }

    public List<Node> convertToServer(List<NodeDAO> daos) {

        return daos.stream().map(this::convertToServer).collect(Collectors.toList());
    }
}
