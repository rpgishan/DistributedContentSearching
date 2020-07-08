package mrt.cse.msc.dc.cybertronez.test;

import java.net.SocketException;

import mrt.cse.msc.dc.cybertronez.Client;

public class StartClients
{
  public static void main(String[] args) throws SocketException
  {
    //Start clients
    String ip = "localhost";
    int portNo = 8082;
    int noOfClients = 10;
    int endingPortNo = portNo + noOfClients;
    while (portNo <= endingPortNo)
    {
      new Client(ip, Integer.toString(portNo), "client:" + portNo);
      portNo++;
      try
      {
        Thread.sleep(2000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
