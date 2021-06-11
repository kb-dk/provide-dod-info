package dk.kb.provide_dod_info.utils;

import dk.kb.provide_dod_info.exception.ArgumentCheck;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;

/**
 * Utility class for dealing with files.
 */
public class FileUtils {

    /**
     * Create or reuse directory
     * @param dirPath Path to directory
     * @return The direcory at the given path.
     * @throws IOException When creating a directory fail
     */
    public static File createDirectory(String dirPath) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(dirPath, "String dirPath");
        Path path = Paths.get(dirPath);
        if(!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return new File(dirPath);
    }

    /**
     * Retrieves an existing directory. Or throws an exception, if the directory does not exist.
     * @param dirPath The path to the directory.
     * @return The directory.
     */
    public static File getExistingDirectory(String dirPath) {
        ArgumentCheck.checkNotNullOrEmpty(dirPath, "String dirPath");
        File res = new File(dirPath);
        if(!res.isDirectory()) {
            throw new IllegalStateException("The path '" + dirPath + "' must contain a directory.");
        }
        return res;
    }

    /**
     * Retrieves an existing file. Or throws an exception, if the file does not exist.
     * @param filePath The path to the file.
     * @return The file.
     */
    public static File getExistingFile(String filePath) {
        ArgumentCheck.checkNotNullOrEmpty(filePath, "String filePath");
        File res = new File(filePath);
        if(!res.isFile()) {
            throw new IllegalStateException("The path '" + filePath + "' must contain a file.");
        }
        return res;
    }
    /**
     * Delete method, which validates that the file is actually not present afterwards.
     * @param f The file to delete.
     */
    public static void deleteFile(File f) {
        if(f == null || !f.exists()) {
            return;
        }
        boolean success = f.delete();
        if(!success) {
            throw new IllegalStateException("Could not delete the file '" + f.getAbsolutePath() + "'");
        }
    }

    /**
     * Copies from one directory to another.
     * @param from The from directory.
     * @param to The to directory.
     * @throws IOException If it  to copy.
     */
    public static void copyDirectory(File from, File to) throws IOException {
        ArgumentCheck.checkExistsDirectory(from, "File from");
        if(to.exists()) {
            ArgumentCheck.checkExistsDirectory(to, "File to");
        } else {
            createDirectory(to.getAbsolutePath());
        }

        for(File f : getFilesInDirectory(from)) {
            copyFileFollowSymbolicLinks(f, new File(to, f.getName()));
        }
    }

    /**
     * Copies a file or the content of its symbolic link to a given destination.
     * @param fromFile The from file.
     * @param toFile The to file.
     * @throws IOException If it  to handle the copy or symbolic links.
     */
    public static void copyFileFollowSymbolicLinks(File fromFile, File toFile) throws IOException {
        ArgumentCheck.checkExistsNormalFile(fromFile, "File from");
        Path toPath = toFile.toPath();
        Path fromPath = getFileOrSymlinkPath(fromFile);
        Files.copy(fromPath, toPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Retrieves the list of files from a directory, with the notorious null-pointer check.
     * @param dir The directory to retrieve the files from.
     * @return The collection of files in a directory.
     */
    public static Collection<File> getFilesInDirectory(File dir) {
        ArgumentCheck.checkExistsDirectory(dir, "File dir");
        File[] files = dir.listFiles();
        if(files != null) {
            return Arrays.asList(files);
        } else {
            throw new IllegalStateException("Unable to obtain the list of files from directory "
                    + dir.getAbsolutePath());
        }
    }

    /**
     * Retrieves the path for the given file or the path which a symbolic link points to.
     * @param f The file to retrieve the path for.
     * @return The path for the file or the path of the symbolic link.
     * @throws IOException If it  to ready the symbolic link.
     */
    public static Path getFileOrSymlinkPath(File f) throws IOException {
        ArgumentCheck.checkExistsNormalFile(f, "File f");
        if(Files.isSymbolicLink(f.toPath())) {
            return Files.readSymbolicLink(f.toPath());
        } else {
            return f.toPath();
        }
    }

    /**
     * Moves one file to another destination.
     * @param orig The original file to move.
     * @param dest The destination for file.
     * @throws IOException If it  to move the file.
     */
    public static void moveFile(File orig, File dest) throws IOException {
        ArgumentCheck.checkExistsNormalFile(orig, "File orig");
        Files.move(orig.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Moves a directory.
     * If the destination directory already exists, then all the files within the origDir is moved individually to
     * the destination folder.
     * @param origDir The original directory, which should be moved.
     * @param destDir The destination of the directory.
     * @throws IOException If it  to move the directory, or if the destination is a file.
     */
    public static void moveDirectory(File origDir, File destDir) throws IOException {
        ArgumentCheck.checkExistsDirectory(origDir, "File origDir");

        if(destDir.exists()) {
            if(!destDir.isDirectory()) {
                throw new IOException("Cannot move a directory ('" + origDir.getAbsolutePath() + "') into a file ('"
                        + destDir.getAbsolutePath() + "')");
            }

            File[] files = origDir.listFiles();
            if(files != null) {
                for(File f : files) {
                    File destFile = new File(destDir, f.getName());
                    moveFile(f, destFile);
                }
            }
            deleteFile(origDir);
        } else {
            Files.move(origDir.toPath(), destDir.toPath(), StandardCopyOption.ATOMIC_MOVE);
        }
    }


}
