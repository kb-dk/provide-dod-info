package dk.kb.provide_dod_info.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class UxCmdUtils {
    private static final Logger log = LoggerFactory.getLogger(UxCmdUtils.class);

    /**
     * Execute a Unix command with up to 6 options/parameters
     * @param cmd The Unix command
     * @param param The options and parameters
     */
    public static synchronized void execCmd(String cmd, String... param /*, Optional<String> option1, Optional<String> option2*/ ) {
        Process p;
        String p1 = param.length > 0 ? param[0] : "";
        String p2 = param.length > 1 ? param[1] : "";
        String p3 = param.length > 2 ? param[2] : "";
        String p4 = param.length > 3 ? param[3] : "";
        String p5 = param.length > 4 ? param[4] : "";
        String p6 = param.length > 5 ? param[5] : "";
        try {
            p = Runtime.getRuntime().exec(cmd + " " + p1 + " " + p2 + " " + p3 + " " + p4 + " " + p5 + " " + p6);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            while (br.readLine() != null)
            p.waitFor();
            log.debug("Unix exit value: " + p.exitValue());
            p.destroy();
        } catch (Exception e) {
            log.error("Failed executing Unix command: " + e);
        }
    }

}
