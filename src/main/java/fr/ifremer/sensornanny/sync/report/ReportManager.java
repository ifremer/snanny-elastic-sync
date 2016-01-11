package fr.ifremer.sensornanny.sync.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.util.DateUtils;

/**
 * Class that allow to write report manager to log file
 * 
 * @author athorel
 *
 */
public class ReportManager {

    private static final String NEW_LINE_TAB = "\n\t";
    private static final Logger LOGGER = Logger.getLogger(ReportManager.class.getName());
    private static final String SNANNY_ELASTIC_SYNC_LOG = "snanny-elastic-sync.log";
    private Writer writer;
    private File logFile;
    private static ReportManager instance;

    public synchronized static void log(String data) {
        if (instance == null) {
            instance = new ReportManager();
        }
        instance.internalLog(data);
    }

    public synchronized static void err(String message, Throwable e) {
        if (instance == null) {
            instance = new ReportManager();
        }
        StringBuilder err = new StringBuilder(message);
        if (e != null) {
            err.append("\nCaused by : ").append(e.getMessage());
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                err.append(NEW_LINE_TAB).append(stackTraceElement.toString());
            }
        }
        instance.internalLog(err.toString());
    }

    /**
     * Constructor allow initialise file writer
     */
    private ReportManager() {
        File outputFolder = Config.outputFolder();
        if (outputFolder == null || outputFolder.canWrite()) {
            logFile = new File(outputFolder, SNANNY_ELASTIC_SYNC_LOG);
            try {
                writer = new BufferedWriter(new FileWriter(logFile, true));
                LOGGER.info(String.format("Create sync result log file : %s", logFile.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.warning(String.format("Unable to write to %s - log will be write in default log", logFile));
            }
        } else {
            LOGGER.warning(String.format(
                    "The folder %s doesn't exist or is not writable - log will be write in default log", outputFolder));
        }

    }

    /**
     * Method that allow to internal log data
     * 
     * @param data data to log
     */
    private void internalLog(String data) {
        if (writer != null) {
            try {
                String formatDateTime = DateUtils.formatDateTime(new Date());
                writer.append(String.format("[%s] - %s\n", formatDateTime, data));
            } catch (IOException e) {
                LOGGER.info(data);
            }
        } else {
            LOGGER.info(data);
        }
    }

    /**
     * Method that release writer
     */
    public static void release() {
        if (instance != null) {
            IOUtils.closeQuietly(instance.writer);
        }
    }

}
