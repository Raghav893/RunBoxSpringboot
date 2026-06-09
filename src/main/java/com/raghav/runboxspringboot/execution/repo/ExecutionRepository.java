package com.raghav.runboxspringboot.execution.repo;

import com.raghav.runboxspringboot.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
}
