package com.bom.approval.service;

import com.bom.approval.entity.ApprovalFlow;
import com.bom.approval.entity.RunningFlow;
import com.bom.approval.repository.ApprovalFlowRepository;
import com.bom.approval.repository.RunningFlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowService {

    private final ApprovalFlowRepository flowRepository;
    private final RunningFlowRepository runningFlowRepository;
    private final RuleEngineService ruleEngineService;

    public ApprovalFlow createFlow(ApprovalFlow flow) {
        return flowRepository.save(flow);
    }

    public List<ApprovalFlow> getAllFlows() {
        return flowRepository.findAll();
    }

    public RunningFlow fireApproval(String targetId, Map<String, Object> data) {
        List<ApprovalFlow> allFlows = flowRepository.findAll();

        ApprovalFlow matchedFlow = allFlows.stream()
                .filter(f -> ruleEngineService.evaluate(f.getConditions(), data))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching approval flow found for the provided data"));

        RunningFlow instance = new RunningFlow();
        instance.setFlowId(matchedFlow.getFlowId());
        instance.setTargetId(targetId);
        instance.setData(data);
        instance.setStatus("PENDING");
        instance.setCurrentStage(1);

        List<RunningFlow.StageInstance> stageInstances = matchedFlow.getStages().stream()
                .map(s -> {
                    RunningFlow.StageInstance si = new RunningFlow.StageInstance();
                    si.setOrder(s.getStageOrder());
                    si.setStatus(s.getStageOrder() == 1 ? "ACTIVE" : "PENDING");
                    si.setApproverActions(new ArrayList<>());
                    return si;
                }).collect(Collectors.toList());

        instance.setStages(stageInstances);
        return runningFlowRepository.save(instance);
    }

    public RunningFlow getRunningFlow(String runningFlowId) {
        return runningFlowRepository.findByRunningFlowId(runningFlowId)
                .orElseThrow(() -> new RuntimeException("Running flow not found"));
    }
}
