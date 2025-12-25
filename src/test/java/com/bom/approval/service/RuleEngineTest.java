package com.bom.approval.service;

import com.bom.approval.entity.ApprovalFlow.ConditionNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleEngineTest {

    private final RuleEngineService ruleEngineService = new RuleEngineService();

    @Test
    public void testSimpleEvaluation() {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 250000);
        data.put("region", "WEST");

        ConditionNode cond1 = new ConditionNode("amount", "GT", 200000, null, null);
        ConditionNode cond2 = new ConditionNode("region", "EQ", "WEST", null, null);

        assertTrue(ruleEngineService.evaluate(Arrays.asList(cond1, cond2), data));
    }

    @Test
    public void testNestedLogic() {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 150000);
        data.put("category", "URGENT");

        ConditionNode cond1 = new ConditionNode("amount", "GT", 200000, null, null);
        ConditionNode cond2 = new ConditionNode("category", "EQ", "URGENT", null, null);

        // (amount > 200k OR category == URGENT)
        ConditionNode orNode = new ConditionNode();
        orNode.setLogic("OR");
        orNode.setNestedConditions(Arrays.asList(cond1, cond2));

        assertTrue(ruleEngineService.evaluate(Arrays.asList(orNode), data));
    }

    @Test
    public void testFailedEvaluation() {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 150000);

        ConditionNode cond1 = new ConditionNode("amount", "GT", 200000, null, null);

        assertFalse(ruleEngineService.evaluate(Arrays.asList(cond1), data));
    }
}
