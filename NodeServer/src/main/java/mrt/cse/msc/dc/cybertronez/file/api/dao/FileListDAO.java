package mrt.cse.msc.dc.cybertronez.file.api.dao;

import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;

import java.util.List;

public class FileListDAO {

    private NodeDAO node;
    private List<String> files;

    public NodeDAO getNode() {

        return node;
    }

    public void setNode(NodeDAO node) {

        this.node = node;
    }

    public List<String> getFiles() {

        return files;
    }

    public void setFiles(List<String> files) {

        this.files = files;
    }
}
