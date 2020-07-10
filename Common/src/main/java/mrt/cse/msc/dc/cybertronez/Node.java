package mrt.cse.msc.dc.cybertronez;

import java.util.Objects;

public class Node {

    private String ip;
    private int port;
    private String username;
    private String userNameHash;
    private StringBuilder fieList = new StringBuilder();

    public StringBuilder getFieList() {

        return fieList;
    }

    public void addFileToList(final String fileName) {

        if (fieList.length() != 0) {
            fieList.append(" , ");//TODO need to change this delim
        }
        fieList.append(fileName);
    }

    public String getUserNameHash() {

        return userNameHash;
    }

    public void setUserNameHash(final String userNameHash) {

        this.userNameHash = userNameHash;
    }

    public Node(final String ip, final String port, final String username) {

        this(ip, Integer.parseInt(port), username);
    }

    public Node(final String ip, final String port) {

        this(ip, Integer.parseInt(port));
    }

    public Node(final String ip, final int port) {

        this(ip, port, ip + ":" + port);
    }

    public Node(final String ip, final int port, final String username) {

        this.ip = ip;
        this.port = port;
        this.username = username;
        this.userNameHash = new HashGenerator().getHash(username);
    }

    public String getIp() {

        return this.ip;
    }

    public String getUsername() {

        return this.username;
    }

    public int getPort() {

        return this.port;
    }

    @Override
    public String toString() {

        return getIp() + ":" + getPort();
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Node node = (Node) o;
        return port == node.port &&
                Objects.equals(ip, node.ip);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ip, port);
    }
}
