package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.Server;
import mrt.cse.msc.dc.cybertronez.bootstrapserver.BootstrapServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StartServers {

    private static final Logger LOGGER = LogManager.getLogger(StartServers.class);

    static int noOfClients = 10;

    public static void main(final String[] args) throws InterruptedException {
        //Start clients
        final String bsIP = "127.0.0.1";
        final String bsPort = "55555";

        new Thread(() -> BootstrapServer.main(args)).start();
        LOGGER.info("Started Bootstrap server");

        int portNo = 8081;
        final int endingPortNo = portNo + noOfClients;
        while (portNo < endingPortNo) {
            Thread.sleep(5000);
            new Server(Integer.toString(portNo), bsIP, bsPort);
            int finalPortNo = portNo;
            LOGGER.info("Started Node server in {}",()-> finalPortNo);
            portNo++;
        }
        LOGGER.info("Started all node servers");
        LOGGER.info("Done...");
//        System.out.println("Done...");
    }
}
