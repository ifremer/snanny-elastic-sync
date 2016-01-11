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

    private static final String ANCESTORS = "/ancestors";
    private static final String OM_INFO = "/ominfo/";
    private static final String OM_RESULT = "/omresult/";
    private static final String FILENAME_PARAMETER = "filename";
    private static final String BASIC_HEADER = "Basic ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ID_PARAMETER = "id";
    private static final String CONTENT_SERVICES = "/content";
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

        OwncloudSyncModel[] activities = get(uri, OwncloudSyncModel[].class, new GsonHttpMessageConverter());
        return activities != null ? Arrays.asList(activities) : new ArrayList<>();
    }

    @Override
    public List<OwncloudSyncModel> getFailedActivities() {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + FILES_FAILURE_SERVICES)
                // GetUri
                .build().encode().toUri();

        OwncloudSyncModel[] activities = get(uri, OwncloudSyncModel[].class, new GsonHttpMessageConverter());
        return activities != null ? Arrays.asList(activities) : new ArrayList<>();
    }

    @Override
    public Content getContent(Long id) {

        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + CONTENT_SERVICES)
                // With id
                .queryParam(ID_PARAMETER, id)
                // GetUri
                .build().encode().toUri();

        return get(uri, Content.class, new GsonHttpMessageConverter());
    }

    /**
     * Allow to call rest template using headers with authentication
     * 
     * @param uri URI to acces with
     * @param clazz returned class
     * @return result of the get action
     */
    private <T> T get(URI uri, Class<T> clazz, HttpMessageConverter<?> converter) {
        RestTemplate template = init(converter);
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
        return get(uri, String.class, null);
    }

    @Override
    public InputStream getResultData(Long idOM, String resultFileName) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + OM_RESULT + idOM).queryParam(
                FILENAME_PARAMETER, resultFileName).build().encode().toUri();
        try {
            URLConnection urlConnection = uri.toURL().openConnection();
            urlConnection.setRequestProperty(AUTHORIZATION_HEADER, BASIC_HEADER + Config.owncloudCredentials());
            return urlConnection.getInputStream();
        } catch (Exception e) {
            // Nothing todo
        }
        return null;

    }

    @Override
    public FileSizeInfo getFileSize(Long idOM, String resultFileName) {

        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + OM_INFO + idOM)
                // With id
                .queryParam(FILENAME_PARAMETER, resultFileName)
                // GetUri
                .build().encode().toUri();

        return get(uri, FileSizeInfo.class, new GsonHttpMessageConverter());
    }

    @Override
    public List<String> getAncestors(String uuid) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.smlEndpoint() + uuid + ANCESTORS).build().encode().toUri();
        SensorMLAncestors result = get(uri, SensorMLAncestors.class, new GsonHttpMessageConverter());
        return result.getAncestors();
    }

    @Override
    public void updateIndexStatus(IndexStatus indexStatus) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Config.owncloudEndpoint() + "/om").build().encode().toUri();
        post(uri, new GsonHttpMessageConverter(), indexStatus, IndexStatusResponse.class);
    }

}
