package com.bom.approval.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Document(collection = "running_flows")
public class RunningFlow {
    @Id
    private String id;
    private String runningFlowId = UUID.randomUUID().toString();
    private String flowId;
    private String targetId; // e.g. LeadId
    private Map<String, Object> data;
    private String status; // PENDING, APPROVED, REJECTED
    private int currentStage;
    private List<StageInstance> stages;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastUpdateAt = LocalDateTime.now();

    @Data
    public static class StageInstance {
        private int order;
        private String status; // PENDING, APPROVED, REJECTED
        private List<ApproverAction> approverActions;
    }

    @Data
    public static class ApproverAction {
        private String userName;
        private String status; // APPROVED, REJECTED
        private String remark;
        private LocalDateTime timestamp = LocalDateTime.now();
    }
}
