package fr.ifremer.sensornanny.sync.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;

/**
 * Archive reader for gzip format.
 * It looks for the .gz extension
 * @author gpagniez
 */
public class GzArchiveReader extends AbstractArchiveFileReader {

    private static final Logger LOGGER = Logger.getLogger(GzArchiveReader.class.getName());

    @Override
    public String getExtension() {
        return ".gz";
    }

    @Override
    public InputStream getFile(String uuid) throws DataNotFoundException{
        InputStream result = null;
        try(GzipCompressorInputStream zipStream = new GzipCompressorInputStream(super.getFile(uuid))){
            File output = writeTempFile(zipStream);
            result = new FileInputStream(output);
        } catch (IOException ioe){
            LOGGER.log(Level.SEVERE, "couldn't write into the temp directory.", ioe);
        }
        return result;
    }
}
