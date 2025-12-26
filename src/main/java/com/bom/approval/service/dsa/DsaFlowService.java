package com.bom.approval.service.dsa;

import com.bom.approval.entity.ApprovalFlow;
import com.bom.approval.entity.ApprovedProduct;
import com.bom.approval.entity.RunningFlow;
import com.bom.approval.entity.User;
import com.bom.approval.repository.ApprovalFlowRepository;
import com.bom.approval.repository.ApprovedProductRepository;
import com.bom.approval.repository.UserRepository;
import com.bom.approval.service.RuleEngineService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DsaFlowService {

    private final ApprovedProductRepository approvedProductRepository;
    private final ApprovalFlowRepository flowRepository;
    private final UserRepository userRepository;
    private final RuleEngineService ruleEngineService;

    public void stageProducts(String dsaId, List<StageItem> items) {
        // Fetch existing records for this DSA
        List<ApprovedProduct> existing = approvedProductRepository.findByDsaId(dsaId);
        List<ApprovalFlow> allFlows = flowRepository.findAll();

        for (StageItem item : items) {
            String resolvedUserId = resolveUserIdForProduct(item.getProductType(), allFlows);

            if (resolvedUserId == null) {
                log.warn("Could not resolve userId for productType: {}. Skipping staging.", item.getProductType());
                continue;
            }

            // Check if exact match {productType, resolvedUserId} already exists for this
            // dsaId
            boolean alreadyExists = existing.stream().anyMatch(p -> p.getProductType().equals(item.getProductType()) &&
                    p.getUserId().equals(resolvedUserId));

            if (!alreadyExists) {
                ApprovedProduct product = new ApprovedProduct();
                product.setDsaId(dsaId);
                product.setProductType(item.getProductType());
                product.setUserId(resolvedUserId);
                approvedProductRepository.save(product);

                // Add to temporary list to prevent duplicate addition
                existing.add(product);
            }
        }
    }

    private String resolveUserIdForProduct(String productType, List<ApprovalFlow> flows) {
        Map<String, Object> data = new HashMap<>();
        data.put("productType", productType);

        // Find matching flow
        for (ApprovalFlow flow : flows) {
            if (ruleEngineService.evaluate(flow.getConditions(), data)) {
                // Flow matched! Get the first stage
                if (flow.getStages() != null && !flow.getStages().isEmpty()) {
                    ApprovalFlow.StageConfig firstStage = flow.getStages().get(0);
                    // Find a ROLE selector
                    for (ApprovalFlow.SelectorNode selector : firstStage.getSelectors()) {
                        if ("ROLE".equalsIgnoreCase(selector.getType())) {
                            String role = selector.getValue().toString();
                            List<User> users = userRepository.findByRole(role);
                            if (!users.isEmpty()) {
                                return users.get(0).getId();
                            }
                        }
                    }
                }
            }
        }
        return null; // Could not resolve
    }

    public RunningFlow authorizeProduct(String dsaId, String productType, String userId) {
        Optional<ApprovedProduct> staged = approvedProductRepository.findByDsaIdAndProductTypeAndUserId(dsaId,
                productType, userId);

        if (staged.isPresent()) {
            ApprovedProduct product = staged.get();
            product.setApprovedAt(LocalDateTime.now());
            approvedProductRepository.save(product);

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
    }
}
