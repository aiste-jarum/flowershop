package lt.aigen.geles.uploadingfiles;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    void saveMultipartFile(MultipartFile file) throws StorageException;

    String getFileAsBase64(String filename) throws IOException;

    byte[] getFile(String filename) throws IOException;

    void deleteFile(String filename) throws IOException;
}
