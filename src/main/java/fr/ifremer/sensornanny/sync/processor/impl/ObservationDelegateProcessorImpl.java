package fr.ifremer.sensornanny.sync.processor.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.JAXBElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.cache.impl.SensorMLCacheManager;
import fr.ifremer.sensornanny.sync.cache.impl.TermCacheManager;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.converter.XmlOMDtoConverter;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Ancestor;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Coordinates;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.dto.model.Axis;
import fr.ifremer.sensornanny.sync.dto.model.OM;
import fr.ifremer.sensornanny.sync.dto.model.OMResult;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import fr.ifremer.sensornanny.sync.dto.model.Term;
import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileInfo;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;
import fr.ifremer.sensornanny.sync.parse.ParseUtil;
import fr.ifremer.sensornanny.sync.parse.impl.OMParser;
import fr.ifremer.sensornanny.sync.parse.impl.SensorMLParser;
import fr.ifremer.sensornanny.sync.parse.observations.impl.MomarObservationParser;
import fr.ifremer.sensornanny.sync.processor.IDelegateProcessor;
import fr.ifremer.sensornanny.sync.util.UrlUtils;
import fr.ifremer.sensornanny.sync.writer.IElasticWriter;
import net.opengis.sos.v_2_0.InsertObservationType;
import net.opengis.sos.v_2_0.InsertObservationType.Observation;

/**
 * Implementation of observation delegate processor
 * 
 * @author athorel
 *
 */
public class ObservationDelegateProcessorImpl implements IDelegateProcessor {

    private static final String FORMAT_ZERO_PAD = "%010d";

    private static final String ID_SEPARATOR = "-";

    private static final String TEMA_QUERY_PARAMETER = "tema";

    @Inject
    IOwncloudDao ownCloudDao;

    @Inject
    IElasticWriter elasticWriter;

    @Inject
    OMParser omParser;

    @Inject
    SensorMLParser smlParser;

    @Inject
    XmlOMDtoConverter xmlONDtoConverter;

    @Inject
    SensorMLCacheManager cacheSystem;

    @Inject
    TermCacheManager cacheTerm;

    @Inject
    MomarObservationParser momarParser;

    @Inject
    ObservationDataManager observationDataManager;

    @Override
    public void execute(FileInfo fileInfo, boolean isDeleted) {
        if (isDeleted) {
            onDelete(fileInfo);
        } else {
            onIndex(fileInfo);
        }
    }

    /**
     * Called on delete file
     */
    private void onDelete(FileInfo fileInfo) {
        try {
            Content content = ownCloudDao.getContent(fileInfo.getFileId());
            JAXBElement<InsertObservationType> result = ParseUtil.parse(omParser, content.getContent());
            List<Observation> observations = result.getValue().getObservation();
            // Suppression de l'index
            for (Observation observation : observations) {
                String uuid = observation.getOMObservation().getIdentifier().getValue();

                elasticWriter.delete(uuid);
            }
        } catch (Exception e) {
            String uuid = getFileNameWithoutExt(fileInfo.getFilePath());
            elasticWriter.delete(uuid);
        }
    }

