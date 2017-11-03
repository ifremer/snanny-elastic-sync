package fr.ifremer.sensornanny.sync.io;

import java.io.InputStream;

import fr.ifremer.sensornanny.sync.dao.rest.DataNotFoundException;

/**
 * A file reader, that's all
 * @author gpagniez
 */
public interface DataFileReader {

    /**
     * Give the format acceptance of the reader
     * @see fr.ifremer.sensornanny.sync.manager.DataFileManager
     * @return
     */
    default String getExtension(){
        return ".*";
    }

    /**
     * Recover the inputstream of a file by its UUID
     * @param uuid the id of the file
     * @return an {@link InputStream} of the file
     * @throws DataNotFoundException no file found for the given id
     */
    InputStream getFile(String uuid) throws DataNotFoundException;
}
