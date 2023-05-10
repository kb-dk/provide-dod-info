package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.exception.ArgumentCheck;
import dk.kb.provide_dod_info.utils.FileUtils;
import dk.kb.provide_dod_info.utils.UxCmdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class used to sort the output data in directories with 50 years intervals from 1400 to 1899
 */
public class DataHandler {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DataHandler.class);
    /**
     * The configuration.
     */
    protected final Configuration conf;

    public DataHandler(Configuration conf) {
        ArgumentCheck.checkNotNull(conf, "Configuration conf");
        this.conf = conf;
    }

    public void addReadMeIDE() {
        try {
            URL resource = DataHandler.class.getResource("/readme.txt");
            if(resource !=null){
                String readmePath = resource.getPath();
                File fromReadme = new File(readmePath);
                File toReadme = new File(conf.getTempDir().getAbsolutePath() + "/readme.txt");
                FileUtils.moveFile(fromReadme, toReadme);
            }
        } catch (IOException e) {
            log.warn("readme.txt was not included");
            e.printStackTrace();
        }
    }

    public void addReadMe() {
        log.info("addReadMe entered");
        try {
            //todo: extract version from pom, hmm can't do like readme
            UxCmdUtils.execCmd("jar xf " +                               // extract
                    "provide-dod-info-1.0.4/lib/provide-dod-info-1.0.4.jar " + // fromFile
                    "readme.txt " );                                           // file to extract
            UxCmdUtils.execCmd("chmod -x readme.txt");
            File fromReadme = new File("readme.txt");
            File toReadme = new File(conf.getTempDir().getAbsolutePath() + "/readme.txt");
            FileUtils.moveFile(fromReadme, toReadme);
        } catch (IOException e) {
            log.warn("readme.txt was not included");
            e.printStackTrace();
        }
    }

    public void sortDirectories(Map<String, String> data) {
        log.info("sortDirectories entered");

        String regEx = "^(14)[0-4][0-9]$";
        String subDir = "/1400to1449";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(14)[5-9][0-9]$";
        subDir = "/1450to1499";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(15)[0-4][0-9]$";
        subDir = "/1500to1549";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(15)[5-9][0-9]$";
        subDir = "/1550to1599";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(16)[0-4][0-9]$";
        subDir = "/1600to1649";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(16)[5-9][0-9]$";
        subDir = "/1650to1699";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(17)[0-4][0-9]$";
        subDir = "/1700to1749";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(17)[5-9][0-9]$";
        subDir = "/1750to1799";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(18)[0-4][0-9]$";
        subDir = "/1800to1849";
        moveToSubDirs(data, regEx, subDir);

        regEx = "^(18)[5-9][0-9]$";
        subDir = "/1850to1899";
        moveToSubDirs(data, regEx, subDir);
    }

    private void moveToSubDirs(Map<String, String> data, String regEx, String subDir) {
        log.info("moveToSubDirs entered");
        List<Map.Entry<String, String>> entryList = data.entrySet().stream()
                .filter(a -> a.getValue().matches(regEx))
                .collect(Collectors.toList());

        List<String> barcodes = new ArrayList<>();
        IntStream.range(0, entryList.size())
                .forEach(i -> barcodes.add(i, entryList.get(i).getKey()));

        for (String barcode : barcodes) {
            String txtFile = barcode + ".txt";
            File fileToMoveTxt = new File(conf.getTempDir() + "/" + txtFile);
            String xmlFile = barcode + ".marc.xml";
            File fileToMoveXml = new File(conf.getTempDir() + "/"  + xmlFile);
            File moveToDir = new File(conf.getTempDir().getAbsolutePath() + subDir);

            try {
                FileUtils.createDirectory(moveToDir.toString());
            } catch (IOException e) {
                log.error("Directory '{}' was not created.", moveToDir);
//                e.printStackTrace();
            }
            File moveToTxt = new File(moveToDir + "/" + txtFile);
            File moveToXml = new File(moveToDir + "/" + xmlFile);
            try {
                FileUtils.moveFile(fileToMoveTxt, moveToTxt);
            } catch (Exception e){
                log.error("The file '{}' could not be moved", fileToMoveTxt);
                log.debug(e.toString());
            }
            try {
                FileUtils.moveFile(fileToMoveXml, moveToXml);
            } catch (Exception e) {
                log.error("The file '{}' could not be moved", fileToMoveXml);
                log.debug(e.toString());
            }
        }
    }
}
