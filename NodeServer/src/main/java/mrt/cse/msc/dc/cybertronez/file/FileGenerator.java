package mrt.cse.msc.dc.cybertronez.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class FileGenerator {

    public static File generate(final int fileSize, final String fileName) {
//        String fileName = "sample-" + Calendar.getInstance().getTimeInMillis() + ".txt";
        final String filePath = fileName;

        final Random random = new Random();
        final File file = new File(filePath);

        try (final FileWriter out = new FileWriter(file)) {
            final char[] chars = new char[fileSize];
            final int min = 32;
            final int max = 126;
            for (int i = 0; i < chars.length; i++) {
                chars[i] = (i != 0 && i % 1024 == 0) ? '\n' : (char) (min + random.nextInt(max - min));
            }
            out.write(chars);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

}
