package com.infsus.dz3_md;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class CamundaConfig {

    private final ZeebeClient zeebeClient;

    @PostConstruct
    public void deployBpmn() {
        zeebeClient.newDeployResourceCommand()
                .addResourceFromClasspath("bpmn/task-lifecycle.bpmn")
                .send()
                .join();
    }

    @ZeebeWorker(type = "mark-ready")
    public void handleMarkReady(io.camunda.zeebe.client.api.response.ActivatedJob job,
                                io.camunda.zeebe.client.api.worker.JobClient jobClient) {
        UUID taskId = UUID.fromString(job.getVariablesAsMap().get("taskId").toString());
        System.out.println("Marking task " + taskId + " as READY");
        jobClient.newCompleteCommand(job.getKey()).send().join();
    }
}
