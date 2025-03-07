package org.cirdles.tripoli.reports;

import org.junit.jupiter.api.Test;


import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTest {
    @Test
    public void reportEqualsTest() {
        Set<ReportColumn> reportColumns1 = new TreeSet<>();
        Set<ReportColumn> reportColumns2 = new TreeSet<>();
        Set<ReportCategory> categorySet1 = new TreeSet<>();
        Set<ReportCategory> categorySet2 = new TreeSet<>();

        ReportColumn column1 = new ReportColumn("Analysis", 0);
        ReportColumn column2 = new ReportColumn("Session", 1);
        ReportColumn columnDupe = new ReportColumn("Analysis", 0);
        ReportColumn column3 = new ReportColumn("Analysis", 0);
        ReportColumn column4 = new ReportColumn("Session", 1);

        reportColumns1.add(column1);
        reportColumns1.add(column2);
        reportColumns2.add(column3);
        reportColumns2.add(column4);

        ReportCategory reportCategory1 = new ReportCategory("Info", reportColumns1, 0);
        ReportCategory reportCategory2 = new ReportCategory("AdditonalInfo", reportColumns2, 1);
        ReportCategory reportCategoryDupe = new ReportCategory("Info", reportColumns2, 0);
        ReportCategory reportCategory3 = new ReportCategory("Info", reportColumns1, 0);
        ReportCategory reportCategory4 = new ReportCategory("AdditonalInfo", reportColumns2, 1);

        categorySet1.add(reportCategory1);
        categorySet1.add(reportCategory2);
        categorySet2.add(reportCategory3);
        categorySet2.add(reportCategory4);

        Report report1 = new Report("TestReport", "", categorySet1);
        Report report2 = new Report("TestReport", "", categorySet2);
        Report reportCopy = new Report(report2);
        Report reportCopy2 = new Report(report1);
        reportCopy2.getCategories().add(new ReportCategory("Test", 2));

        assertAll(
                () -> assertTrue(column1.equals(columnDupe), "ReportColumns not equal"),
                () -> assertTrue(reportCategory1.equals(reportCategoryDupe), "ReportCategory not equal"),
                () -> assertTrue(report1.equals(report2),"Report not equal"),
                () -> assertTrue(report1.equals(reportCopy), "Copy not equal"),
                () -> assertFalse(report1.equals(reportCopy2), "Report shouldnt be equal")
        );
    }
}
