package mrt.cse.msc.dc.cybertronez;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator
{
  String getHash(String word)
  {
    String hash = "";
    try
    {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(word.getBytes(StandardCharsets.UTF_8));

      return bytesToHex(encodedHash);
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }

    return hash;
  }

  private String bytesToHex(byte[] hashes)
  {
    StringBuilder hexString = new StringBuilder();

    for (byte hash : hashes)
    {
      String hex = Integer.toHexString(0xff & hash);

      if (hex.length() == 1)
      {
        hexString.append('0');
      }

      hexString.append(hex);
    }

    return hexString.toString();
  }
}
