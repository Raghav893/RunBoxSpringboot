package com.raghav.runboxspringboot.execution.entity;

import com.raghav.runboxspringboot.submit.entity.Submission;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_results")
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(columnDefinition = "TEXT")
    private String stdout;

    @Column(columnDefinition = "TEXT")
    private String stderr;

    @Column(nullable = false)
    private Integer exitCode;

    @Column(nullable = false)
    private Long executionTimeMs;

    private Long memoryUsedBytes;
    private LocalDateTime completedAt;
}
