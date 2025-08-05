package org.burgas.backendserver.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.entity.Image;
import org.burgas.backendserver.service.ImageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/images")
public class ImageController {

    private final ImageService imageService;

    @GetMapping(value = "/by-id")
    public ResponseEntity<Resource> getImageById(@RequestParam UUID imageId) {
        Image image = this.imageService.findById(imageId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(
                        new InputStreamResource(
                                new ByteArrayInputStream(image.getData())
                        )
                );
    }
}
