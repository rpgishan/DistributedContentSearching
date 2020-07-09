package mrt.cse.msc.dc.cybertronez;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class HashGenerator
{
  public static String getHash(final String word)
  {
    final String hash = "";
    try
    {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] encodedHash = digest.digest(word.getBytes(StandardCharsets.UTF_8));

      return bytesToHex(encodedHash);
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }

    return hash;
  }

  public static String bytesToHex(final byte[] hashes)
  {
    final StringBuilder hexString = new StringBuilder();

    for (final byte hash : hashes)
    {
      final String hex = Integer.toHexString(0xff & hash);

      if (hex.length() == 1)
      {
        hexString.append('0');
      }

      hexString.append(hex);
    }

    return hexString.toString();
  }

    public static int getDifference(byte[] fileName, byte[] nodeId) {

        int diff = 0;
        ArrayList<Integer> diffList = new ArrayList<>();
        for (int i = 0; i < 64; i++)
        {
          diff = diff + Math.abs(fileName[i] - nodeId[i]);
        }

        return diff;
    }




}
