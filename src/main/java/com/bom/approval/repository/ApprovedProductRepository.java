package com.bom.approval.repository;

import com.bom.approval.entity.ApprovedProduct;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovedProductRepository extends MongoRepository<ApprovedProduct, String> {
    List<ApprovedProduct> findByDsaId(String dsaId);

    Optional<ApprovedProduct> findByDsaIdAndProductType(String dsaId, String productType);

    Optional<ApprovedProduct> findByDsaIdAndProductTypeAndUserId(String dsaId, String productType, String userId);

    List<ApprovedProduct> findByUserIdAndApprovedAtIsNull(String userId);
}
