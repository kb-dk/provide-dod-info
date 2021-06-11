package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Extracts the MODS metadata from alma.
 *
 * Usage:
 * dk.kb.provide-dod-info.AlmaExtract /PATH/TO/provide-dod-info.yml [ISBN]+
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
            System.err.println("Needs at least one argument: ");
            System.err.println(" * The configuration file.");
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
//            almaRetriever.retrieveAlmaMetadataForBooks(workbook);
            almaRetriever.retrieveAlmaMetadataForFiles(workbook);
            try {
                FileOutputStream out = new FileOutputStream(conf.getOutDir()+"/AlmaExtractResult.xlsx");
                workbook.write(out);
                out.close();
                System.out.println("AlmaExtractResult.xlsx written successfully on disk.");
            }
            catch (Exception e) {
                log.error("Failed to write Excel-file.");
                e.printStackTrace();
            }

        } catch (Exception e ) {
            throw new IllegalStateException("Failure to retrieve alma metadata in MODS.", e);
        }
    }

    /**
     * Retrieves the different kinds of metadata for given ISBN number.
     * @param almaMetadataRetriever The Alma metadata retriever.
     * @param isbn The ISBN number of the record to retrieve the metadata for.
     */
//    protected static void retrieveMetadataForIsbn(AlmaMetadataRetriever almaMetadataRetriever,
//                                                  String isbn) throws IOException {
//        log.info("Retrieving the metadata for ISBN: '" + isbn + "'");
//        File modsMetadataFile = new File(outputDir, isbn + ".mods.xml");
//        try (OutputStream out = new FileOutputStream(modsMetadataFile)) {
//            almaMetadataRetriever.retrieveMetadataForISBN(isbn, out);
//            //Saved to modsMetadataFile
//        }
//        log.info("Metadata for ISBN '" + isbn + "' can be found at: " + modsMetadataFile.getAbsolutePath());
//    }
}
