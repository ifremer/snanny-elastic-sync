package fr.ifremer.sensornanny.sync.processor.impl;

import com.google.inject.Inject;
import fr.ifremer.sensornanny.observation.parser.TimePosition;
import fr.ifremer.sensornanny.sync.cache.impl.SensorMLCacheManager;
import fr.ifremer.sensornanny.sync.cache.impl.TermCacheManager;
import fr.ifremer.sensornanny.sync.converter.PermissionsConverter;
import fr.ifremer.sensornanny.sync.converter.XmlOMDtoConverter;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.rest.SensorNotFoundException;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Ancestor;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Coordinates;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.Permission;
import fr.ifremer.sensornanny.sync.dto.model.*;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
import fr.ifremer.sensornanny.sync.parse.ParseUtil;
import fr.ifremer.sensornanny.sync.parse.impl.OMParser;
import fr.ifremer.sensornanny.sync.processor.IDelegateProcessor;
import fr.ifremer.sensornanny.sync.report.ReportManager;
import fr.ifremer.sensornanny.sync.util.UrlUtils;
import fr.ifremer.sensornanny.sync.writer.IElasticWriter;
import net.opengis.sos.v_2_0.InsertObservationType;
import org.apache.commons.collections4.CollectionUtils;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of observation delegate processor
 *
 * @author athorel
 */
public class ObservationDelegateProcessorImpl implements IDelegateProcessor {

    private static final Logger LOGGER = Logger.getLogger(ObservationDelegateProcessorImpl.class.getName());

    private static final String FORMAT_ZERO_PAD = "%010d";

    private static final String ID_SEPARATOR = "-";

    private static final String TEMA_QUERY_PARAMETER = "tema";

    @Inject
    private IOwncloudDao ownCloudDao;

    @Inject
    private IElasticWriter elasticWriter;

    @Inject
    private OMParser omParser;

    @Inject
    private XmlOMDtoConverter xmlONDtoConverter;

    @Inject
    private PermissionsConverter permissionConverter;

    @Inject
    private SensorMLCacheManager cacheSystem;

    @Inject
    private TermCacheManager cacheTerm;

    @Inject
    private ObservationDataManager observationDataManager;

    @Override
    public void execute(OwncloudSyncModel model) {
        if (model.isStatus()) {
            onIndex(model);
        } else {
            onDelete(model);
        }
    }

    /**
     * Called on delete file
     */
    private void onDelete(OwncloudSyncModel fileInfo) {
        elasticWriter.delete(fileInfo.getUuid());
    }

