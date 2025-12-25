package com.bom.approval.config;

import com.bom.approval.entity.RunningFlow;
import com.bom.approval.repository.RunningFlowRepository;
import com.bom.approval.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalAlertScheduler {

    private final RunningFlowRepository runningFlowRepository;
    private final EmailService emailService;

    // Checks every 24 hours (86400000 ms)
    @Scheduled(fixedRate = 86400000)
    public void checkPendingApprovals() {
        log.info("Checking pending approvals for alerts...");
        List<RunningFlow> pendingFlows = runningFlowRepository.findByStatus("PENDING");

        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

        for (RunningFlow flow : pendingFlows) {
            if (flow.getCreatedAt().isBefore(twoDaysAgo)) {
                log.info("Sending alert for flow: {}", flow.getRunningFlowId());
                // In a real scenario, we'd find the current stage approvers' emails
                emailService.sendEmail("admin@example.com",
                        "SLA Breach: Approval Pending",
                        "Flow " + flow.getRunningFlowId() + " has been pending for more than 2 days.");
            }
        }
    }
}
