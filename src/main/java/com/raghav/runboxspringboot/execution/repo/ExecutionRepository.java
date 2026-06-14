package com.raghav.runboxspringboot.execution.repo;

import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
    Optional<Execution> findBySubmissionSubmissionIdAndSubmissionUser(UUID submissionId, User user);

    List<Execution> findBySubmissionUser(User user);
}
