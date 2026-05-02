package com.example.aitestgenerator.repository;

import com.example.aitestgenerator.model.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    List<TestRun> findAllByOrderByCreatedAtDesc();
}
