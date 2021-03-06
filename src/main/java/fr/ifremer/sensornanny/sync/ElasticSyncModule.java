package fr.ifremer.sensornanny.sync;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

import fr.ifremer.sensornanny.sync.advice.LogAdvice;
import fr.ifremer.sensornanny.sync.advice.LogAdviceSimpleMatcher;
import fr.ifremer.sensornanny.sync.cache.impl.SensorMLCacheManager;
import fr.ifremer.sensornanny.sync.cache.impl.TermCacheManager;
import fr.ifremer.sensornanny.sync.converter.AbstractXMLConverter;
import fr.ifremer.sensornanny.sync.converter.PermissionsConverter;
import fr.ifremer.sensornanny.sync.converter.XmlOMDtoConverter;
import fr.ifremer.sensornanny.sync.converter.XmlSensorMLDtoConverter;
import fr.ifremer.sensornanny.sync.converter.XmlTimePeriodDateConverter;
import fr.ifremer.sensornanny.sync.dao.IObservationDao;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.ISystemDao;
import fr.ifremer.sensornanny.sync.dao.ITermDao;
import fr.ifremer.sensornanny.sync.dao.impl.ObservationDaoImpl;
import fr.ifremer.sensornanny.sync.dao.impl.OwncloudDaoImpl;
import fr.ifremer.sensornanny.sync.dao.impl.SystemDaoImpl;
import fr.ifremer.sensornanny.sync.dao.impl.TermDaoImpl;
import fr.ifremer.sensornanny.sync.io.DataFileReader;
import fr.ifremer.sensornanny.sync.io.Bz2ArchiveReader;
import fr.ifremer.sensornanny.sync.io.GzArchiveReader;
import fr.ifremer.sensornanny.sync.io.OwnCloudFileReader;
import fr.ifremer.sensornanny.sync.io.ZipArchiveReader;
import fr.ifremer.sensornanny.sync.parse.ParserManager;
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
 * the
 * Guice Injectors module that allow creation of singleton dependencies
 * 
 * @author athorel
 */
public class ElasticSyncModule extends AbstractModule {

    @Override
    protected void configure() {
        // DAO
        bind(IObservationDao.class).to(ObservationDaoImpl.class).asEagerSingleton();
        bind(ISystemDao.class).to(SystemDaoImpl.class).asEagerSingleton();
        bind(IOwncloudDao.class).to(OwncloudDaoImpl.class).asEagerSingleton();
        bind(ITermDao.class).to(TermDaoImpl.class);

        // Reader/Writer
        bind(IOwncloudReader.class).to(OwncloudReaderImpl.class).asEagerSingleton();
        bind(ElasticMapping.class);
        bind(IElasticWriter.class).to(ElasticWriterImpl.class).asEagerSingleton();
        bind(IElasticProcessor.class).to(ElasticProcessorImpl.class);
        bind(ObservationDelegateProcessorImpl.class);

        // File Reader
        Multibinder<DataFileReader> fileReaderBindings = Multibinder.newSetBinder(binder(),DataFileReader.class);
        fileReaderBindings.addBinding().to(ZipArchiveReader.class);
        fileReaderBindings.addBinding().to(Bz2ArchiveReader.class);
        fileReaderBindings.addBinding().to(GzArchiveReader.class);
        bind(OwnCloudFileReader.class);

        // Observation data manager - using semaphores to handle low memory usage
        bind(ObservationDataManager.class);
        bind(ParserManager.class);

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
