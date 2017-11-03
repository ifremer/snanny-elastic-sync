package fr.ifremer.sensornanny.sync.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Abstract class for managing archive file types
 * @author gpagniez
 */
abstract class AbstractArchiveFileReader extends OwnCloudFileReader {

    private static final int BUFFER_SIZE = 4096;

    /**
     * Create a temporary buffer file for extraction. It will be deleted as the batch shutdown
     * @param stream the {@link InputStream} from the input archive
     * @return a {@link File} object of the newly created temporary file
     * @throws IOException issue when reading/writing on the system
     */
    File writeTempFile(InputStream stream) throws IOException {
        File temp = File.createTempFile("elasticsync", UUID.randomUUID().toString());
        temp.deleteOnExit();
        byte[] byteBuffer = new byte[BUFFER_SIZE];
        int read;
        //write the inputstream directly in the temp file outputstream
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp))){
            while((read = stream.read(byteBuffer)) != -1){
                bos.write(byteBuffer, 0, read);
            }
        }
        return temp;
    }
}