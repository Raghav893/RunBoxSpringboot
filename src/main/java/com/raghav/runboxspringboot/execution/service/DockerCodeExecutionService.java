package com.raghav.runboxspringboot.execution.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.raghav.runboxspringboot.execution.config.LanguageConfig;
import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.submit.entity.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DockerCodeExecutionService {
    private final DockerClient dockerClient;

    //setting limits for docker container it will be same for all the languages
    private static final long MEMORY_BYTES = 128*1024*1024L;
    private static final long TIMEOUT_SECONDS= 10L;
    private static final String WORKDIR = "/sandbox";
    private static final int TIMEOUT_EXIT_CODE = -1;
    private static final String STDIN_FILE = WORKDIR + "/.stdin";
    private record ExecOutput(String stdout, String stderr, int exitCode) {}


    public Execution executionResultGenerator(Submission submission) throws Exception{
        LanguageConfig config = LanguageConfig.valueOf(submission.getLanguage().name());
        String containerId= null;
        try {
            containerId = createContainer(config);
            dockerClient.startContainerCmd(containerId).exec();
            copySourceCode(containerId, submission.getSourceCode(), config.filename);
            if (config.compileCmd != null) {
                ExecOutput compileOut = execCommand(containerId, config.compileCmd, "");
                if (compileOut.exitCode() != 0) {
                    return buildResult(submission, compileOut, 0L);
                }
            }
            long start = System.currentTimeMillis();
            ExecOutput runOut = execCommand(containerId, config.runCmd, submission.getStdin());
            long elapsed = System.currentTimeMillis() - start;

            return buildResult(submission, runOut, elapsed);

        }
        finally {
            if (containerId != null) {
                cleanupContainer(containerId);
            }
        }

    }

    private String createContainer(LanguageConfig language){
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withMemory(MEMORY_BYTES)
                .withMemorySwap(MEMORY_BYTES)
                .withCpuCount(1L)
                .withNetworkMode("none")//no Internet
                .withCapDrop(Capability.ALL)//drop all linux capabilites
                .withSecurityOpts(List.of("no-new-privileges"))
                .withReadonlyRootfs(true)
                .withTmpFs(Map.of(WORKDIR, "rw,noexec,nosuid,size=32m")); // only /sandbox is writable

        CreateContainerResponse containerResponse = dockerClient
                .createContainerCmd(language.image)
                .withCmd("sleep","infinity")//keep alive for exec
                .withWorkingDir(WORKDIR)
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .exec();
        return containerResponse.getId();
    }

    // Writes content to a file inside the container using base64 encoding
    // This avoids stdin piping which causes "The pipe has been ended" errors on Windows npipe
    private void writeFileInContainer(String containerId, String content, String filePath) throws Exception {
        String encoded = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", "echo '" + encoded + "' | base64 -d > " + filePath)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        dockerClient.execStartCmd(exec.getId())
                .exec(new ExecStartResultCallback())
                .awaitCompletion(5, TimeUnit.SECONDS);
    }

    private void copySourceCode(String containerId, String sourceCode, String fileName) throws Exception {
        writeFileInContainer(containerId, sourceCode, WORKDIR + "/" + fileName);
    }

    // Executes a command inside the container without using stdin streaming.
    // If stdin is needed, it's written to a file first and redirected via shell.
    private ExecOutput execCommand(String containerId, String[] cmd, String stdin) throws Exception {
        // Build the shell command: run the actual command with stdin redirected from a file
        String cmdString = String.join(" ", cmd);
        String shellCmd;

        if (stdin != null && !stdin.isEmpty()) {
            // Write stdin to a file first, then redirect it
            writeFileInContainer(containerId, stdin, STDIN_FILE);
            shellCmd = cmdString + " < " + STDIN_FILE;
        } else {
            shellCmd = cmdString;
        }

        ExecCreateCmdResponse exec = dockerClient
                .execCreateCmd(containerId)
                .withCmd("sh", "-c", shellCmd)
                .withWorkingDir(WORKDIR)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        boolean completed = dockerClient.execStartCmd(exec.getId())
                .exec(new ExecStartResultCallback(stdout, stderr))
                .awaitCompletion(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            return new ExecOutput(
                    stdout.toString(StandardCharsets.UTF_8),
                    "Execution timed out after " + TIMEOUT_SECONDS + " seconds",
                    TIMEOUT_EXIT_CODE
            );
        }

        long exitCode = dockerClient.inspectExecCmd(exec.getId())
                .exec()
                .getExitCodeLong();

        return new ExecOutput(stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8),
                (int) exitCode);
    }

    private void cleanupContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).withTimeout(2).exec();
        } catch (Exception ignored) {}
        try {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception ignored) {}
    }

    private Execution buildResult(Submission submission, ExecOutput out, long elapsedMs) {
        Execution result = new Execution();
        result.setSubmission(submission);
        result.setStdout(out.stdout());
        result.setStderr(out.stderr());
        result.setExitCode(out.exitCode());
        result.setExecutionTimeMs(elapsedMs);
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }
}
