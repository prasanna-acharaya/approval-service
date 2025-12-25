package com.bom.approval.repository;

import com.bom.approval.entity.RunningFlow;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface RunningFlowRepository extends MongoRepository<RunningFlow, String> {
    Optional<RunningFlow> findByRunningFlowId(String runningFlowId);

    List<RunningFlow> findByStatus(String status);
}
