package com.bom.approval;

import com.bom.approval.entity.ApprovalFlow;
import com.bom.approval.entity.RunningFlow;
import com.bom.approval.service.FlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class WorkflowIntegrationTest {

    @Autowired
    private FlowService flowService;

    @MockBean
    private JavaMailSender mailSender; // Mock mail sender to avoid connection issues

    @Test
    public void testCompleteWorkflow() {
        // 1. Create a flow configuration
        ApprovalFlow flow = new ApprovalFlow();
        flow.setName("High Value Flow");

        ApprovalFlow.ConditionNode cond = new ApprovalFlow.ConditionNode("amount", "GT", 100000, null, null);
        flow.setConditions(Arrays.asList(cond));

        ApprovalFlow.StageConfig stage1 = new ApprovalFlow.StageConfig();
        stage1.setStageOrder(1);
        stage1.setSelectionRule("ANY_1");

        flow.setStages(Arrays.asList(stage1));
        flowService.createFlow(flow);

        // 2. Fire approval
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 150000);

        RunningFlow instance = flowService.fireApproval("lead_123", data);

        assertNotNull(instance);
        assertEquals("PENDING", instance.getStatus());
        assertEquals(1, instance.getCurrentStage());
    }
}
