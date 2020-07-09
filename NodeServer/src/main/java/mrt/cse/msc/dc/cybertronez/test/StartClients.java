package mrt.cse.msc.dc.cybertronez.test;

import java.net.SocketException;

import mrt.cse.msc.dc.cybertronez.Client;

public class StartClients
{
  static int noOfClients = 10;
  public static void main(String[] args) throws SocketException
  {
    //Start clients
    String ip = "127.0.0.1";
    int portNo = 8081;
    int endingPortNo = portNo + noOfClients;
    while (portNo < endingPortNo)
    {
      new Client(ip, Integer.toString(portNo), "client:" + portNo);
      portNo++;
      try
      {
        Thread.sleep(5000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
    System.out.println("Done...");
  }
}
