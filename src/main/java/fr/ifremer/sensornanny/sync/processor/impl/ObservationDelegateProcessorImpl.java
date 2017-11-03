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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of observation delegate processor
 *
 * @author athorel
 */
public class ObservationDelegateProcessorImpl implements IDelegateProcessor {

    private static final String DEPLOYMENT_ID_SEPARATOR = "_";

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
                // First delete observations and systems
                elasticWriter.delete(observation.getIdentifier());
                // Get the procedure
                String procedure = observation.getProcedure();
                String systemUuid = new File(procedure).getName();

                // Retrieve the sensorML
                final SensorML sensor = cacheSystem.getData(systemUuid, observation.getBeginPosition(),
                        observation.getEndPosition());

                if(sensor != null){
                    // Retrieve the observation result and parse it
                    OMResult observationResult = observation.getResult();

                    final List<Ancestor> ancestors = getAncestors(systemUuid, observation.getBeginPosition(),
                            observation.getEndPosition());

                    // Retrieve the first sensor in ancestors with coordinates
                    final Axis axis = getFirstValidAxisInSML(ancestors, observation.getBeginPosition(), observation.getEndPosition());

                    boolean hasData = observationDataManager.readData(fileInfo.getUuid(), observationResult, new Consumer<TimePosition>() {

                        private SensorML usedSensor = sensor;
                        private Axis usedAxis = axis;

                        @Override
                        public void accept(TimePosition timePosition) {

                            String identifier = observation.getIdentifier();

                            if(timePosition != null) {

                                // Si le system n'a pas été trouvé via les dates de
                                // l'observation, on essaye de le
                                // trouver via la date de la première position
                                if (usedSensor == null) {
                                    usedSensor = cacheSystem.getData(systemUuid, timePosition.getDate(),
                                            timePosition.getDate());
                                }
                                if (usedSensor == null) {
                                    System.out.println("Unable to find SML " + systemUuid);
                                    throw new SensorNotFoundException("Unable to find SML " + systemUuid);
                                }
                                if ((timePosition.getLatitude() == null || timePosition.getLongitude() == null) && usedAxis == null) {
                                    usedAxis = getFirstValidAxisInSML(ancestors, timePosition.getDate(),
                                            timePosition.getDate());
                                }

                                ObservationJson item = new ObservationJson();
                                item.setUuid(identifier);
                                item.setAncestors(ancestors);
                                item.setName(observation.getName());
                                item.setResult(observationResult.getUrl());
                                item.setAuthor(content.getUser());

                                item.setDescription(observation.getDescription());
                                item.setUpdateTimestamp(observation.getUpdateDate());

                                // Get the timestamp
                                item.setResultTimestamp(timePosition.getDate());

                                item.setDeploymentId(String.valueOf(Objects.hash(identifier, usedSensor.getUuid(),
                                        usedSensor.getStartTime(), usedSensor.getEndTime())));

                                if (timePosition.getLatitude() != null && timePosition.getLongitude() != null) {
                                    item.setDepth(timePosition.getDepth());
                                    item.setCoordinates(timePosition.getLatitude() + "," + timePosition.getLongitude());
                                } else if (usedAxis != null) {
                                    item.setDepth(usedAxis.getDep());
                                    item.setCoordinates(usedAxis.getLat().doubleValue() + "," + usedAxis.getLon().doubleValue());
                                }

                                // Set permissions
                                item.setPermission(permission);

                                String uuid = identifier + ID_SEPARATOR + String.format(FORMAT_ZERO_PAD, timePosition.getRecordNumber());

                                //write observation
                                if (elasticWriter.write(uuid, item)) {
                                    indexStatus.increaseIndexed();
                                }
                            }
                        }
                    });

                    //index systems
                    List<SensorML> sensors = new ArrayList<>();
                    fetchSystems(sensor, sensors);
                    writeSystems(observation.getIdentifier(), sensors, hasData);

                } else {
                    //sensor not found : indexation for this observation should not occur
                    indexStatus.setStatus(false);
                    String message = String.format("Sensor %s not found, indexation for observation %s is canceled",
                            systemUuid, observation.getIdentifier());
                    ReportManager.log(message);
                    LOGGER.warning(message);
                }

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
     * Complete a list of systems from the given sensor with all its component recursively
     * @param sensor
     * @param systems
     */
    private void fetchSystems(SensorML sensor, List<SensorML> systems) {
        if(sensor.getComponents() != null){
            for(Comp component : sensor.getComponents()){
                String idSystem = component.getHref().substring(component.getHref().lastIndexOf("/") + 1);
                SensorML sml = cacheSystem.getData(idSystem);
                if(sml != null)
                    fetchSystems(sml,systems);
            }
        }
        systems.add(sensor);
    }

    private void writeSystems(String identifier, List<SensorML> sensors, boolean hasData) {
        String uuid = identifier + ID_SEPARATOR + "1" + ID_SEPARATOR;
        for (int i = 0; i< sensors.size(); i++) {
            elasticWriter.write(uuid + String.format(FORMAT_ZERO_PAD, i), sensors.get(i), hasData);
        }
    }

    protected Axis getFirstValidAxisInSML(List<Ancestor> ancestors, Date start, Date end) {
        if (ancestors != null) {
            for (int i = ancestors.size() - 1; i >= 0; i--) {
                Ancestor ancestor = ancestors.get(i);
                SensorML data = cacheSystem.getData(ancestor.getUuid(), start, end);
                if (data != null && data.getCoordinate() != null) {
                    return data.getCoordinate();
                }
            }
        }
        return null;
    }

    /**
     * Allow to fil the ancestor
     *
     * @param systemUuid    system UUID
     * @param beginPosition start date of the deployment
     * @param endPosition   end date of the deployment
     * @throws Exception Exception while getting system
     */
    private List<Ancestor> getAncestors(String systemUuid, Date beginPosition, Date endPosition) throws Exception {

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
                        ancestor.setDeploymentId(createDeploymentId(compSensorML.getUuid(), compSensorML.getStartTime(),
                                compSensorML.getEndTime()));
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
                ancestor.setDeploymentId(createDeploymentId(system.getUuid(), system.getStartTime(), system.getEndTime()));
                systemAncestors.add(ancestor);
            }
        }
        return systemAncestors;
    }

    /**
     * This method allow to extract terms and transform references to real term
     *
     * @param termsHrefs list of references
     *                   {example http://www.ifremer.fr/tematres/vocab/index.php?tema=125}
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

    private String createDeploymentId(String uuid, Long start, Long end) {
        StringBuilder builder = new StringBuilder(uuid);
        builder.append(DEPLOYMENT_ID_SEPARATOR);
        if (start != null) {
            builder.append(start);
        }
        builder.append(DEPLOYMENT_ID_SEPARATOR);
        if (end != null) {
            builder.append(end);
        }
        return builder.toString();
    }

}
