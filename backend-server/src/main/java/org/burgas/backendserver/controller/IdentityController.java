package org.burgas.backendserver.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.identity.IdentityRequest;
import org.burgas.backendserver.dto.identity.IdentityResponse;
import org.burgas.backendserver.exception.WrongUploadFileTypeException;
import org.burgas.backendserver.message.ImageMessages;
import org.burgas.backendserver.service.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/identities")
public class IdentityController {

    private final IdentityService identityService;

    @GetMapping
    public ResponseEntity<List<IdentityResponse>> getAllIdentities() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.identityService.findAll());
    }

    @GetMapping(value = "/by-id")
    public ResponseEntity<IdentityResponse> getIdentityById(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.identityService.findById(identityId));
    }

    @PostMapping(value = "/create")
    public ResponseEntity<IdentityResponse> createIdentity(@RequestBody IdentityRequest identityRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.identityService.createInMaster(identityRequest));
    }

    @PutMapping(value = "/update")
    public ResponseEntity<IdentityResponse> updateIdentity(@RequestBody IdentityRequest identityRequest, @RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.identityService.updateInMaster(identityRequest, identityId));
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<String> deleteIdentity(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(this.identityService.deleteFromMaster(identityId));
    }

    @PatchMapping(value = "/change-password")
    public ResponseEntity<String> changeIdentityPassword(@RequestParam UUID identityId, @RequestParam String password) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(this.identityService.changePasswordInMaster(identityId, password));
    }

    @PatchMapping(value = "/enable-disable")
    public ResponseEntity<String> enableOrDisableIdentity(@RequestParam UUID identityId, @RequestParam Boolean enabled) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(this.identityService.enableDisableOnMaster(identityId, enabled));
    }

    @PostMapping(value = "/upload-image")
    public ResponseEntity<String> uploadIdentityImage(@RequestParam UUID identityId, @RequestPart MultipartFile file) throws IOException {
        if (Objects.requireNonNull(file.getContentType()).split("/")[0].equals("image")) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                    .body(this.identityService.uploadImageOnMaster(identityId, file));
        } else {
            throw new WrongUploadFileTypeException(ImageMessages.WRONG_UPLOAD_FILE_TYPE.getMessage());
        }
    }

    @PutMapping(value = "/change-image")
    public ResponseEntity<String> changeIdentityImage(@RequestParam UUID identityId, @RequestPart MultipartFile file) throws IOException {
        if (Objects.requireNonNull(file.getContentType()).split("/")[0].equals("image")) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                    .body(this.identityService.changeImageOnMaster(identityId, file));
        } else {
            throw new WrongUploadFileTypeException(ImageMessages.WRONG_UPLOAD_FILE_TYPE.getMessage());
        }
    }

    @DeleteMapping(value = "/delete-image")
    public ResponseEntity<String> deleteIdentityImage(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(this.identityService.deleteImageFromMaster(identityId));
    }
}
