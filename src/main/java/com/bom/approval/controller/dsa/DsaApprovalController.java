package com.bom.approval.controller.dsa;

import com.bom.approval.entity.RunningFlow;
import com.bom.approval.service.dsa.DsaFlowService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dsa/approval")
@RequiredArgsConstructor
public class DsaApprovalController {

    private final DsaFlowService dsaFlowService;

    @PostMapping("/stage")
    public ResponseEntity<String> stage(@RequestBody StageRequest request) {
        dsaFlowService.stageProducts(request.getDsaId(), request.getItems());
        return ResponseEntity.ok("Products staged successfully");
    }

    @PostMapping("/authorize")
    public ResponseEntity<RunningFlow> authorize(@RequestBody AuthorizeRequest request) {
        return ResponseEntity
                .ok(dsaFlowService.authorizeProduct(request.getDsaId(), request.getProductType(), request.getUserId()));
    }

    @PostMapping("/verify")
    public ResponseEntity<List<Map<String, Object>>> verify(@RequestBody VerifyRequest request) {
        return ResponseEntity.ok(dsaFlowService.verifyProducts(request.getDsaId()));
    }

    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<?>> getPending(@PathVariable String userId) {
        return ResponseEntity.ok(dsaFlowService.getPendingApprovals(userId));
    }

    @Data
    public static class AuthorizeRequest {
        private String dsaId;
        private String productType;
        private String userId; // The person initiating the authorization
    }

    @Data
    public static class VerifyRequest {
        private String dsaId;
    }

    @Data
    public static class StageRequest {
        private String dsaId;
        private List<DsaFlowService.StageItem> items;
    }
}
