package bnorbert.springbootwebsite.controller;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import bnorbert.springbootwebsite.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<Void> resize(@RequestParam(value = "input") String input) throws IOException {
        imageService.resize(input);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/detection")
    public ResponseEntity<Void> detect(@RequestParam(value = "input") String input) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        imageService.detect(input);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
