package fr.ifremer.sensornanny.sync.dao.rest;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class OwncloudRestErrorHandler implements ResponseErrorHandler {

    /**
     * Resource called by request
     */
    private Resource resource;

    private OwncloudRestErrorHandler(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return !HttpStatus.OK.equals(response.getStatusCode());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
            throw new DataNotFoundException(resource.getName() + " not found");
        }
    }

    public static OwncloudRestErrorHandler of(Resource resource) {
        return new OwncloudRestErrorHandler(resource);
    }

    public static OwncloudRestErrorHandler of(String resource) {
        return of(new Resource() {

            @Override
            public String getName() {
                return resource;
            }
        });
    }

}
