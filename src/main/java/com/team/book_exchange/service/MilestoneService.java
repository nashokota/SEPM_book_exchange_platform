package com.team.book_exchange.service;

import com.team.book_exchange.dto.view.MilestoneView;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MilestoneService {

    private static final Pattern MERGE_BRANCH_PATTERN =
        Pattern.compile("Merge pull request #\\d+ from [^/]+/(feature/[a-z0-9-]+)");

    public List<MilestoneView> getMilestones() {
        List<MilestoneView> milestones = loadFromGitHistory();
        return milestones.isEmpty() ? fallbackMilestones() : milestones;
    }

    private List<MilestoneView> loadFromGitHistory() {
        Path repositoryRoot = findRepositoryRoot();
        if (repositoryRoot == null) {
            return List.of();
        }

        List<String> mergeLines = runGitCommand(
            repositoryRoot,
            "log",
            "--first-parent",
            "--merges",
            "--pretty=format:%H|%h|%cs|%s",
            "HEAD"
        );

        List<MilestoneView> milestones = new ArrayList<>();
        for (String mergeLine : mergeLines) {
            MilestoneView milestone = toMilestone(repositoryRoot, mergeLine);
            if (milestone != null) {
                milestones.add(milestone);
            }
        }

        return milestones;
    }

    private MilestoneView toMilestone(Path repositoryRoot, String mergeLine) {
        String[] parts = mergeLine.split("\\|", 4);
        if (parts.length != 4) {
            return null;
        }

        String commitHash = parts[0];
        String shortCommitHash = parts[1];
        LocalDate mergedDate = LocalDate.parse(parts[2]);
        String mergeSubject = parts[3];

        Matcher matcher = MERGE_BRANCH_PATTERN.matcher(mergeSubject);
        if (!matcher.find()) {
            return null;
        }

        String branchName = matcher.group(1);
        if ("feature/ui-redesign".equals(branchName)) {
            return null;
        }

        String branchSubject = firstLine(
            runGitCommand(repositoryRoot, "show", "-s", "--format=%s", commitHash + "^2")
        );

        String normalizedSubject = branchSubject != null ? branchSubject : mergeSubject;
        String category = categoryFromSubject(normalizedSubject);
        String title = cleanTitle(normalizedSubject, branchName);

        return new MilestoneView(
            branchName,
            formatBranchLabel(branchName),
            title,
            category,
            shortCommitHash,
            mergedDate
        );
    }

    private String categoryFromSubject(String subject) {
        String normalized = subject.toLowerCase(Locale.ROOT);

        if (normalized.startsWith("feat:")) {
            return "Feature";
        }
        if (normalized.startsWith("test:")) {
            return "Testing";
        }
        if (normalized.startsWith("ci:")) {
            return "CI/CD";
        }
        if (normalized.startsWith("chore:")) {
            return "Platform";
        }

        return "Milestone";
    }

    private String cleanTitle(String subject, String branchName) {
        String cleaned = subject.replaceFirst("^[a-z]+:\\s*", "").trim();
        if (cleaned.isBlank() || cleaned.toLowerCase(Locale.ROOT).startsWith("merge ")) {
            return formatBranchLabel(branchName);
        }

        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private String formatBranchLabel(String branchName) {
        String label = branchName.replace("feature/", "").replace('-', ' ').trim();
        String[] words = label.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }

        return builder.toString();
    }

    private Path findRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();

        while (current != null) {
            if (Files.exists(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }

    private List<String> runGitCommand(Path repositoryRoot, String... arguments) {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(repositoryRoot.toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            List<String> outputLines;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                outputLines = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();
            }

            int exitCode = process.waitFor();
            return exitCode == 0 ? outputLines : List.of();
        } catch (IOException ex) {
            return List.of();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    private String firstLine(List<String> lines) {
        return lines.stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    private List<MilestoneView> fallbackMilestones() {
        return List.of(
            new MilestoneView("feature/render-deployment", "Render Deployment", "Configure staged CI and Render deployment from main", "CI/CD", "3c85fce", null),
            new MilestoneView("feature/testing", "Testing", "Strengthen automated test execution with Surefire and Failsafe", "Testing", "5311df3", null),
            new MilestoneView("feature/full-dockerization", "Full Dockerization", "Dockerize the full application with app and PostgreSQL services", "Platform", "d6ccb26", null),
            new MilestoneView("feature/controller-tests", "Controller Tests", "Add controller integration tests", "Testing", "058a048", null),
            new MilestoneView("feature/rest-api-foundation", "REST API Foundation", "Add REST API foundation for books, categories, and requests", "Feature", "d1b6d88", null),
            new MilestoneView("feature/request-flow", "Request Flow", "Add buyer request flow and seller approval actions", "Feature", "8501b1d", null),
            new MilestoneView("feature/public-book-search-filter-pagination", "Public Book Search Filter Pagination", "Add public book search, filter, and pagination", "Feature", "707d027", null),
            new MilestoneView("feature/public-book-browsing", "Public Book Browsing", "Add public book browsing and book details pages", "Feature", "d7ae568", null),
            new MilestoneView("feature/book-listing-foundation", "Book Listing Foundation", "Add seller book listing management foundation", "Feature", "9dc3cb5", null),
            new MilestoneView("feature/category-management", "Category Management", "Add admin category management", "Feature", "9b92938", null),
            new MilestoneView("feature/seller-application-flow", "Seller Application Flow", "Add seller application submission and admin approval flow", "Feature", "58fea93", null),
            new MilestoneView("feature/security-registration-login", "Security Registration Login", "Add buyer registration, login, and role-based page access", "Feature", "a18cad0", null),
            new MilestoneView("feature/auth-domain-foundation", "Auth Domain Foundation", "Add user role and seller application domain foundation", "Feature", "10995e9", null),
            new MilestoneView("feature/project-bootstrap", "Project Bootstrap", "Bootstrap the Spring Boot app with PostgreSQL and a home page", "Platform", "c6b01c1", null)
        );
    }
}
