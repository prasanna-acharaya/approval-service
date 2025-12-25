package com.bom.approval.service;

import com.bom.approval.entity.ApprovalFlow.ConditionNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RuleEngineService {

    public boolean evaluate(List<ConditionNode> conditions, Map<String, Object> data) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        // Default logic is AND for the top-level list
        for (ConditionNode node : conditions) {
            if (!evaluateNode(node, data)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateNode(ConditionNode node, Map<String, Object> data) {
        if (node.getNestedConditions() != null && !node.getNestedConditions().isEmpty()) {
            String logic = node.getLogic() != null ? node.getLogic() : "AND";
            if ("OR".equalsIgnoreCase(logic)) {
                for (ConditionNode nested : node.getNestedConditions()) {
                    if (evaluateNode(nested, data))
                        return true;
                }
                return false;
            } else {
                for (ConditionNode nested : node.getNestedConditions()) {
                    if (!evaluateNode(nested, data))
                        return false;
                }
                return true;
            }
        }

        Object actualValue = data.get(node.getField());
        return compare(actualValue, node.getOperator(), node.getValue());
    }

    private boolean compare(Object actual, String operator, Object expected) {
        if (actual == null)
            return "NEQ".equalsIgnoreCase(operator);

        switch (operator.toUpperCase()) {
            case "EQ":
                return actual.toString().equals(expected.toString());
            case "NEQ":
                return !actual.toString().equals(expected.toString());
            case "GT":
                return compareNumbers(actual, expected) > 0;
            case "LT":
                return compareNumbers(actual, expected) < 0;
            case "CONTAINS":
                return actual.toString().contains(expected.toString());
            default:
                return false;
        }
    }

    private int compareNumbers(Object actual, Object expected) {
        try {
            Double a = Double.parseDouble(actual.toString());
            Double e = Double.parseDouble(expected.toString());
            return a.compareTo(e);
        } catch (Exception e) {
            return 0;
        }
    }
}
