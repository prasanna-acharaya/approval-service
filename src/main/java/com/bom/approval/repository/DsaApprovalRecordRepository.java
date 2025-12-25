package com.bom.approval.repository;

import com.bom.approval.entity.DsaApprovalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DsaApprovalRecordRepository extends MongoRepository<DsaApprovalRecord, String> {
    List<DsaApprovalRecord> findByDsaId(String dsaId);

    List<DsaApprovalRecord> findByUserId(String userId);
}
