package com.raghav.runboxspringboot.execution.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.raghav.runboxspringboot.execution.config.LanguageConfig;
import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.submit.entity.Language;
import com.raghav.runboxspringboot.submit.entity.Submission;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DockerCodeExecutionService {
    private final DockerClient dockerClient;

    //setting limits for docker container it will be same for all the languages
    private static final long MEMORY_BYTES = 128*1024*1024L;
    private static final long TIMEOUT_SECONDS= 10L;
    private static final String WORKDIR = "/sandbox";


    public Execution executionResultGenerator(Submission submission){
        Language lang = submission.getLanguage();
        String containerId= null;

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
}
