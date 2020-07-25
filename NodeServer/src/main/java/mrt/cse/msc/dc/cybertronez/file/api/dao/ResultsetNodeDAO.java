package mrt.cse.msc.dc.cybertronez.file.api.dao;

import mrt.cse.msc.dc.cybertronez.dao.NodeDAO;

public class ResultsetNodeDAO extends NodeDAO {

    private String fileName;

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }
}
