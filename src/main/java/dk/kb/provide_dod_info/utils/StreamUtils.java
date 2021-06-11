package dk.kb.provide_dod_info.utils;

import dk.kb.provide_dod_info.exception.ArgumentCheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling standard stream issues.
 */
public final class StreamUtils {
    /** The default buffer size. 32 kb. */
    private static final int IO_BUFFER_SIZE = 32*1024;

    /**
     * Utility function for moving data from an inputstream to an outputstream.
     *
     * @param in The input stream to copy to the output stream.
     * @param out The output stream where the input stream should be copied.
     * @throws IOException If any problems occur with transferring the data between the streams.
     */
    public static void copyInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
        ArgumentCheck.checkNotNull(in, "InputStream in");
        ArgumentCheck.checkNotNull(out, "OutputStream out");

        try {
            byte[] buf = new byte[IO_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Extracts the content of an input stream as lines.
     * @param in The input stream.
     * @return A list of all the lines from the inputstream.
     * @throws IOException If it .
     */
    public static List<String> extractInputStreamAsLines(InputStream in) throws IOException {
        ArgumentCheck.checkNotNull(in, "InputStream in");
        List<String> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while((line = br.readLine()) != null) {
                res.add(line);
            }
        }
        return res;
    }

    /**
     * Extracts the content of an input stream as a string.
     * @param in The input stream to extract.
     * @return The string of the input stream.
     * @throws IOException If the input stream cannot be read.
     */
    public static String extractInputStreamAsString(InputStream in) throws IOException {
        ArgumentCheck.checkNotNull(in, "InputStream in");
        StringBuilder res = new StringBuilder();
        for(String s : extractInputStreamAsLines(in)) {
            res.append(s).append("\n");
        }

        return res.toString();
    }
}
