package fr.ifremer.sensornanny.sync;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import fr.ifremer.sensornanny.sync.advice.LogAdvice;
import fr.ifremer.sensornanny.sync.advice.LogAdviceSimpleMatcher;
import fr.ifremer.sensornanny.sync.cache.impl.SensorMLCacheManager;
import fr.ifremer.sensornanny.sync.cache.impl.TermCacheManager;
import fr.ifremer.sensornanny.sync.converter.AbstractXMLConverter;
import fr.ifremer.sensornanny.sync.converter.PermissionsConverter;
import fr.ifremer.sensornanny.sync.converter.XmlOMDtoConverter;
import fr.ifremer.sensornanny.sync.converter.XmlSensorMLDtoConverter;
import fr.ifremer.sensornanny.sync.converter.XmlTimePeriodDateConverter;
import fr.ifremer.sensornanny.sync.dao.IElasticDao;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.ITermDao;
import fr.ifremer.sensornanny.sync.dao.impl.ElasticDaoImpl;
import fr.ifremer.sensornanny.sync.dao.impl.OwncloudDaoImpl;
import fr.ifremer.sensornanny.sync.dao.impl.TermDaoImpl;
import fr.ifremer.sensornanny.sync.parse.impl.OMParser;
import fr.ifremer.sensornanny.sync.parse.impl.SensorMLParser;
import fr.ifremer.sensornanny.sync.processor.ElasticProcessorImpl;
import fr.ifremer.sensornanny.sync.processor.IElasticProcessor;
import fr.ifremer.sensornanny.sync.processor.impl.ObservationDataManager;
import fr.ifremer.sensornanny.sync.processor.impl.ObservationDelegateProcessorImpl;
import fr.ifremer.sensornanny.sync.reader.IOwncloudReader;
import fr.ifremer.sensornanny.sync.reader.impl.OwncloudReaderImpl;
import fr.ifremer.sensornanny.sync.writer.IElasticWriter;
import fr.ifremer.sensornanny.sync.writer.impl.ElasticWriterImpl;

/**
 * Guice Injectors module that allow creation of singleton dependencies
 * 
 * @author athorel
 */
public class ElasticSyncModule extends AbstractModule {

    @Override
    protected void configure() {
        // DAO
        bind(IElasticDao.class).to(ElasticDaoImpl.class);
        bind(IOwncloudDao.class).to(OwncloudDaoImpl.class);
        bind(ITermDao.class).to(TermDaoImpl.class);

        // Reader/Writer
        bind(IOwncloudReader.class).to(OwncloudReaderImpl.class);
        bind(IElasticWriter.class).to(ElasticWriterImpl.class);
        bind(IElasticProcessor.class).to(ElasticProcessorImpl.class);
        bind(ObservationDelegateProcessorImpl.class);

        // Observation data manager - using semaphores to handle low memory usage
        bind(ObservationDataManager.class);

        // AOP
        bindInterceptor(Matchers.not(Matchers.inPackage(AbstractXMLConverter.class.getPackage())),
                new LogAdviceSimpleMatcher(), new LogAdvice());

        // Parsers
        bind(OMParser.class);
        bind(SensorMLParser.class);

        // Converters
        bind(XmlOMDtoConverter.class);
        bind(XmlTimePeriodDateConverter.class);
        bind(XmlSensorMLDtoConverter.class);
        bind(PermissionsConverter.class);

        // CacheManager
        bind(SensorMLCacheManager.class);
        bind(TermCacheManager.class);
    }

}
