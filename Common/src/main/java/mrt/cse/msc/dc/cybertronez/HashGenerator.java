package mrt.cse.msc.dc.cybertronez;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class HashGenerator {

    private static final Logger LOGGER = LogManager.getLogger(HashGenerator.class);

    public String getHash(final String word) {

        final String hash = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedHash = digest.digest(word.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e);
        }

        return hash;
    }

    public String bytesToHex(final byte[] hashes) {

        final StringBuilder hexString = new StringBuilder();

        for (final byte hash : hashes) {
            final String hex = Integer.toHexString(0xff & hash);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }

    public int getDifference(final byte[] fileName, final byte[] nodeId) {

        int diff = 0;
        final ArrayList<Integer> diffList = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            diff = diff + Math.abs(fileName[i] - nodeId[i]);
        }

        return diff;
    }

}
