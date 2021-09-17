package dk.kb.provide_dod_info.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Class for executing a Unix command */
public class UxCmdUtils {
    private static final Logger log = LoggerFactory.getLogger(UxCmdUtils.class);

    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .forEach(consumer);
        }
    }

    public static void execCmd(String param)  {
        Process process;
        try {
            process = Runtime.getRuntime().exec( param );
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            if (exitCode != 0){
                log.warn("UNIX command '{}' failed, exit code = {}", param, exitCode);
//                throw new IllegalStateException("UNIX command failed");
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed executing Unix command: " + e);
        }
    }
}
