package com.infsus.dz3_md;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow")
public class WorkflowController {

    private final ZeebeClient zeebeClient;

    /** Create a new process instance and return its business key (taskId). */
    @PostMapping("/start")
    public Map<String, String> startProcess() {
        String taskId = UUID.randomUUID().toString();
        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("task_lifecycle")
                .latestVersion()
                .variables(Map.of("taskId", taskId,               // correlation key for the message
                        "approved", true))              // default branch; adjust as needed
                .send()
                .join();
        return Map.of("taskId", taskId);
    }

    @PostMapping("/confirm/{taskId}")
    public void confirm(@PathVariable String taskId) {
        zeebeClient.newPublishMessageCommand()
                .messageName("ExternalConfirmation")
                .correlationKey(taskId)
                .send()
                .join();
    }
}
