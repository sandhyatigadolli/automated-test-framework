package com.example.test_framework_api.controller;

import com.example.test_framework_api.model.TestStatus;
import com.example.test_framework_api.model.TestCase;
import com.example.test_framework_api.model.TestResult;
import com.example.test_framework_api.model.TestSuite;
import com.example.test_framework_api.model.User;
import com.example.test_framework_api.repository.UserRepository;
import com.example.test_framework_api.repository.TestResultRepository;
import com.example.test_framework_api.repository.TestRunRepository;
import com.example.test_framework_api.repository.TestSuiteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final TestResultRepository testResultRepository;
    private final TestRunRepository testRunRepository;
    private final TestSuiteRepository testSuiteRepository;

    // @GetMapping("/me/stats")
    // public ResponseEntity<?> getMyStats(Authentication authentication) {
    //     String username = authentication.getName();
    //     User user = userRepository.findByUsername(username)
    //             .orElseThrow(() -> new RuntimeException("User not found"));

    //     log.info("Fetching stats for user: {} (ID: {})", username, user.getId());

    //     // Step 1: Get all test suites created by this user
    //     List<TestSuite> userSuites = testSuiteRepository.findByCreatedById(user.getId());
    //     log.info("User {} has {} test suites", username, userSuites.size());

    //     if (userSuites.isEmpty()) {
    //         return ResponseEntity.ok(Map.of(
    //             "passed", 0,
    //             "failed", 0,
    //             "pending", 0,
    //             "passRate", 0.0,
    //             "suiteCount", 0,
    //             "totalTestCases", 0
    //         ));
    //     }

    //     // Step 2: Get all test run IDs from user's suites
    //     Set<Long> testRunIds = userSuites.stream()
    //             .filter(suite -> suite.getTestRun() != null)
    //             .map(suite -> suite.getTestRun().getId())
    //             .collect(Collectors.toSet());

    //     log.info("Found {} test runs for user suites", testRunIds.size());
        
    //     // DIAGNOSTIC: Check which suites have test runs
    //     userSuites.forEach(suite -> {
    //         log.info("Suite '{}' (ID: {}) has TestRun: {}", 
    //             suite.getName(), 
    //             suite.getId(), 
    //             suite.getTestRun() != null ? suite.getTestRun().getId() : "NULL");
    //     });

    //     // Step 3: Get all test results from these test runs
    //     List<TestResult> allResults = testRunIds.stream()
    //             .flatMap(runId -> testResultRepository.findByTestRunId(runId).stream())
    //             .collect(Collectors.toList());

    //     log.info("Found {} total test results", allResults.size());

    //     // DIAGNOSTIC: If no results found, check if suites were executed
    //     if (allResults.isEmpty()) {
    //         log.warn("No test results found! User has {} suites but none have been executed.", userSuites.size());
    //         log.warn("To get pass/fail stats, execute the test suites from the UI.");
    //     }

    //     // Step 4: Count passed, failed, and pending tests
    //     long passedCount = allResults.stream()
    //             .filter(r -> r.getStatus() == TestStatus.PASSED)
    //             .count();
        
    //     long failedCount = allResults.stream()
    //             .filter(r -> r.getStatus() == TestStatus.FAILED)
    //             .count();

    //     // Step 5: Get all test cases from user's suites
    //     long totalTestCases = userSuites.stream()
    //             .filter(suite -> suite.getTestCases() != null)
    //             .mapToLong(suite -> suite.getTestCases().size())
    //             .sum();

    //     // Pending = total test cases - executed tests
    //     long executedTests = passedCount + failedCount;
    //     long pendingCount = totalTestCases - executedTests;
    //     // Ensure pending is never negative
    //     if (pendingCount < 0) pendingCount = 0;

    //     // Calculate pass rate based on executed tests (excluding pending)
    //     double passRate = executedTests > 0 ? (passedCount * 100.0 / executedTests) : 0.0;

    //     Map<String, Object> stats = new HashMap<>();
    //     stats.put("passed", passedCount);
    //     stats.put("failed", failedCount);
    //     stats.put("pending", pendingCount);
    //     stats.put("passRate", passRate);
    //     stats.put("suiteCount", userSuites.size());
    //     stats.put("totalTestCases", totalTestCases);

    //     log.info("Stats for user {}: {} suites, {} test cases ({} passed, {} failed, {} pending, {:.2f}% pass rate)",
    //             username, userSuites.size(), totalTestCases, passedCount, failedCount, pendingCount, passRate);

    //     return ResponseEntity.ok(stats);
    // }

    @GetMapping("/me/stats")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching stats for user: {} (ID: {})", username, user.getId());

        // Step 1: Get all test suites created by this user
        List<TestSuite> userSuites = testSuiteRepository.findByCreatedById(user.getId());
        log.info("User {} has {} test suites", username, userSuites.size());

        if (userSuites.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "passed", 0,
                "failed", 0,
                "pending", 0,
                "passRate", 0.0,
                "suiteCount", 0,
                "totalTestCases", 0
            ));
        }

        // Step 2: Get suite IDs for fetching test results
        Set<Long> suiteIds = userSuites.stream()
                .map(TestSuite::getId)
                .collect(Collectors.toSet());

        log.info("Fetching results for {} suite IDs", suiteIds.size());

        // Step 3: Get all test results by suite ID (not test run ID)
        List<TestResult> allResults = suiteIds.stream()
                .flatMap(suiteId -> testResultRepository.findByTestSuiteId(suiteId).stream())
                .collect(Collectors.toList());

        log.info("Found {} total test results from suite IDs", allResults.size());

        // DIAGNOSTIC: If no results found, check if suites were executed
        if (allResults.isEmpty()) {
            log.warn("No test results found! User has {} suites but none have been executed.", userSuites.size());
            log.warn("To get pass/fail stats, execute the test suites from the UI.");
        } else {
            log.info("Results breakdown - PASSED: {}, FAILED: {}", 
                allResults.stream().filter(r -> r.getStatus() == TestStatus.PASSED).count(),
                allResults.stream().filter(r -> r.getStatus() == TestStatus.FAILED).count());
        }

        // Step 4: Count passed, failed, and pending tests
        long passedCount = allResults.stream()
                .filter(r -> r.getStatus() == TestStatus.PASSED)
                .count();
        
        long failedCount = allResults.stream()
                .filter(r -> r.getStatus() == TestStatus.FAILED)
                .count();

        // Step 5: Get all test cases from user's suites
        long totalTestCases = userSuites.stream()
                .filter(suite -> suite.getTestCases() != null)
                .mapToLong(suite -> suite.getTestCases().size())
                .sum();

        // Pending = total test cases - executed tests
        long executedTests = passedCount + failedCount;
        long pendingCount = totalTestCases - executedTests;
        // Ensure pending is never negative
        if (pendingCount < 0) pendingCount = 0;

        // Calculate pass rate based on executed tests (excluding pending)
        double passRate = executedTests > 0 ? (passedCount * 100.0 / executedTests) : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("passed", passedCount);
        stats.put("failed", failedCount);
        stats.put("pending", pendingCount);
        stats.put("passRate", passRate);
        stats.put("suiteCount", userSuites.size());
        stats.put("totalTestCases", totalTestCases);

        log.info("Stats for user {}: {} suites, {} test cases ({} passed, {} failed, {} pending, {:.2f}% pass rate)",
                username, userSuites.size(), totalTestCases, passedCount, failedCount, pendingCount, passRate);

        return ResponseEntity.ok(stats);
    }
    
    // @GetMapping("/me/stats")
    // public ResponseEntity<?> getMyStats(Authentication authentication) {
    //     String username = authentication.getName();
    //     User user = userRepository.findByUsername(username)
    //             .orElseThrow(() -> new RuntimeException("User not found"));

    //     log.info("Fetching stats for user: {} (ID: {})", username, user.getId());

    //     // Step 1: Get all test suites created by this user
    //     List<TestSuite> userSuites = testSuiteRepository.findByCreatedById(user.getId());
    //     log.info("User {} has {} test suites", username, userSuites.size());

    //     if (userSuites.isEmpty()) {
    //         return ResponseEntity.ok(Map.of(
    //             "passed", 0,
    //             "failed", 0,
    //             "pending", 0,
    //             "passRate", 0.0,
    //             "suiteCount", 0,
    //             "totalTestCases", 0
    //         ));
    //     }

    //     // Step 2: Get all test run IDs from user's suites
    //     Set<Long> testRunIds = userSuites.stream()
    //             .filter(suite -> suite.getTestRun() != null)
    //             .map(suite -> suite.getTestRun().getId())
    //             .collect(Collectors.toSet());

    //     log.info("Found {} test runs for user suites", testRunIds.size());

    //     // Step 3: Get all test results from these test runs
    //     List<TestResult> allResults = testRunIds.stream()
    //             .flatMap(runId -> testResultRepository.findByTestRunId(runId).stream())
    //             .collect(Collectors.toList());

    //     log.info("Found {} total test results", allResults.size());

    //     // Step 4: Count passed, failed, and pending tests
    //     long passedCount = allResults.stream()
    //             .filter(r -> r.getStatus() == TestStatus.PASSED)
    //             .count();
        
    //     long failedCount = allResults.stream()
    //             .filter(r -> r.getStatus() == TestStatus.FAILED)
    //             .count();

    //     // Step 5: Get all test cases from user's suites
    //     long totalTestCases = userSuites.stream()
    //             .filter(suite -> suite.getTestCases() != null)
    //             .mapToLong(suite -> suite.getTestCases().size())
    //             .sum();

    //     // Pending = total test cases - executed tests
    //     long executedTests = passedCount + failedCount;
    //     long pendingCount = totalTestCases - executedTests;
    //     // Ensure pending is never negative
    //     if (pendingCount < 0) pendingCount = 0;

    //     // Calculate pass rate based on executed tests (excluding pending)
    //     double passRate = executedTests > 0 ? (passedCount * 100.0 / executedTests) : 0.0;

    //     Map<String, Object> stats = new HashMap<>();
    //     stats.put("passed", passedCount);
    //     stats.put("failed", failedCount);
    //     stats.put("pending", pendingCount);
    //     stats.put("passRate", passRate);
    //     stats.put("suiteCount", userSuites.size());
    //     stats.put("totalTestCases", totalTestCases);

    //     log.info("Stats for user {}: {} suites, {} test cases ({} passed, {} failed, {} pending, {:.2f}% pass rate)",
    //             username, userSuites.size(), totalTestCases, passedCount, failedCount, pendingCount, passRate);

    //     return ResponseEntity.ok(stats);
    // }

    // FIX: Return safe DTOs without circular references
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> userDtos = users.stream()
            .map(user -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", user.getId());
                dto.put("username", user.getUsername());
                dto.put("email", user.getEmail());
                dto.put("roles", user.getRoles());
                dto.put("enabled", user.isEnabled());
                dto.put("createdAt", user.getCreatedAt());
                
                // Calculate test execution count
                long testExecutions = testResultRepository.findAll().stream()
                    .filter(r -> r.getExecutedBy() != null && r.getExecutedBy().getId().equals(user.getId()))
                    .count();
                dto.put("testExecutions", testExecutions);
                
                long testRunsCreated = testRunRepository.findAll().stream()
                    .filter(r -> r.getCreatedBy() != null && r.getCreatedBy().getId().equals(user.getId()))
                    .count();
                dto.put("testRunsCreated", testRunsCreated);
                
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        long totalTests = testResultRepository.findAll().stream()
            .filter(r -> r.getExecutedBy() != null && r.getExecutedBy().getId().equals(id))
            .count();

        long passedTests = testResultRepository.findAll().stream()
            .filter(r -> r.getExecutedBy() != null && r.getExecutedBy().getId().equals(id))
            .filter(r -> r.getStatus().name().equals("PASSED"))
            .count();

        long failedTests = totalTests - passedTests;

        long testRunsCreated = testRunRepository.findAll().stream()
            .filter(r -> r.getCreatedBy() != null && r.getCreatedBy().getId().equals(id))
            .count();

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("username", user.getUsername());
        dto.put("email", user.getEmail());
        dto.put("roles", user.getRoles());
        dto.put("enabled", user.isEnabled());
        dto.put("createdAt", user.getCreatedAt());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", totalTests);
        stats.put("passedTests", passedTests);
        stats.put("failedTests", failedTests);
        stats.put("testRunsCreated", testRunsCreated);
        stats.put("passRate", totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0.0);
        dto.put("statistics", stats);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Set<String> roles = user.getRoles();
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add("ROLE_ADMIN");
        user.setRoles(roles);
        userRepository.save(user);

        log.info("User {} promoted to ADMIN", user.getUsername());

        return ResponseEntity.ok(Map.of(
            "message", "User promoted to admin",
            "username", user.getUsername(),
            "roles", user.getRoles()
        ));
    }

    @PutMapping("/{id}/demote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> demoteToUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);
        userRepository.save(user);

        log.info("User {} demoted to USER", user.getUsername());

        return ResponseEntity.ok(Map.of(
            "message", "Admin demoted to user",
            "username", user.getUsername(),
            "roles", user.getRoles()
        ));
    }

    // @GetMapping("/me/tests")
    // public ResponseEntity<?> getMyTests(Authentication authentication) {
    //     String username = authentication.getName();
    //     User user = userRepository.findByUsername(username).orElseThrow();

    //     List<Map<String, Object>> myTests = testResultRepository.findAll().stream()
    //         .filter(r -> r.getExecutedBy() != null && r.getExecutedBy().getId().equals(user.getId()))
    //         .map(result -> {
    //             Map<String, Object> dto = new HashMap<>();
    //             dto.put("testName", result.getTestName());
    //             dto.put("status", result.getStatus().toString());
    //             dto.put("duration", result.getDuration() != null ? result.getDuration() : 0L);
    //             dto.put("createdAt", result.getCreatedAt());
    //             dto.put("retryCount", result.getRetryCount() != null ? result.getRetryCount() : 0);
    //             return dto;
    //         })
    //         .collect(Collectors.toList());

    //     long passedCount = myTests.stream()
    //         .filter(t -> "PASSED".equals(t.get("status")))
    //         .count();

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("username", username);
    //     response.put("totalTests", myTests.size());
    //     response.put("passedTests", passedCount);
    //     response.put("failedTests", myTests.size() - passedCount);
    //     response.put("passRate", myTests.size() > 0 ? (passedCount * 100.0 / myTests.size()) : 0.0);
    //     response.put("tests", myTests);

    //     return ResponseEntity.ok(response);
    // }

    @GetMapping("/me/tests")
    public ResponseEntity<?> getMyTests(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        List<Map<String, Object>> myTests = testResultRepository.findAll().stream()
            .filter(r -> r.getExecutedBy() != null && r.getExecutedBy().getId().equals(user.getId()))
            .map(result -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("testName", result.getTestName());
                dto.put("status", result.getStatus().toString());
                dto.put("duration", result.getDuration() != null ? result.getDuration() : 0L);
                dto.put("createdAt", result.getCreatedAt());
                dto.put("retryCount", result.getRetryCount() != null ? result.getRetryCount() : 0);
                return dto;
            })
            .collect(Collectors.toList());

        long passedCount = myTests.stream()
            .filter(t -> "PASSED".equals(t.get("status")))
            .count();

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("totalTests", myTests.size());
        response.put("passedTests", passedCount);
        response.put("failedTests", myTests.size() - passedCount);
        response.put("passRate", myTests.size() > 0 ? (passedCount * 100.0 / myTests.size()) : 0.0);
        response.put("tests", myTests);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        long totalTests = testResultRepository.findAll().stream()
            .filter(r -> r.getExecutedBy() != null && r.getExecutedBy().getId().equals(user.getId()))
            .count();

        long testRunsCreated = testRunRepository.findAll().stream()
            .filter(r -> r.getCreatedBy() != null && r.getCreatedBy().getId().equals(user.getId()))
            .count();

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("username", user.getUsername());
        dto.put("email", user.getEmail());
        dto.put("roles", user.getRoles());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("testExecutions", totalTests);
        stats.put("testRunsCreated", testRunsCreated);
        dto.put("statistics", stats);

        return ResponseEntity.ok(dto);
    }
}