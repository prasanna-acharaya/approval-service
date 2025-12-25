package com.bom.approval.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "approved_products")
public class ApprovedProduct {
    @Id
    private String id;
    private String dsaId;
    private String userId;
    private String productType;
    private LocalDateTime approvedAt;
    private String runningFlowId;
}
