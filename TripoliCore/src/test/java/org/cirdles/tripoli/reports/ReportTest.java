package org.cirdles.tripoli.reports;

import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTest {
    static ReportColumn testColumn;
    static ReportCategory testCategory;
    static Report testReport;

    /**
     * Build report structure using semi-realistic data
     */
    @BeforeAll
    static void setUpBeforeClass(){
        Set<ReportColumn> analysisColumns = new TreeSet<>();
        Set<ReportColumn> isotopicColumns = new TreeSet<>();
        Set<ReportCategory> reportCategories = new TreeSet<>();

        testColumn = new ReportColumn("Analysis", 0, null);
        testCategory = new ReportCategory("Isotopic Ratios", 0);
        testCategory.addColumn(testColumn);

        analysisColumns.add(new ReportColumn("Analysis", 0, null));
        analysisColumns.add(new ReportColumn("Session", 1, null));

        isotopicColumns.add(new ReportColumn("206Pb/238U", 0, null));
        isotopicColumns.add(new ReportColumn("207Pb/235U", 1, null));
        isotopicColumns.add(new ReportColumn("207Pb/206Pb", 2, null));

        reportCategories.add(new ReportCategory("Analysis Info", analysisColumns, 0));
        reportCategories.add(new ReportCategory("Isotopic Ratios", isotopicColumns, 1));

        testReport = new Report("Test Report", "Test Report Method", reportCategories);
    }

    /**
     * Ensures serialized reports maintain data integrity
     * @throws TripoliException
     */
    @Test
    public void reportSerializeTest() throws TripoliException {
        testReport.serializeReport();
        Report deserializedReport = (Report) TripoliSerializer.getSerializedObjectFromFile(testReport.getTripoliReportFile().getAbsolutePath(), true);
        assertTrue(testReport.equals(deserializedReport));
    }

    /**
     * Ensures the overridden equals method properly compares all elements in each class as well
     * as the nested structure of the parent classes
     */
    @Test
    public void reportEqualsTest() {
        ReportColumn dupeColumn = new ReportColumn(testColumn);
        ReportColumn alteredColumn = new ReportColumn(testColumn);
        ReportCategory dupeCategory = new ReportCategory(testCategory);
        ReportCategory alteredCategory = new ReportCategory(testCategory);
        Report dupeReport = new Report(testReport);
        Report alteredReport = new Report(testReport);

        alteredReport.addCategory(new ReportCategory("Test Category", 2));
        alteredCategory.addColumn(new ReportColumn("Test Column", 1, null));
        alteredColumn.setPositionIndex(2);

        assertAll(
                () -> assertTrue(testColumn.equals(dupeColumn), "ReportColumns should be equal"),
                () -> assertTrue(testCategory.equals(dupeCategory), "ReportCategory should be equal"),
                () -> assertTrue(testReport.equals(dupeReport),"Report should be equal"),

                () -> assertFalse(testReport.equals(alteredReport), "Reports should NOT be equal"),
                () -> assertFalse(testCategory.equals(alteredCategory), "Categories should NOT be equal"),
                () -> assertFalse(testColumn.equals(alteredColumn), "Columns should NOT be equal")
        );
    }

}
