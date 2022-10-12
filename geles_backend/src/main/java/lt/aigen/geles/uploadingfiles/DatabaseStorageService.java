package lt.aigen.geles.uploadingfiles;

import lt.aigen.geles.models.File;
import lt.aigen.geles.repositories.FileRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Base64;

@Service("databaseStorage")
public class DatabaseStorageService implements StorageService {
    private FileRepository fileRepository;

    public DatabaseStorageService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public void saveMultipartFile(MultipartFile multipartFile) throws StorageException {
        if (multipartFile.isEmpty()) {
            throw new StorageException("Failed to store empty file");
        }

        var file = new File();
        file.setName(multipartFile.getResource().getFilename());
        try {
            file.setData(multipartFile.getBytes());
            fileRepository.save(file);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        } catch (ConstraintViolationException e) {
            throw new StorageException("File with filename already exists", e);
        }
    }

    @Override
    @Transactional
    public byte[] getFile(String filename) throws StorageFileNotFoundException {
        var file = fileRepository.findByName(filename);

        if (file.isEmpty()) {
            throw new StorageFileNotFoundException("File not found: " + filename);
        }

        return file.get().getData();
    }

    @Override
    @Transactional
    public String getFileAsBase64(String filename) throws StorageFileNotFoundException {
        byte[] file = getFile(filename);

        Tika tika = new Tika();
        String mimeType = tika.detect(file);

        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(file);
    }

    @Override
    public void deleteFile(String filename) {
        fileRepository.deleteByName(filename);
    }
}
