package com.raghav.runboxspringboot.execution.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.raghav.runboxspringboot.execution.config.LanguageConfig;
import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.submit.entity.Language;
import com.raghav.runboxspringboot.submit.entity.Submission;
import lombok.RequiredArgsConstructor;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    //The sdk requires a .tar file which the sdk file uses to write code in docker
    //  so what are we gonna do  is we are  going to write source code that is given in submission
    // and create a byte array which is going to stream using a dependency which is compressed Tar
    // and stream code into  it and create .Tar file which will help us to give code to sdk

    private void copySourceCode(String containerId,String sourceCode,String fileName ) throws Exception {
        byte[] tarBytes = createTar(fileName,sourceCode.getBytes(StandardCharsets.UTF_8));

        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(new ByteArrayInputStream(tarBytes))
                .withRemotePath(WORKDIR)
                .exec();
    }
    private byte[] createTar(String fileName, byte[] content) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bos)){
            TarArchiveEntry entry = new TarArchiveEntry(fileName);
            entry.setSize(content.length);
            tarArchiveOutputStream.putArchiveEntry(entry);
            tarArchiveOutputStream.write(content);
            tarArchiveOutputStream.closeArchiveEntry();

        }
        return bos.toByteArray();
    }
    private ExecOutput execCommand(String containerId, String[] cmd, String stdin)throws Exception{
        ExecCreateCmdResponse exec = dockerClient
                .execCreateCmd(containerId)
                .withCmd(cmd)
                .withWorkingDir(WORKDIR)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .exec();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();


        boolean completed = dockerClient.execStartCmd(exec.getId())
                .withStdIn(new ByteArrayInputStream((stdin == null ? "" : stdin).getBytes(StandardCharsets.UTF_8)))
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
