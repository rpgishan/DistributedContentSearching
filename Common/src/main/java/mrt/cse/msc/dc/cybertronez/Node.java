package mrt.cse.msc.dc.cybertronez;

public class Node {
    private String ip;
    private int port;
    private String username;

    public Node(final String ip, final int port, final String username){
        this.ip = ip;
        this.port = port;
        this.username = username;
    }

    public String getIp(){
        return this.ip;
    }

    public String getUsername(){
        return this.username;
    }

    public int getPort(){
        return this.port;
    }
}
