package com.bom.approval.controller;

import com.bom.approval.entity.ApprovalFlow;
import com.bom.approval.service.FlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config/flows")
@RequiredArgsConstructor
public class FlowConfigController {

    private final FlowService flowService;

    @PostMapping
    public ResponseEntity<ApprovalFlow> createFlow(@RequestBody ApprovalFlow flow) {
        return ResponseEntity.ok(flowService.createFlow(flow));
    }

    @GetMapping
    public ResponseEntity<List<ApprovalFlow>> getAllFlows() {
        return ResponseEntity.ok(flowService.getAllFlows());
    }
}
