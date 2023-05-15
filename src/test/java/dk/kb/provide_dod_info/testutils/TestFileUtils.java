package dk.kb.provide_dod_info.testutils;

import dk.kb.provide_dod_info.utils.StreamUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class TestFileUtils {

    protected static final String TEMPDIR_NAME = "tempDir";
    protected static File tempDir = new File(TEMPDIR_NAME);

    public static String readFile(File file) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            StringBuilder res = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                res.append(line);
                // Add new line feed??
            }

            return res.toString();
        }
    }

    public static void setup() {
        try {
            tempDir = createEmptyDirectory(TEMPDIR_NAME);
        } catch (Exception e) {
            throw new RuntimeException("FAIL", e);
        }
    }

    public static void tearDown() {
        deleteFile(tempDir);
    }

    public static File getTempDir() {
        return tempDir;
    }

    public static File createEmptyDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if(Files.exists(path)) {
            deleteFile(path.toFile());
        }
        Files.createDirectories(path);
        return new File(dirPath);
    }

    public static void deleteFile(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteFile(f);
            }
        }
        file.delete();
    }

    public static void createFile(File outputFile, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static File createTempFile(String content) throws IOException {
        File outputFile = new File(tempDir, UUID.randomUUID().toString());
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return outputFile;
    }

    public static File copyFileToTemp(File f) throws IOException {
        File res = new File(tempDir, f.getName());

        copyFile(f, res);

        return res;
    }

    public static void copyFile(File from, File to) throws IOException {
        StreamUtils.copyInputStreamToOutputStream(new FileInputStream(from), new FileOutputStream(to));
    }
}
