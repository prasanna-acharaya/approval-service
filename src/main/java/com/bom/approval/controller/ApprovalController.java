package com.bom.approval.controller;

import com.bom.approval.entity.RunningFlow;
import com.bom.approval.service.ApprovalService;
import com.bom.approval.service.FlowService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final FlowService flowService;
    private final ApprovalService approvalService;

    @PostMapping("/fire")
    public ResponseEntity<RunningFlow> fireApproval(@RequestBody FireRequest request) {
        RunningFlow instance = flowService.fireApproval(request.getTargetId(), request.getData());
        return ResponseEntity.ok(instance);
    }

    @PostMapping("/action")
    public ResponseEntity<RunningFlow> processAction(@RequestBody ActionRequest request) {
        RunningFlow updated = approvalService.processAction(
                request.getRunningFlowId(),
                request.getUserName(),
                request.getAction(),
                request.getRemark());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{runningFlowId}")
    public ResponseEntity<RunningFlow> getStatus(@PathVariable String runningFlowId) {
        return ResponseEntity.ok(flowService.getRunningFlow(runningFlowId));
    }

    @Data
    public static class FireRequest {
        private String targetId;
        private Map<String, Object> data;
    }

    @Data
    public static class ActionRequest {
        private String runningFlowId;
        private String userName;
        private String action; // APPROVED, REJECTED
        private String remark;
    }
}
