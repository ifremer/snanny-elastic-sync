package fr.ifremer.sensornanny.sync.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import fr.ifremer.sensornanny.sync.config.Config;

public class ReportManager {

    private static final Logger LOGGER = Logger.getLogger(ReportManager.class.getName());

    private static BufferedWriter errorReport;

    static {
        try {
            errorReport = new BufferedWriter(new FileWriter(new File(Config.outputFolder(), "snanny-sync-error.log")));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static synchronized void err(String path, String user, Throwable e) {
        try {
            if (errorReport != null) {
                errorReport.write("Err with file " + path + ":" + user + " Cause : " + e.getMessage());
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
