package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import dk.kb.provide_dod_info.utils.DateUtils;
import dk.kb.provide_dod_info.utils.ExcelUtils;
import dk.kb.provide_dod_info.utils.FileUtils;
import dk.kb.provide_dod_info.utils.ZipUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.zip.ZipOutputStream;


/**
 * Extracts MARC metadata from alma.
 * Add specific extracted metadata to Excel file
 * Sort output data in directories
 * Zip data to out.zip
 *
 * Usage:
 * dk.kb.provide-dod-info.AlmaExtract /PATH_TO/provide-dod-info.yml
 *
 */
public class AlmaExtract {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaExtract.class);

    /** The output directory.*/
    protected static File outputDir = new File(".");

    /**
     * Requires one argument: the configuration file.
     * @param args The arguments.
     */
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Needs the configuration file 'provide-dod-info.yml' as argument.");
            System.exit(-1);
        }

        String confPath = args[0];
        File confFile = new File(confPath);
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            Configuration conf = Configuration.createFromYAMLFile(confFile);
            HttpClient httpClient = new HttpClient();

            AlmaMetadataRetriever almaMetadataRetriever = new AlmaMetadataRetriever(conf, httpClient);
            AlmaRetriever almaRetriever = new AlmaRetriever(conf, almaMetadataRetriever);
            almaRetriever.retrieveAlmaMetadataForFiles(workbook);

            DataHandler dataHandler = new DataHandler(conf);
            String excelFile = conf.getTempDir().getName() + "/"  + conf.getOutFileName();
            File existingExcelFile = FileUtils.getExistingFile(excelFile);
            String absolutePathExcelFile = existingExcelFile.getAbsolutePath();

//                dataHandler.addReadMeIDE();
            dataHandler.addReadMe();

            Map<String, String> values;
            if (StringUtils.isNotEmpty(absolutePathExcelFile)) {
                values = ExcelUtils.getValues(absolutePathExcelFile);
                dataHandler.sortDirectories(values);
            }

            String yyyyMMdd = DateUtils.getDate();
            String sourceDir = conf.getTempDir().getAbsolutePath();
            String outZipFilename = "/DOD_OCR_korpus_" + yyyyMMdd + ".zip";
            FileOutputStream fos = new FileOutputStream(conf.getOutDir().getAbsolutePath() + outZipFilename);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File dirToZip = new File(sourceDir);

            ZipUtils.zipFile(dirToZip, dirToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
            FileUtils.deleteDirectory( conf.getTempDir());
            log.debug("Output ready");

        } catch (Exception e ) {
            throw new IllegalStateException("Something went wrong. Check log for errors", e);
        }
    }
}
