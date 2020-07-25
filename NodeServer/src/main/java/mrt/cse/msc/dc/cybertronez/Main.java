package mrt.cse.msc.dc.cybertronez;

public class Main {

    public static void main(final String[] args) {

        if (args.length >= 3) {
            final String port = args[0];
            final String bsIp = args[1];
            final String bsPort = args[2];

            new Server(port, bsIp, bsPort);
        }
    }
}
