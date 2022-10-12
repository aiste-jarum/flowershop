package lt.aigen.geles.controller;

import lt.aigen.geles.uploadingfiles.StorageFileNotFoundException;
import lt.aigen.geles.uploadingfiles.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {
    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        storageService.saveMultipartFile(file);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) {
        try {
            return new ResponseEntity<>(storageService.getFile(filename), HttpStatus.OK);
        } catch (IOException | StorageFileNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
