package com.bom.approval.repository;

import com.bom.approval.entity.ApprovalFlow;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ApprovalFlowRepository extends MongoRepository<ApprovalFlow, String> {
    Optional<ApprovalFlow> findByFlowId(String flowId);
}
