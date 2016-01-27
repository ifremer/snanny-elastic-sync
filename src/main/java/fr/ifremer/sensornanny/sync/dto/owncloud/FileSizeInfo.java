package fr.ifremer.sensornanny.sync.dto.owncloud;

/**
 * Informations about file
 * 
 * @author athorel
 *
 */
public class FileSizeInfo {

    /** Filename */
    private String fileName;

    /** Filesize */
    private Long fileSize;

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
