package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondInbound;
import com.example.fikrie.Service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/inbound")
public class InboundController {

    @Autowired
    InboundService inboundService;

    @GetMapping("/get-by-reference")
    public ResponseEntity<RequestRespondInbound> getInboundByReference(@RequestBody RequestRespondInbound requestRespondInbound) {
        return ResponseEntity.ok(inboundService.getInboundByReference(requestRespondInbound.getReference()));
    }

    @GetMapping("/get-by-sku")
    public ResponseEntity<RequestRespondInbound> getInboundBySku(@RequestBody RequestRespondInbound requestRespondInbound) {
        return ResponseEntity.ok(inboundService.getInboundBySku(requestRespondInbound.getProductSku()));
    }

    @GetMapping("/get-all")
    public ResponseEntity<RequestRespondInbound> getAllInbound() {
        return ResponseEntity.ok(inboundService.getAllInbound());
    }

    @PostMapping("/register")
    public ResponseEntity<RequestRespondInbound> registerInbound(@RequestBody RequestRespondInbound requestRespondInbound) {
        return ResponseEntity.ok(inboundService.registerInbound(requestRespondInbound));
    }

    @PutMapping("/update/{requestInboundId}")
    public ResponseEntity<RequestRespondInbound> updateInbound(@PathVariable(required = true) Long requestInboundId, @RequestBody RequestRespondInbound requestUpdateInbound) {
        return ResponseEntity.ok(inboundService.updateInbound(requestInboundId, requestUpdateInbound));
    }

    @DeleteMapping("/delete/{requestInboundId}")
    public ResponseEntity<RequestRespondInbound> deleteInbound(@PathVariable(required = true) Integer requestInboundId) {
        return ResponseEntity.ok(inboundService.deleteInbound(Long.valueOf(requestInboundId)));
    }
}
