package fr.ifremer.sensornanny.sync.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;

/**
 * Archive reader for zip format.
 * It looks for the .zip extension
 * @author gpagniez
 */
public class ZipArchiveReader extends AbstractArchiveFileReader {

    private static final Logger LOGGER = Logger.getLogger(ZipArchiveReader.class.getName());

    @Override
    public String getExtension() {
        return ".zip";
    }

    @Override
    public InputStream getFile(String uuid) throws DataNotFoundException{
        InputStream result = null;
        try(ZipArchiveInputStream zipStream = new ZipArchiveInputStream(super.getFile(uuid))){
            zipStream.getNextEntry();
            File output = writeTempFile(zipStream);
            result = new FileInputStream(output);
        } catch (IOException ioe){
            LOGGER.log(Level.SEVERE, "couldn't write into the temp directory.", ioe);
        }
        return result;
    }

}
