package com.bom.approval.service;

import com.bom.approval.entity.ApprovedProduct;
import com.bom.approval.entity.DsaApprovalRecord;
import com.bom.approval.entity.ApprovalFlow;
import com.bom.approval.entity.RunningFlow;
import com.bom.approval.repository.ApprovalFlowRepository;
import com.bom.approval.repository.ApprovedProductRepository;
import com.bom.approval.repository.DsaApprovalRecordRepository;
import com.bom.approval.repository.RunningFlowRepository;
import com.bom.approval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final RunningFlowRepository runningFlowRepository;
    private final ApprovalFlowRepository flowRepository;
    private final ApprovedProductRepository approvedProductRepository;
    private final DsaApprovalRecordRepository dsaApprovalRecordRepository;
    private final UserRepository userRepository;

    public RunningFlow processAction(String runningFlowId, String userName, String action, String remark) {
        RunningFlow instance = runningFlowRepository.findByRunningFlowId(runningFlowId)
                .orElseThrow(() -> new RuntimeException("Running flow not found"));

        if (!"PENDING".equals(instance.getStatus())) {
            throw new RuntimeException("Flow is already " + instance.getStatus());
        }

        RunningFlow.StageInstance currentStage = instance.getStages().get(instance.getCurrentStage() - 1);

        RunningFlow.ApproverAction approverAction = new RunningFlow.ApproverAction();
        approverAction.setUserName(userName);
        approverAction.setStatus(action);
        approverAction.setRemark(remark);
        approverAction.setTimestamp(LocalDateTime.now());

        approverAction.setTimestamp(LocalDateTime.now());

        currentStage.getApproverActions().add(approverAction);

        if ("APPROVED".equalsIgnoreCase(action)) {
            userRepository.findByUserName(userName).ifPresent(user -> {
                // Recording DSA Specific Relationship (Suggestion 2)
                if (instance.getData() != null && instance.getData().containsKey("productType")) {
                    DsaApprovalRecord record = new DsaApprovalRecord();
                    record.setUserId(user.getUserName());
                    record.setDsaId(instance.getTargetId());
                    record.setProductTypeName(instance.getData().get("productType").toString());
                    record.setApprovedDate(LocalDateTime.now());
                    record.setRunningFlowId(instance.getRunningFlowId());

                    if (instance.getData().containsKey("makerId")) {
                        record.setMakerId(instance.getData().get("makerId").toString());
                    }

                    dsaApprovalRecordRepository.save(record);
                }
            });
        }

        if ("REJECTED".equalsIgnoreCase(action)) {
            instance.setStatus("REJECTED");
            currentStage.setStatus("REJECTED");
        } else {
            checkAndAdvanceStage(instance, currentStage);
        }

        instance.setLastUpdateAt(LocalDateTime.now());
        return runningFlowRepository.save(instance);
    }

    private void checkAndAdvanceStage(RunningFlow instance, RunningFlow.StageInstance currentStageInstance) {
        ApprovalFlow config = flowRepository.findByFlowId(instance.getFlowId())
                .orElseThrow(() -> new RuntimeException("Flow config not found"));

        ApprovalFlow.StageConfig stageConfig = config.getStages().get(instance.getCurrentStage() - 1);

        // Simple logic for now: ALL approvers must approve (mocking selector logic)
        // In a real scenario, we'd use stageConfig.getSelectionRule()
        boolean isComplete = false;
        if ("ANY_1".equalsIgnoreCase(stageConfig.getSelectionRule())) {
            isComplete = true; // One approval is enough
        } else {
            // Default to ANY_1 for simplicity in this demo, or implement ALL/PERCENT
            isComplete = true;
        }

        if (isComplete) {
            currentStageInstance.setStatus("APPROVED");
            if (instance.getCurrentStage() < instance.getStages().size()) {
                instance.setCurrentStage(instance.getCurrentStage() + 1);
                instance.getStages().get(instance.getCurrentStage() - 1).setStatus("ACTIVE");
            } else {
                instance.setStatus("APPROVED");
                // Explicitly record approved product if present in data
                if (instance.getData() != null && instance.getData().containsKey("productType")) {
                    String productType = instance.getData().get("productType").toString();
                    ApprovedProduct record = new ApprovedProduct();
                    record.setDsaId(instance.getTargetId());
                    record.setProductType(productType);
                    record.setApprovedAt(LocalDateTime.now());
                    record.setRunningFlowId(instance.getRunningFlowId());
                    approvedProductRepository.save(record);
                }
            }
        }
    }
}
