package org.burgas.backendserver.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.bill.BillResponse;
import org.burgas.backendserver.service.BillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/bills")
public class BillController {

    private final BillService billService;

    @GetMapping(value = "/by-identity")
    public ResponseEntity<List<BillResponse>> getBillsByIdentity(@RequestParam UUID identityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.billService.findByIdentityId(identityId));
    }

    @GetMapping(value = "/by-id")
    public ResponseEntity<BillResponse> getBillById(@RequestParam UUID billId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.billService.findById(billId));
    }
}
