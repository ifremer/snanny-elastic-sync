package fr.ifremer.sensornanny.sync.manager;

import java.util.Set;
import org.springframework.util.Assert;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.io.DataFileReader;
import fr.ifremer.sensornanny.sync.io.OwnCloudFileReader;

/**
 * A manager used to recover the {@link DataFileReader} implementation based on the filename extension.
 * The specific Readers are linked by Guice Multibinding
 * @see fr.ifremer.sensornanny.sync.ElasticSyncModule
 * @author gpagniez
 */
public class DataFileManager {

    @Inject
    private Set<DataFileReader> readers;

    @Inject
    private OwnCloudFileReader standard;

    /**
     * Recover the reader instance from the given filename
     * @param fileName - a full filename
     * @return if any specific reader is linked to the extension with {@link DataFileReader#getExtension()}, it returns
     * the proper reader. If the extension doesn't match any of thoses, it return the {@link OwnCloudFileReader}
     */
    public DataFileReader getReader(String fileName){

        Assert.hasText(fileName, "Invalid parameter : fileName should not be empty");
        String extension = fileName.substring(fileName.lastIndexOf("."));

        return readers.stream()
               .filter(x -> extension.equals(x.getExtension()))
               .findFirst().orElse(standard);
    }
}
