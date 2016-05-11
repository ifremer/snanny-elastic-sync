package fr.ifremer.sensornanny.sync.dao.rest;

/**
 * Sensor not found exception
 */
public class SensorNotFoundException extends RuntimeException {

    /**
     * Declare a Sensor not found exception
     *
     * @param message name of the resource
     */
    public SensorNotFoundException(String message) {
        super(message);
    }

    /**
     * Declare a Sensor not found exception
     *
     * @param message name of the resource
     */
    public SensorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
