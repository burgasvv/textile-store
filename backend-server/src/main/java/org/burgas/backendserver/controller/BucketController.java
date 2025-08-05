package org.burgas.backendserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.bill.BillResponse;
import org.burgas.backendserver.dto.bucket.BucketResponse;
import org.burgas.backendserver.service.BucketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/buckets")
public class BucketController {

    private final BucketService bucketService;

    @GetMapping(value = "/by-cookie")
    public ResponseEntity<BucketResponse> getBucketByCookie(HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.findByCookie(request));
    }

    @GetMapping(value = "/add-product-by-cookie")
    public ResponseEntity<BucketResponse> addProductByCookie(HttpServletRequest request, HttpServletResponse response, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.addProductByCookieToMaster(request, response, productId));
    }

    @GetMapping(value = "/remove-product-by-cookie")
    public ResponseEntity<BucketResponse> removeProductByCookie(HttpServletRequest request, HttpServletResponse response, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.removeProductByCookieFromMaster(request, response, productId));
    }

    @GetMapping(value = "/plus-product-amount-by-cookie")
    public ResponseEntity<BucketResponse> plusProductAmountByCookie(HttpServletRequest request, HttpServletResponse response, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.plusProductAmountByCookieInMaster(request, response, productId));
    }

    @GetMapping(value = "/minus-product-amount-by-cookie")
    public ResponseEntity<BucketResponse> minusProductAmountByCookie(HttpServletRequest request, HttpServletResponse response, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.minusProductAmountByCookieInMaster(request, response, productId));
    }

    @GetMapping(value = "/by-session")
    public ResponseEntity<BucketResponse> getBucketBySession(HttpServletRequest httpServletRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.findInSession(httpServletRequest));
    }

    @GetMapping(value = "/add-product-in-session")
    public ResponseEntity<BucketResponse> addProductToBucketInSession(HttpServletRequest httpServletRequest, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.addProductToBucketInSession(httpServletRequest, productId));
    }

    @GetMapping(value = "/remove-product-in-session")
    public ResponseEntity<BucketResponse> removeProductFromBucketInSession(HttpServletRequest httpServletRequest, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.removeProductFromBucketInSession(httpServletRequest, productId));
    }

    @GetMapping(value = "/plus-product-amount-in-session")
    public ResponseEntity<BucketResponse> plusProductAmountInSession(HttpServletRequest httpServletRequest, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.plusProductAmountInSession(httpServletRequest, productId));
    }

    @GetMapping(value = "/minus-product-amount-in-session")
    public ResponseEntity<BucketResponse> minusProductAmountInSession(HttpServletRequest httpServletRequest, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.minusProductAmountInSession(httpServletRequest, productId));
    }

    @GetMapping(value = "by-id")
    public ResponseEntity<BucketResponse> getBucketById(@RequestParam UUID bucketId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.findById(bucketId));
    }

    @GetMapping(value = "/by-identity")
    public ResponseEntity<BucketResponse> getBucketByIdentityId(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.findBucketByIdentityId(identityId));
    }

    @PostMapping(value = "/add-product")
    public ResponseEntity<BucketResponse> addProductToBucket(@RequestParam UUID identityId, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.addProductToMaster(identityId, productId));
    }

    @PutMapping(value = "/plus-product-amount")
    public ResponseEntity<BucketResponse> plusProductAmount(@RequestParam UUID identityId, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.plusAmountOnMaster(identityId, productId));
    }

    @PutMapping(value = "/minus-product-amount")
    public ResponseEntity<BucketResponse> minusProductAmount(@RequestParam UUID identityId, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.minusAmountOnMaster(identityId, productId));
    }

    @DeleteMapping(value = "/remove-product")
    public ResponseEntity<BucketResponse> removeProductFromBucket(@RequestParam UUID identityId, @RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.removeProductFromBucketFromMaster(identityId, productId));
    }

    @DeleteMapping(value = "/clean-bucket")
    public ResponseEntity<BucketResponse> cleanBucket(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.cleanBucketOnMaster(identityId));
    }

    @PostMapping(value = "/pay-bill")
    public ResponseEntity<BillResponse> payBucketBill(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.bucketService.payBill(identityId));
    }
}
