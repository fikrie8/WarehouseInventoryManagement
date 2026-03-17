package com.example.fikrie.Controller;

import com.example.fikrie.DataTransferObject.RequestRespondOutbound;
import com.example.fikrie.Service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/outbound")
public class OutboundController {

    @Autowired
    OutboundService outboundService;

    @GetMapping("/get-by-reference")
    public ResponseEntity<RequestRespondOutbound> getOutboundByReference(@RequestBody RequestRespondOutbound requestRespondOutbound) {
        return ResponseEntity.ok(outboundService.getOutboundByReference(requestRespondOutbound.getReference()));
    }

    @GetMapping("/get-by-sku")
    public ResponseEntity<RequestRespondOutbound> getOutboundBySku(@RequestBody RequestRespondOutbound requestRespondOutbound) {
        return ResponseEntity.ok(outboundService.getOutboundBySku(requestRespondOutbound.getProductSku()));
    }

    @GetMapping("/get-all")
    public ResponseEntity<RequestRespondOutbound> getAllOutbound() {
        return ResponseEntity.ok(outboundService.getAllOutbound());
    }

    @PostMapping("register")
    public ResponseEntity<RequestRespondOutbound> registerOutbound(@RequestBody RequestRespondOutbound requestRespondOutbound) {
        return ResponseEntity.ok(outboundService.registerOutbound(requestRespondOutbound));
    }

    @PutMapping("/update/{requestOutboundId}")
    public ResponseEntity<RequestRespondOutbound> updateOutbound(@PathVariable(required = true) Long requestOutboundId, @RequestBody RequestRespondOutbound requestUpdateOutbound) {
        return ResponseEntity.ok(outboundService.updateOutbound(requestOutboundId, requestUpdateOutbound));
    }

    @DeleteMapping("/delete/{requestOutboundId}")
    public ResponseEntity<RequestRespondOutbound> deleteOutbound(@PathVariable(required = true) Integer requestOutboundId) {
        return ResponseEntity.ok(outboundService.deleteOutbound(Long.valueOf(requestOutboundId)));
    }
}
