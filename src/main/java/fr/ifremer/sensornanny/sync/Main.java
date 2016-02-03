package fr.ifremer.sensornanny.sync;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
import fr.ifremer.sensornanny.sync.manager.NodeManager;
import fr.ifremer.sensornanny.sync.parse.ParserManager;
import fr.ifremer.sensornanny.sync.processor.IElasticProcessor;
import fr.ifremer.sensornanny.sync.reader.FifoReader;
import fr.ifremer.sensornanny.sync.reader.IOwncloudReader;
import fr.ifremer.sensornanny.sync.report.ReportManager;
import fr.ifremer.sensornanny.sync.util.DateUtils;

/**
 * Main class for elasticsearch synchronisation
 * 
 * @author athorel
 *
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String RANGE_OPTION = "r";

    private static final String RELAUNCH_FAILURES = "f";

    private static final String SINCE_OPTION = "s";

    private static final String HELP_OPTION = "h";

    private static Injector injector;

    public static void main(String[] args) {

        Options options = new Options();
        Option helpOption = Option.builder(HELP_OPTION).longOpt("help").numberOfArgs(0).desc("print this message")
                .build();
        options.addOption(helpOption);

        Option sinceOption = Option.builder(SINCE_OPTION).longOpt("since").numberOfArgs(1).argName("period").desc(
                "Synchronize from owncloud to elasticsearch since a period").build();
        options.addOption(sinceOption);
        Option rangeOption = Option.builder(RANGE_OPTION).longOpt("range").numberOfArgs(2).argName("from> <to").desc(
                "Synchronize from owncloud to elasticsearch from date to date").build();
        options.addOption(rangeOption);
        Option failOption = Option.builder(RELAUNCH_FAILURES).longOpt("relaunch_failure").numberOfArgs(0).desc(
                "Synchronize from owncloud to elasticsearch the last failed synchronization").build();
        options.addOption(failOption);

        CommandLineParser parser = new DefaultParser();
        try {

            CommandLine result = parser.parse(options, args);
            Option[] resultOptions = result.getOptions();
            if (resultOptions.length != 1) {
                showHelp(options);
            } else {
                injector = Guice.createInjector(new ElasticSyncModule());
                Option option = resultOptions[0];
                switch (option.getOpt()) {

                    case SINCE_OPTION:
                        executeWithOptionSince(option);
                        break;
                    case RANGE_OPTION:
                        executeWithOptionRange(option);
                        break;
                    case RELAUNCH_FAILURES:
                        executeWithOptionFailure();
                        break;
                    default:
                        showHelp(options);
                        break;
                }
            }

        } catch (ParseException e) {
            showHelp(options);
        }

        NodeManager.destroy();
        System.exit(0);
    }

    /**
     * Method that allow to execute option with since parameter
     * 
     * @param option option of since
     * @throws ParseException exception on parsing options
     */
    private static void executeWithOptionSince(Option option) throws ParseException {
        Date to;
        Date from;
        try {
            String sinceValue = option.getValue(0);
            Duration duration = Duration.parse("P" + sinceValue);
            Instant instant = Instant.now();
            to = new Date(instant.getEpochSecond() * 1000);
            from = new Date(((Instant) duration.subtractFrom(instant)).getEpochSecond() * 1000);
            execute(from, to);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getMessage());
        }
    }

    /**
     * Method that allow to execute synchronisation on last failed files
     */
    private static void executeWithOptionFailure() {
        long time = System.currentTimeMillis();
        LOGGER.info(String.format("Sync Failed sync Owncloud files"));
        injector.getInstance(ParserManager.class);
        IOwncloudReader owncloudReader = injector.getInstance(IOwncloudReader.class);
        List<OwncloudSyncModel> activities = owncloudReader.getFailedSyncActivities();

        executeSynchronization(activities);

        long timeTook = (System.currentTimeMillis() - time) / 1000;
        LOGGER.info(String.format("Time %ds", activities.size(), timeTook));
    }

    /**
     * Method that allow to execute option with range parameters (date from and date to)
     * 
     * @param option option of since
     * @throws ParseException exception on parsing options
     */
    private static void executeWithOptionRange(Option option) throws ParseException {
        Date from = DateUtils.parse(option.getValue(0));
        Date to = DateUtils.parse(option.getValue(1));
        execute(from, to);
    }

    /**
     * Diplay the help message
     * 
     * @param options using help message
     */
    private static void showHelp(Options options) {
        new HelpFormatter().printHelp("java -jar elastic-sync.jar [-s||-f] ", options);
        System.exit(0);
    }

    /**
     * Execute treatment with activity
     * 
     * @param from
     * @param to
     */
    protected static void execute(Date from, Date to) {
        long time = System.currentTimeMillis();
        LOGGER.info(String.format("Sync Owncloud files modified between %s and %s", from, to));
        injector.getInstance(ParserManager.class);
        IOwncloudReader owncloudReader = injector.getInstance(IOwncloudReader.class);
        List<OwncloudSyncModel> activities = owncloudReader.getActivities(from, to);

        executeSynchronization(activities);

        long timeTook = (System.currentTimeMillis() - time) / 1000;
        LOGGER.info(String.format("Sync %d files modified, Time %ds", activities.size(), timeTook));
    }

    private static void executeSynchronization(List<OwncloudSyncModel> activities) {
        ReportManager.log(String.format("Synchronize files : %d O&M to sync", activities.size()));
        LOGGER.info(String.format("%d files to synchronize ", activities.size()));
        // Ensure node manager is ok
        try {
            if (activities.size() > 0) {
                FifoReader fifoReader = new FifoReader(activities.iterator());

                new SearchRequestBuilder(NodeManager.getInstance().getClient(), SearchAction.INSTANCE).setIndices(Config
                        .observationsIndex()).setSize(0).setTerminateAfter(1).get();

                LOGGER.info(String.format("Start %d executors", Config.threadNumbers()));
                ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("T-%d").build();
                ExecutorService executors = Executors.newFixedThreadPool(Config.threadNumbers(), namedThreadFactory);
                OwncloudSyncModel read;
                IElasticProcessor processor = injector.getInstance(IElasticProcessor.class);
                while ((read = fifoReader.read()) != null) {
                    executors.execute(processor.of(read));
                }
                executors.shutdown();
                while (!executors.isTerminated()) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            ReportManager.err("ElasticSearch is not listening", e);
            LOGGER.log(Level.SEVERE, "ElasticSearch is not listening", e);
        }
        ReportManager.log("End of sync");
        ReportManager.release();
    }

}