    /**
     * Called on Index a new file
     *
     * @param fileInfo fileInfo to index
     */
    private void onIndex(OwncloudSyncModel fileInfo) {
        IndexStatus indexStatus = new IndexStatus();
        indexStatus.setFileId(fileInfo.getFileId());
        indexStatus.setIndexedObservations(0);
        indexStatus.setTime(System.currentTimeMillis());
        try {
            Content content = ownCloudDao.getOM(fileInfo.getUuid());
            JAXBElement<InsertObservationType> result = ParseUtil.parse(omParser, content.getContent());
            List<OM> observations = xmlONDtoConverter.fromXML(result);

            // Get shares informations
            Permission permission = permissionConverter.extractPermissions(content.getShares());

            // Suppression de l'index
            for (OM observation : observations) {
                indexStatus.setUuid(observation.getIdentifier());
                // First delete observations
                elasticWriter.delete(observation.getIdentifier());
                // Get the procedure
                String procedure = observation.getProcedure();
                String systemUuid = new File(procedure).getName();
                List<Ancestor> ancestors = getAncestors(systemUuid, observation.getBeginPosition(), observation.getEndPosition());

                // Retrieve the observation result and parse it
                OMResult observationResult = observation.getResult();
                // Retrieve the sensorML
                final SensorML sensor = cacheSystem.getData(systemUuid, observation.getBeginPosition(), observation.getEndPosition());

                final Axis axis = sensor != null ? sensor.getCoordinate() : null;

                observationDataManager.readData(fileInfo.getUuid(), observationResult, new Consumer<TimePosition>() {

                    SensorML usedSensor = sensor;
                    Axis usedAxis = axis;

                    @Override
                    public void accept(TimePosition timePosition) {

                        // Si le system n'a pas été trouvé via les dates de l'observation, on essaye de le
                        // trouver via la date de la prmière position
                        if (usedSensor == null) {
                            usedSensor = cacheSystem.getData(systemUuid, timePosition.getDate(), timePosition.getDate());
                        }
                        if (usedSensor == null) {
                            throw new SensorNotFoundException("Unable to find SML " + systemUuid);
                        }
                        if (usedAxis == null) {
                            usedAxis = usedSensor.getCoordinate();
                        }

                        ObservationJson item = new ObservationJson();
                        String identifier = observation.getIdentifier();
                        item.setUuid(identifier);
                        item.setAncestors(ancestors);
                        item.setName(observation.getName());
                        item.setResult(observationResult.getUrl());
                        item.setAuthor(content.getUser());

                        item.setDescription(observation.getDescription());
                        item.setUpdateTimestamp(observation.getUpdateDate());

                        // Get the timestamp
                        item.setResultTimestamp(timePosition.getDate());

                        Coordinates coordinates = new Coordinates();
                        if (timePosition.getLatitude() != null && timePosition.getLongitude() != null) {
                            coordinates.setLat(timePosition.getLatitude());
                            coordinates.setLon(timePosition.getLongitude());
                            item.setDepth(timePosition.getDepth());
                        } else if (usedAxis != null) {
                            coordinates.setLat(usedAxis.getLat().doubleValue());
                            coordinates.setLon(usedAxis.getLon().doubleValue());
                            item.setDepth(usedAxis.getDep());
                        }
                        item.setCoordinates(coordinates);

                        // Set permissions
                        item.setPermission(permission);

                        String uuid = new StringBuilder(identifier).append(ID_SEPARATOR).append(String.format(
                                FORMAT_ZERO_PAD, timePosition.getRecordNumber())).toString();
                        if (elasticWriter.write(uuid, item)) {
                            indexStatus.increaseIndexed();
                        }
                    }
                });

                ownCloudDao.updateIndexStatus(indexStatus);
                ReportManager.log(String.format("File %s, Sync succeed - Indexed elements : %d", fileInfo.getName(),
                        indexStatus.getIndexedObservations()));
            }
        } catch (Exception e) {
            indexStatus.setStatus(false);
            indexStatus.setMessage(e.getMessage());
            ReportManager.err(String.format("File %s, Sync failed ", fileInfo.getName()), e);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            ownCloudDao.updateIndexStatus(indexStatus);
        }

    }

    /**
     * Allow to fil the ancestor
     *
     * @param item       item to fill
     * @param systemUuid system UUID
     * @throws Exception Exception while getting system
     */
    protected List<Ancestor> getAncestors(String systemUuid, Date beginPosition, Date endPosition) throws Exception {

        List<Ancestor> systemAncestors = new ArrayList<>();
        // Get the first ancestor
        if (systemUuid != null) {

            // Get the ancestors
            List<String> parentAncestors = ownCloudDao.getAncestors(systemUuid, beginPosition, endPosition);
            if (CollectionUtils.isNotEmpty(parentAncestors)) {
                for (String parentAncestor : parentAncestors) {
                    SensorML compSensorML = cacheSystem.getData(parentAncestor);
                    LOGGER.info("get sensorML ancestor : " + parentAncestor);
                    if (compSensorML != null) {
                        Ancestor ancestor = new Ancestor();
                        ancestor.setUuid(compSensorML.getUuid());
                        ancestor.setDescription(compSensorML.getDescription());
                        ancestor.setName(compSensorML.getName());
                        ancestor.setTerms(getTerms(compSensorML.getTerms()));
                        ancestor.setKeywords(compSensorML.getKeywords());
                        systemAncestors.add(ancestor);
                    } else {
                        LOGGER.warning("Unable to get sensorML ancestor : " + parentAncestor);
                    }
                }
            }
            // Get the data from direct ancestor
            SensorML system = cacheSystem.getData(systemUuid, beginPosition, endPosition);
            if (system != null) {
                Ancestor ancestor = new Ancestor();
                ancestor.setUuid(systemUuid);
                ancestor.setDescription(system.getDescription());
                ancestor.setName(system.getName());
                ancestor.setTerms(getTerms(system.getTerms()));
                ancestor.setKeywords(system.getKeywords());
                systemAncestors.add(ancestor);
            }
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

}
