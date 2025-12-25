package com.bom.approval.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "dsa_approval_records")
public class DsaApprovalRecord {
    @Id
    private String id;
    private String userId; // Approver User Name or ID
    private String makerId; // Initiator User ID
    private String productTypeName;
    private String dsaId;
    private LocalDateTime approvedDate;
    private String runningFlowId;
}
