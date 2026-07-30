package com.openclassrooms.starterjwt.testing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public final class TestRatioReporter {

    private static final String REPORT_MARKER_START = "<!-- test-ratio:start -->";
    private static final String REPORT_MARKER_END = "<!-- test-ratio:end -->";

    private TestRatioReporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Expected the Surefire and Failsafe report directories, the minimum ratio and the JaCoCo index.");
        }

        Path surefireReports = Path.of(args[0]);
        Path failsafeReports = Path.of(args[1]);
        BigDecimal minimumRatio = new BigDecimal(args[2]);
        Path jacocoIndex = Path.of(args[3]);

        long unitTestCount = countTestCases(surefireReports);
        long integrationTestCount = countTestCases(failsafeReports);
        long totalTestCount = unitTestCount + integrationTestCount;

        if (totalTestCount == 0) {
            throw new IllegalStateException("No test cases were found in the Surefire and Failsafe reports.");
        }

        BigDecimal actualRatio = BigDecimal.valueOf(integrationTestCount)
                .divide(BigDecimal.valueOf(totalTestCount), 4, RoundingMode.HALF_UP);

        System.out.printf(
                "Test ratio: %d integration / %d total = %.2f%%%n",
                integrationTestCount,
                totalTestCount,
                actualRatio.movePointRight(2));

        boolean targetReached = BigDecimal.valueOf(integrationTestCount)
                .compareTo(BigDecimal.valueOf(totalTestCount).multiply(minimumRatio)) >= 0;

        addRatioToJacocoReport(
                jacocoIndex,
                integrationTestCount,
                totalTestCount,
                actualRatio,
                minimumRatio,
                targetReached);
    }

    private static long countTestCases(Path reportDirectory) throws Exception {
        if (!Files.isDirectory(reportDirectory)) {
            return 0;
        }

        try (Stream<Path> reports = Files.list(reportDirectory)) {
            return reports
                    .filter(Files::isRegularFile)
                    .filter(TestRatioReporter::isXmlTestReport)
                    .mapToLong(TestRatioReporter::readTestCount)
                    .sum();
        }
    }

    private static void addRatioToJacocoReport(
            Path jacocoIndex,
            long integrationTestCount,
            long totalTestCount,
            BigDecimal actualRatio,
            BigDecimal minimumRatio,
            boolean targetReached) throws Exception {
        if (!Files.isRegularFile(jacocoIndex)) {
            throw new IllegalStateException("JaCoCo report was not found at " + jacocoIndex);
        }

        String status = targetReached ? "TARGET REACHED" : "BELOW TARGET";
        String color = targetReached ? "#4f8a10" : "#9f6000";
        String ratioBlock = """
                %s
                <div style="margin: 1em 0; padding: 0.8em; border: 1px solid %s;">
                  <strong>Integration test ratio:</strong>
                  %d / %d tests (%s%%), minimum %s%% -
                  <strong style="color: %s;">%s</strong>
                </div>
                %s
                """.formatted(
                REPORT_MARKER_START,
                color,
                integrationTestCount,
                totalTestCount,
                actualRatio.movePointRight(2).setScale(2, RoundingMode.HALF_UP),
                minimumRatio.movePointRight(2).setScale(2, RoundingMode.HALF_UP),
                color,
                status,
                REPORT_MARKER_END);

        String report = Files.readString(jacocoIndex, StandardCharsets.UTF_8);
        int existingBlockStart = report.indexOf(REPORT_MARKER_START);
        int existingBlockEnd = report.indexOf(REPORT_MARKER_END);

        if (existingBlockStart >= 0 && existingBlockEnd >= existingBlockStart) {
            int endAfterMarker = existingBlockEnd + REPORT_MARKER_END.length();
            report = report.substring(0, existingBlockStart)
                    + ratioBlock
                    + report.substring(endAfterMarker);
        } else {
            String headingEnd = "</h1>";
            int insertionPoint = report.indexOf(headingEnd);
            if (insertionPoint < 0) {
                throw new IllegalStateException("Unable to locate the JaCoCo report heading in " + jacocoIndex);
            }
            insertionPoint += headingEnd.length();
            report = report.substring(0, insertionPoint)
                    + ratioBlock
                    + report.substring(insertionPoint);
        }

        Files.writeString(jacocoIndex, report, StandardCharsets.UTF_8);
    }

    private static boolean isXmlTestReport(Path report) {
        String fileName = report.getFileName().toString();
        return fileName.startsWith("TEST-") && fileName.endsWith(".xml");
    }

    private static long readTestCount(Path report) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document reportDocument = factory.newDocumentBuilder().parse(report.toFile());
            return Long.parseLong(reportDocument.getDocumentElement().getAttribute("tests"));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read test report " + report, exception);
        }
    }
}
