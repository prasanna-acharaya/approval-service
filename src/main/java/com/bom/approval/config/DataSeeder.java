package com.bom.approval.config;

import com.bom.approval.entity.ApprovalFlow;
import com.bom.approval.entity.User;
import com.bom.approval.repository.ApprovalFlowRepository;
import com.bom.approval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApprovalFlowRepository flowRepository;

    @Override
    public void run(String... args) {
        seedUsers();
        seedFlows();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding sample users...");

            User manager = new User();
            manager.setUserName("manager1");
            manager.setRole("BRANCH_MANAGER");
            manager.setEmail("manager@bank.com");

            User credit = new User();
            credit.setUserName("credit1");
            credit.setRole("CREDIT_OFFICER");
            credit.setEmail("credit@bank.com");

            User zonal = new User();
            zonal.setUserName("zonal1");
            zonal.setRole("ZONAL_MANAGER");
            zonal.setEmail("zonal@bank.com");

            User legal = new User();
            legal.setUserName("legal1");
            legal.setRole("LEGAL_COMPLIANCE");
            legal.setEmail("legal@bank.com");

            User mortgage = new User();
            mortgage.setUserName("mortgage1");
            mortgage.setRole("MORTGAGE_HEAD");
            mortgage.setEmail("mortgage@bank.com");

            User bankManager = new User();
            bankManager.setId("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003");
            bankManager.setUserName("manager2");
            bankManager.setRole("BANK_MANAGER");
            bankManager.setEmail("bankmanager@bank.com");

            // External Aligned Users
            User externalAdmin = new User();
            externalAdmin.setId("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380001");
            externalAdmin.setUserName("ADMIN001");
            externalAdmin.setRole("ADMIN");
            externalAdmin.setEmail("admin@bom.com");

            User externalChecker = new User();
            externalChecker.setId("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380002");
            externalChecker.setUserName("CHECKER001");
            externalChecker.setRole("CHECKER");
            externalChecker.setEmail("checker@bom.com");

            User externalDsa = new User();
            externalDsa.setId("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005");
            externalDsa.setUserName("DSA_ACTIVE_001");
            externalDsa.setRole("DSA");
            externalDsa.setEmail("dsa001@example.com");

            userRepository.saveAll(Arrays.asList(manager, credit, zonal, legal, mortgage, bankManager, externalAdmin,
                    externalChecker, externalDsa));
        }
    }

    private void seedFlows() {
        flowRepository.deleteAll(); // Force refresh for testing
        log.info("Refreshing/Seeding sample approval flows...");

        // 1. Basic Flow: amount <= 100k
        ApprovalFlow basicFlow = new ApprovalFlow();
        basicFlow.setName("Basic Loan Approval");

        ApprovalFlow.ConditionNode basicCond = new ApprovalFlow.ConditionNode();
        basicCond.setField("amount");
        basicCond.setOperator("LT");
        basicCond.setValue(100001);

        basicFlow.setConditions(Collections.singletonList(basicCond));

        ApprovalFlow.StageConfig stage1 = new ApprovalFlow.StageConfig();
        stage1.setStageOrder(1);
        stage1.setSelectionRule("ANY_1");

        ApprovalFlow.SelectorNode managerSelector = new ApprovalFlow.SelectorNode();
        managerSelector.setType("ROLE");
        managerSelector.setValue("BRANCH_MANAGER");

        stage1.setSelectors(Collections.singletonList(managerSelector));
        basicFlow.setStages(Collections.singletonList(stage1));

        // 2. High Value Flow: amount > 200k
        ApprovalFlow enterpriseFlow = new ApprovalFlow();
        enterpriseFlow.setName("Enterprise Loan Approval");

        ApprovalFlow.ConditionNode enterpriseCond = new ApprovalFlow.ConditionNode();
        enterpriseCond.setField("amount");
        enterpriseCond.setOperator("GT");
        enterpriseCond.setValue(200000);

        enterpriseFlow.setConditions(Collections.singletonList(enterpriseCond));

        // Stage 1: Credit Officer
        ApprovalFlow.StageConfig stageCredit = new ApprovalFlow.StageConfig();
        stageCredit.setStageOrder(1);
        stageCredit.setSelectionRule("ANY_1");

        ApprovalFlow.SelectorNode creditSelector = new ApprovalFlow.SelectorNode();
        creditSelector.setType("ROLE");
        creditSelector.setValue("CREDIT_OFFICER");
        stageCredit.setSelectors(Collections.singletonList(creditSelector));

        // Stage 2: Zonal Manager
        ApprovalFlow.StageConfig stageZonal = new ApprovalFlow.StageConfig();
        stageZonal.setStageOrder(2);
        stageZonal.setSelectionRule("ANY_1");

        ApprovalFlow.SelectorNode zonalSelector = new ApprovalFlow.SelectorNode();
        zonalSelector.setType("ROLE");
        zonalSelector.setValue("ZONAL_MANAGER");
        stageZonal.setSelectors(Collections.singletonList(zonalSelector));

        enterpriseFlow.setStages(Arrays.asList(stageCredit, stageZonal));

        // 3. NEW: Unified Flow (Corporate + Home Loan)
        ApprovalFlow complexFlow = new ApprovalFlow();
        complexFlow.setName("Corporate Home Loan Unified Flow");

        ApprovalFlow.ConditionNode condCorp = new ApprovalFlow.ConditionNode();
        condCorp.setField("category");
        condCorp.setOperator("EQ");
        condCorp.setValue("CORPORATE");

        ApprovalFlow.ConditionNode condHome = new ApprovalFlow.ConditionNode();
        condHome.setField("productType");
        condHome.setOperator("EQ");
        condHome.setValue("HOME_LOAN");

        complexFlow.setConditions(Arrays.asList(condCorp, condHome)); // Implicit AND

        // Stage 1: Legal Compliance
        ApprovalFlow.StageConfig s1 = new ApprovalFlow.StageConfig();
        s1.setStageOrder(1);
        s1.setSelectionRule("ANY_1");
        ApprovalFlow.SelectorNode selLegal = new ApprovalFlow.SelectorNode();
        selLegal.setType("ROLE");
        selLegal.setValue("LEGAL_COMPLIANCE");
        s1.setSelectors(Collections.singletonList(selLegal));

        // Stage 2: Product Specialist (Mortgage Head)
        ApprovalFlow.StageConfig s2 = new ApprovalFlow.StageConfig();
        s2.setStageOrder(2);
        s2.setSelectionRule("ANY_1");
        ApprovalFlow.SelectorNode selMortgage = new ApprovalFlow.SelectorNode();
        selMortgage.setType("ROLE");
        selMortgage.setValue("MORTGAGE_HEAD");
        s2.setSelectors(Collections.singletonList(selMortgage));

        // Stage 3: Zonal Head
        ApprovalFlow.StageConfig s3 = new ApprovalFlow.StageConfig();
        s3.setStageOrder(3);
        s3.setSelectionRule("ANY_1");
        ApprovalFlow.SelectorNode selZonal = new ApprovalFlow.SelectorNode();
        selZonal.setType("ROLE");
        selZonal.setValue("ZONAL_MANAGER");
        s3.setSelectors(Collections.singletonList(selZonal));

        complexFlow.setStages(Arrays.asList(s1, s2, s3));

        // 4. DSA Product Specific Flows
        ApprovalFlow homeLoanFlow = createProductFlow("DSA Home Loan Flow", "HOME_LOAN", "BANK_MANAGER");
        ApprovalFlow eduLoanFlow = createProductFlow("DSA Education Loan Flow", "EDUCATION_LOAN", "BANK_MANAGER");
        ApprovalFlow vehicleLoanFlow = createProductFlow("DSA Vehicle Loan Flow", "VEHICLE_LOAN", "BANK_MANAGER");

        flowRepository.saveAll(
                Arrays.asList(basicFlow, enterpriseFlow, complexFlow, homeLoanFlow, eduLoanFlow, vehicleLoanFlow));
    }

    private ApprovalFlow createProductFlow(String name, String productType, String approverRole) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setName(name);

        ApprovalFlow.ConditionNode cond = new ApprovalFlow.ConditionNode();
        cond.setField("productType");
        cond.setOperator("EQ");
        cond.setValue(productType);
        flow.setConditions(Collections.singletonList(cond));

        ApprovalFlow.StageConfig stage = new ApprovalFlow.StageConfig();
        stage.setStageOrder(1);
        stage.setSelectionRule("ANY_1");

        ApprovalFlow.SelectorNode selector = new ApprovalFlow.SelectorNode();
        selector.setType("ROLE");
        selector.setValue(approverRole);
        stage.setSelectors(Collections.singletonList(selector));

        flow.setStages(Collections.singletonList(stage));
        return flow;
    }
}
