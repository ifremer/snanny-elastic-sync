package fr.ifremer.sensornanny.sync.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;

/**
 * Archive reader for Bzip2 format.
 * It looks for the .bz2 extension
 * @author gpagniez
 */
public class Bz2ArchiveReader extends AbstractArchiveFileReader {

    private static final Logger LOGGER = Logger.getLogger(Bz2ArchiveReader.class.getName());

    @Override
    public String getExtension() {
        return ".bz2";
    }

    @Override
    public InputStream getFile(String uuid) throws DataNotFoundException {
        InputStream result = null;
        try(BZip2CompressorInputStream zipStream = new BZip2CompressorInputStream(super.getFile(uuid))){
            File output = writeTempFile(zipStream);
            result = new FileInputStream(output);
        } catch (IOException ioe){
            LOGGER.log(Level.SEVERE, "couldn't write into the temp directory.", ioe);
        }
        return result;
    }
}