    /**
     * Called on Index
     * 
     * @param fileInfo fileInfo to index
     */
    private void onIndex(FileInfo fileInfo) {
        IndexStatus indexStatus = new IndexStatus();
        indexStatus.setFileId(fileInfo.getFileId());
        indexStatus.setIndexedObservations(0);
        indexStatus.setTime(System.currentTimeMillis());
        try {
            Content content = ownCloudDao.getContent(fileInfo.getFileId());
            JAXBElement<InsertObservationType> result = ParseUtil.parse(omParser, content.getContent());
            List<OM> observations = xmlONDtoConverter.fromXML(result);
            final int syncModulo = Config.syncModulo();
            // Suppression de l'index
            for (OM observation : observations) {

                indexStatus.setUuid(observation.getIdentifier());
                // Get the procedure
                String procedure = observation.getProcedure();
                String systemUuid = new File(procedure).getName();
                List<Ancestor> ancestors = getAncestors(systemUuid);

                // Retrieve the observation result and parse it
                OMResult observationResult = observation.getResult();
                // Retrieve the sensorML
                SensorML sensor = cacheSystem.getData(systemUuid);
                Axis axis = sensor.getCoordinate();

                observationDataManager.readData(fileInfo.getFileId(), observationResult, new Consumer<TimePosition>() {

                    @Override
                    public void accept(TimePosition timePosition) {

                        if (timePosition.getRecordNumber() % syncModulo == 0) {

                            ObservationJson item = new ObservationJson();
                            String identifier = observation.getIdentifier();
                            item.setUuid(identifier);
                            item.setAncestors(ancestors);
                            item.setName(observation.getName());
                            item.setResult(observationResult.getUrl());

                            item.setDescription(observation.getDescription());
                            item.setUpdateTimestamp(observation.getUpdateDate());

                            // Get the timestamp
                            item.setResultTimestamp(timePosition.getDate());

                            Coordinates coordinates = new Coordinates();
                            if (timePosition.getLatitude() != null && timePosition.getLongitude() != null) {
                                coordinates.setLat(timePosition.getLatitude());
                                coordinates.setLon(timePosition.getLongitude());
                                item.setDepth(timePosition.getDepth());
                            } else if (axis != null) {
                                coordinates.setLat(axis.getLat());
                                coordinates.setLon(axis.getLon());
                                item.setDepth(axis.getDep());
                            }
                            item.setCoordinates(coordinates);
                            String uuid = new StringBuilder(identifier).append(ID_SEPARATOR).append(String.format(
                                    FORMAT_ZERO_PAD, timePosition.getRecordNumber())).toString();
                            if (elasticWriter.write(uuid, item)) {
                                indexStatus.increaseIndexed();
                            }
                        }
                    }
                });

                ownCloudDao.updateIndexStatus(indexStatus);
            }
        } catch (Exception e) {
            indexStatus.setStatus(false);
            indexStatus.setMessage(e.getMessage());
            ownCloudDao.updateIndexStatus(indexStatus);
        }

    }

    /**
     * Allow to fil the ancestor
     * 
     * @param item item to fill
     * @param systemUuid system UUID
     * @throws Exception Exception while getting system
     */
    protected List<Ancestor> getAncestors(String systemUuid) throws Exception {

        List<Ancestor> systemAncestors = new ArrayList<>();
        // Get the first ancestor
        if (systemUuid != null) {

            // Get the ancestors
            List<String> parentAncestors = ownCloudDao.getAncestors(systemUuid);
            if (CollectionUtils.isNotEmpty(parentAncestors)) {
                for (String parentAncestor : parentAncestors) {
                    SensorML compSensorML = cacheSystem.getData(parentAncestor);
                    Ancestor ancestor = new Ancestor();
                    ancestor.setUuid(compSensorML.getUuid());
                    ancestor.setDescription(compSensorML.getDescription());
                    ancestor.setName(compSensorML.getName());
                    ancestor.setTerms(getTerms(compSensorML.getTerms()));
                    ancestor.setKeywords(compSensorML.getKeywords());
                    systemAncestors.add(ancestor);
                }
            }
            // Get the data from direct ancestor
            SensorML system = cacheSystem.getData(systemUuid);
            Ancestor ancestor = new Ancestor();
            ancestor.setUuid(systemUuid);
            ancestor.setDescription(system.getDescription());
            ancestor.setName(system.getName());
            ancestor.setTerms(getTerms(system.getTerms()));
            ancestor.setKeywords(system.getKeywords());
            systemAncestors.add(ancestor);
        }
        return systemAncestors;
    }

    /**
     * This method allow to extract terms and transform references to real term
     * 
     * @param termsHrefs list of references {@Example http://www.ifremer.fr/tematres/vocab/index.php?tema=125}
     * @return list of real terms
     */
    private List<String> getTerms(List<String> termsHrefs) {
        List<String> terms = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(termsHrefs)) {
            for (String href : termsHrefs) {
                // Retrieve the termId
                String termId = UrlUtils.parse(href, TEMA_QUERY_PARAMETER);
                if (termId != null) {
                    Term term = cacheTerm.getData(termId);
                    if (term != null) {
                        terms.add(term.getLabel());
                    }
                }
            }
        }
        return terms;
    }

    private String getFileNameWithoutExt(String filePath) {
        String filename = new File(filePath).getName();
        // Remove extensions
        return StringUtils.removeEnd(filename, ".xml");
    }

}
