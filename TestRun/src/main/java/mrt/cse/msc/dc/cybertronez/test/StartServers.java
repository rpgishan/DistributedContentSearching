package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.Server;

public class StartServers {

    static int noOfClients = 10;

    public static void main(final String[] args) {
        //Start clients
        final String bsIP = "127.0.0.1";
        final String bsPort = "55555";
        int portNo = 8081;
        final int endingPortNo = portNo + noOfClients;
        while (portNo < endingPortNo) {
            new Server(Integer.toString(portNo), bsIP, bsPort);
            portNo++;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done...");
    }
}
