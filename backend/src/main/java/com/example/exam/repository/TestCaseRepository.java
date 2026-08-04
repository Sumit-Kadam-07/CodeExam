package com.example.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.exam.model.TestCase;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
}
