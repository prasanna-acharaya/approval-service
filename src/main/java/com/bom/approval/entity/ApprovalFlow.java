package com.bom.approval.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "approval_flows")
public class ApprovalFlow {
    @Id
    private String id;
    private String flowId = UUID.randomUUID().toString();
    private String name;
    private List<ConditionNode> conditions;
    private List<StageConfig> stages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConditionNode {
        private String field;
        private String operator; // GT, LT, EQ, NEQ, CONTAINS
        private Object value;
        private String logic; // AND, OR (for nested conditions)
        private List<ConditionNode> nestedConditions;
    }

    @Data
    public static class StageConfig {
        private int stageOrder;
        private List<SelectorNode> selectors;
        private String selectionRule; // ANY_1, ANY_X_PERCENT, ALL
    }

    @Data
    public static class SelectorNode {
        private String type; // ROLE, USERNAME, CUSTOM_FIELD
        private String field;
        private Object value;
    }
}
