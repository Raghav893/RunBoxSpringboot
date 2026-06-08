package com.raghav.runboxspringboot.submit.repo;

import com.raghav.runboxspringboot.submit.dto.SubmissionResponseDTO;
import com.raghav.runboxspringboot.submit.entity.Submission;
import com.raghav.runboxspringboot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> getSubmissionsBySubmissionIdAndUser(UUID submissionId, User user);

    List<Submission> getSubmissionsByUser(User user);
}
