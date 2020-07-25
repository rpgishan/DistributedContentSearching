package mrt.cse.msc.dc.cybertronez.file.api.dao;

import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;

public class ResultsetNodeDAO extends NodeDAO {

    private String fileName;
    private String hops;

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    public String getHops() {

        return hops;
    }

    public void setHops(String hops) {

        this.hops = hops;
    }
}
