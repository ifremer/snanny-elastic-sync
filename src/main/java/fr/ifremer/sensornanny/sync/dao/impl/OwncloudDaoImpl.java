package fr.ifremer.sensornanny.sync.dao.impl;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;
import fr.ifremer.sensornanny.sync.dao.rest.OwncloudRestErrorHandler;
import fr.ifremer.sensornanny.sync.dto.owncloud.*;
import fr.ifremer.sensornanny.sync.report.ReportManager;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of the OwncloudApi
 *
 * @author athorel
 */
public class OwncloudDaoImpl implements IOwncloudDao {

    private static final Logger LOGGER = Logger.getLogger(OwncloudDaoImpl.class.getName());
    // OM endpoint
    private static final String OM_PATH = "/om/";

    // Data endpoint
    private static final String ANCESTORS = "/ancestors";

    // Auth
    private static final String BASIC_HEADER = "Basic ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Get informations from owncloud
    private static final String TO_PARAMETER = "to";
    private static final String FROM_PARAMETER = "from";
    private static final String FILES_SERVICES = "/files";
    private static final String FILES_FAILURE_SERVICES = "/lastfailure";
    private static final String BEGINTIME_PARAMETER = "beginTime";
    private static final String ENDTIME_PARAMETER = "endTime";

    @Override
    public List<OwncloudSyncModel> getActivities(Date from, Date to) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + FILES_SERVICES)
                // From
                .queryParam(FROM_PARAMETER, from.getTime() / 1000)
                        // To
                .queryParam(TO_PARAMETER, to.getTime() / 1000)
                        // GetUri
                .build().encode().toUri();

        OwncloudSyncModel[] activities = get(uri, OwncloudSyncModel[].class, new GsonHttpMessageConverter(), null);
        return activities != null ? Arrays.asList(activities) : new ArrayList<>();
    }

    @Override
    public List<OwncloudSyncModel> getFailedActivities() {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + FILES_FAILURE_SERVICES)
                // GetUri
                .build().encode().toUri();

        OwncloudSyncModel[] activities = get(uri, OwncloudSyncModel[].class, new GsonHttpMessageConverter(), null);
        return activities != null ? Arrays.asList(activities) : new ArrayList<>();
    }

    @Override
    public Content getOM(String uuid) {

        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint()).path(OM_PATH)
                // With id
                .path(uuid)
                        // GetUri
                .build().encode().toUri();

        return get(uri, Content.class, new GsonHttpMessageConverter(), null);
    }

    /**
     * Allow to call rest template using headers with authentication
     *
     * @param uri   URI to acces with
     * @param clazz returned class
     * @return result of the get action
     */
    private <T> T get(URI uri, Class<T> clazz, HttpMessageConverter<?> converter, String resourceName) {
        RestTemplate template = init(converter);
        if (resourceName != null) {
            template.setErrorHandler(OwncloudRestErrorHandler.of(resourceName));
        }

        LOGGER.info("Call " + uri);
        ReportManager.log("Call " + uri);
        ResponseEntity<T> response = template.exchange(uri, HttpMethod.GET, createEntity(), clazz);

        return response.getBody();

    }

    /**
     * Allow to call rest template using headers with authentication
     *
     * @param uri   URI to acces with
     * @param clazz returned class
     * @return result of the get action
     */
    private <T, R> ResponseEntity<R> post(URI uri, HttpMessageConverter<?> converter, T data, Class<R> clazz) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION_HEADER, BASIC_HEADER + Config.owncloudCredentials());

        HttpEntity<T> httpEntity = new HttpEntity<>(data, headers);

        RestTemplate restTemplate = init(converter);
        return restTemplate.exchange(uri, HttpMethod.POST, httpEntity, clazz);
    }

    private RestTemplate init(HttpMessageConverter<?> converter) {
        RestTemplate restTemplate = new RestTemplate();

        if (converter != null) {
            restTemplate.setMessageConverters(Arrays.asList(converter));
        }

        return restTemplate;

    }

    private HttpEntity<?> createEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION_HEADER, BASIC_HEADER + Config.owncloudCredentials());
        return new HttpEntity<>(headers);
    }

    @Override
    public String getSML(String uuid) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.smlEndpoint() + uuid).build().encode().toUri();
        return get(uri, String.class, null, "SML " + uri.toString());
    }

    @Override
    public String getSML(String uuid, Date startTime, Date endTime) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Config.smlEndpoint() + uuid);
        if (startTime != null) {
            builder.queryParam("startTime", startTime.getTime());
        }
        if (endTime != null) {
            builder.queryParam("endTime", endTime.getTime());
        }
        URI uri = builder.build().encode().toUri();
        return get(uri, String.class, null, "SML " + uri.toString());
    }

    @Override
    public InputStream getResultData(String uuid) throws DataNotFoundException {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint()).path(OM_PATH).path(uuid).path("/stream")
                .build().encode().toUri();
        try {
            URLConnection urlConnection = uri.toURL().openConnection();
            urlConnection.setRequestProperty(AUTHORIZATION_HEADER, BASIC_HEADER + Config.owncloudCredentials());
            return urlConnection.getInputStream();
        } catch (Exception e) {
            // Nothing todo
            throw new DataNotFoundException("Unable to find file from uuid " + uuid, e);
        }

    }

    @Override
    public FileSizeInfo getResultFileSize(String uuid) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint()).path(OM_PATH).path(uuid).path("/filesize")
                // GetUri
                .build().encode().toUri();

        return get(uri, FileSizeInfo.class, new GsonHttpMessageConverter(), null);
    }

    @Override
    public List<String> getAncestors(String uuid, Date beginPosition, Date endPosition) {
        Long beginTime = beginPosition != null ? beginPosition.getTime() : null;
        Long endTime = endPosition != null ? endPosition.getTime() : null;
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.smlEndpoint() + uuid + ANCESTORS)
                // beginTime
                .queryParam(BEGINTIME_PARAMETER, beginTime)
                        // endTimeTime
                .queryParam(ENDTIME_PARAMETER, endTime)
                .build().encode().toUri();
        SensorMLAncestors result = get(uri, SensorMLAncestors.class, new GsonHttpMessageConverter(), "SML " + uri
                .toString());
        return result.getAncestors();
    }

    @Override
    public void updateIndexStatus(IndexStatus indexStatus) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint()).path(OM_PATH).build().encode().toUri();
        post(uri, new GsonHttpMessageConverter(), indexStatus, IndexStatusResponse.class);
    }

}
