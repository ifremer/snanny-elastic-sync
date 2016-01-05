package fr.ifremer.sensornanny.sync;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.elasticsearch.common.util.concurrent.ThreadFactoryBuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dto.owncloud.Activity;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;
import fr.ifremer.sensornanny.sync.processor.IElasticProcessor;
import fr.ifremer.sensornanny.sync.reader.FifoReader;
import fr.ifremer.sensornanny.sync.reader.IOwncloudReader;
import fr.ifremer.sensornanny.sync.util.DateUtils;

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
                    default:
                        showHelp(options);
                        break;
                }
            }

        } catch (ParseException e) {
            showHelp(options);
        }
        System.exit(0);
    }

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

    private static void executeWithOptionFailure() {
        long time = System.currentTimeMillis();
        LOGGER.info(String.format("Sync Failed sync Owncloud files"));

        IOwncloudReader owncloudReader = injector.getInstance(IOwncloudReader.class);
        Map<FileInfo, List<Activity>> activities = owncloudReader.getFailedSyncActivities();

        executeSynchronization(activities);

        long timeTook = (System.currentTimeMillis() - time) / 1000;
        LOGGER.info(String.format("Sync %d files modified, Time %ds", activities.size(), timeTook));
    }

    private static void executeWithOptionRange(Option option) throws ParseException {
        Date from = parseDate(option.getValue(0));
        Date to = parseDate(option.getValue(1));
        execute(from, to);
    }

    private static void showHelp(Options options) {
        new HelpFormatter().printHelp("java -jar elastic-sync.jar [-s||-f] ", options);
        System.exit(0);
    }

    private static Date parseDate(String date) {
        return DateUtils.parse(date);
    }

    protected static void execute(Date from, Date to) {
        long time = System.currentTimeMillis();
        LOGGER.info(String.format("Sync Owncloud files modified between %s and %s", from, to));

        IOwncloudReader owncloudReader = injector.getInstance(IOwncloudReader.class);
        Map<FileInfo, List<Activity>> activities = owncloudReader.getActivities(from, to);

        executeSynchronization(activities);

        long timeTook = (System.currentTimeMillis() - time) / 1000;
        LOGGER.info(String.format("Sync %d files modified, Time %ds", activities.size(), timeTook));
    }

    private static void executeSynchronization(Map<FileInfo, List<Activity>> activities) {
        LOGGER.info(String.format("%d files to synchronize ", activities.size()));
        if (activities.size() > 0) {
            FifoReader fifoReader = new FifoReader(activities.entrySet().iterator());

            LOGGER.info(String.format("Start %d executors", Config.threadNumbers()));
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("T-%d").build();
            ExecutorService executors = Executors.newFixedThreadPool(Config.threadNumbers(), namedThreadFactory);
            Entry<FileInfo, List<Activity>> read;
            IElasticProcessor processor = injector.getInstance(IElasticProcessor.class);
            while ((read = fifoReader.read()) != null) {
                executors.execute(processor.of(read.getKey(), read.getValue()));
            }
            executors.shutdown();
            while (!executors.isTerminated()) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
