package mrt.cse.msc.dc.cybertronez.test;

import mrt.cse.msc.dc.cybertronez.Client;

public class StartClients
{
  static int noOfClients = 10;
  public static void main(String[] args)
  {
    //Start clients
    String ip = "127.0.0.1";
    int portNo = 8081;
    int endingPortNo = portNo + noOfClients;
    while (portNo < endingPortNo)
    {
      new Client(ip, Integer.toString(portNo),ip,"55555");
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
