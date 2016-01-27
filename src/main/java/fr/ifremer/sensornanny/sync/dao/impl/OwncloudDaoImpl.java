package fr.ifremer.sensornanny.sync.dao.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;
import fr.ifremer.sensornanny.sync.dao.rest.OwncloudRestErrorHandler;
import fr.ifremer.sensornanny.sync.dto.owncloud.Content;
import fr.ifremer.sensornanny.sync.dto.owncloud.FileSizeInfo;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatus;
import fr.ifremer.sensornanny.sync.dto.owncloud.IndexStatusResponse;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
import fr.ifremer.sensornanny.sync.dto.owncloud.SensorMLAncestors;

/**
 * Implementation of the OwncloudApi
 * 
 * @author athorel
 *
 */
public class OwncloudDaoImpl implements IOwncloudDao {

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
     * @param uri URI to acces with
     * @param clazz returned class
     * @return result of the get action
     */
    private <T> T get(URI uri, Class<T> clazz, HttpMessageConverter<?> converter, String resourceName) {
        RestTemplate template = init(converter);
        if (resourceName != null) {
            template.setErrorHandler(OwncloudRestErrorHandler.of(resourceName));
        }

        ResponseEntity<T> response = template.exchange(uri, HttpMethod.GET, createEntity(), clazz);

        return response.getBody();

    }

    /**
     * Allow to call rest template using headers with authentication
     * 
     * @param uri URI to acces with
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
    public List<String> getAncestors(String uuid) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.smlEndpoint() + uuid + ANCESTORS).build().encode().toUri();
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
