package mrt.cse.msc.dc.cybertronez;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator
{
  String getHash(final String word)
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

  private String bytesToHex(final byte[] hashes)
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
}
