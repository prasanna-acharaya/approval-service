package com.bom.approval.service.dsa;

import com.bom.approval.entity.ApprovedProduct;
import com.bom.approval.entity.RunningFlow;
import com.bom.approval.repository.ApprovedProductRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DsaFlowService {

    private final ApprovedProductRepository approvedProductRepository;

    public void stageProducts(String dsaId, List<StageItem> items) {
        // Fetch existing records for this DSA
        List<ApprovedProduct> existing = approvedProductRepository.findByDsaId(dsaId);

        for (StageItem item : items) {
            // Check if exact match {productType, userId} already exists for this dsaId
            boolean alreadyExists = existing.stream().anyMatch(p -> p.getProductType().equals(item.getProductType()) &&
                    p.getUserId().equals(item.getUserId()));

            if (!alreadyExists) {
                ApprovedProduct product = new ApprovedProduct();
                product.setDsaId(dsaId);
                product.setProductType(item.getProductType());
                product.setUserId(item.getUserId());
                approvedProductRepository.save(product);

                // Add to temporary list to prevent duplicate addition if input has internal
                // duplicates
                existing.add(product);
            }
        }
    }

    public RunningFlow authorizeProduct(String dsaId, String productType, String userId) {
        Optional<ApprovedProduct> staged = approvedProductRepository.findByDsaIdAndProductTypeAndUserId(dsaId,
                productType, userId);

        if (staged.isPresent()) {
            ApprovedProduct product = staged.get();
            product.setApprovedAt(LocalDateTime.now());
            approvedProductRepository.save(product);

            // Return a mock/minimal RunningFlow for compatibility with controller,
            // or we could refactor controller to return something else.
            // For now, let's keep it minimal.
            RunningFlow flow = new RunningFlow();
            flow.setStatus("APPROVED");
            flow.setTargetId(dsaId);
            return flow;
        } else {
            throw new RuntimeException("No staged approval found for the provided DSA, Product, and User.");
        }
    }

    public List<Map<String, Object>> verifyProducts(String dsaId) {
        List<ApprovedProduct> records = approvedProductRepository.findByDsaId(dsaId);

        return records.stream().map(record -> {
            Map<String, Object> res = new HashMap<>();
            res.put("name", record.getProductType());
            res.put("value", record.getApprovedAt() != null ? 1 : 0);
            res.put("approvedDate", record.getApprovedAt());
            res.put("approverId", record.getUserId());
            return res;
        }).collect(Collectors.toList());
    }

    public List<ApprovedProduct> getPendingApprovals(String userId) {
        return approvedProductRepository.findByUserIdAndApprovedAtIsNull(userId);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StageItem {
        private String productType;
        private String userId;
    }
}
