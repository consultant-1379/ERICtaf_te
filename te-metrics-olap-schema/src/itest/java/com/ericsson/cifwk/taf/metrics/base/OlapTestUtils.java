package com.ericsson.cifwk.taf.metrics.base;
/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.olap4j.CellSet;
import org.olap4j.OlapException;
import org.olap4j.layout.CellSetFormatter;
import org.olap4j.layout.RectangularCellSetFormatter;
import org.olap4j.query.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;

public class OlapTestUtils {
    public static void execute(Query myQuery) throws OlapException {
        myQuery.validate();

        System.out.println("==========================================================");
        System.out.println(myQuery.getSelect().toString());
        System.out.println("==========================================================");

        CellSet cellSet = myQuery.execute();

        printResults(cellSet);
    }

    public static void printResults(CellSet execute) {
        CellSetFormatter formatter = new RectangularCellSetFormatter(false);
        PrintWriter writer = new PrintWriter(System.out);
        formatter.format(execute, writer);
        writer.flush();
    }

    public static String[][] olapToTable(CellSet actual) {
        final char HEADER_SPLITTER = '+';
        final char ROW_SPLITTER = '|';

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CellSetFormatter formatter = new RectangularCellSetFormatter(false);
        PrintWriter writer = new PrintWriter(output);
        formatter.format(actual, writer);
        writer.flush();

        String[] rows = output.toString().split(System.lineSeparator());

        int headerRow = findSplittingRow(rows);
        int columnsCnt = CharMatcher.is(HEADER_SPLITTER).countIn(rows[headerRow]);

        formatHeaders(HEADER_SPLITTER, ROW_SPLITTER, rows, headerRow);

        return splitTable(rows, ROW_SPLITTER, columnsCnt, 1, 1);
    }

    public static String[][] splitTable(String[] rows, char rowSplitter, int columnsCnt, int skipLeft, int skipTop) {
        CharMatcher trimmer = CharMatcher.anyOf("\" ");
        String[][] result = new String[rows.length - skipTop][columnsCnt - skipLeft];
        for (int i = skipTop; i < rows.length; i++) {
            String[] column = splitQuoted(rows[i], rowSplitter);
            for (int j = skipLeft; j < columnsCnt; j++) {
                result[i - skipTop][j - skipLeft] = trimmer.trimFrom(column[j]);
            }
        }
        return result;
    }

    protected static String[] splitQuoted(String row, char rowSplitter) {
        List<String> result = Lists.newArrayList();
        String[] columns = row.split("\\" + rowSplitter);

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if (column.startsWith("\"")) {
                StringBuilder buffer = new StringBuilder();
                for (int j = i; j < columns.length; j++) {
                    buffer.append(columns[j]);
                    buffer.append(",");
                    if (columns[j].endsWith("\"")) {
                        buffer.deleteCharAt(0);
                        buffer.setLength(buffer.length() - 2);
                        column = buffer.toString();
                        i = j;
                        break;
                    }
                }
            }
            result.add(column);
        }

        return result.toArray(new String[]{});
    }

    private static void formatHeaders(char HEADER_SPLITTER, char ROW_SPLITTER, String[] rows, int headerRow) {
        char[] header = rows[headerRow].toCharArray();
        for (int i = headerRow; i > 0; i--) { //shift headers down, overwrite line
            StringBuilder builder = new StringBuilder(rows[i - 1]);
            for (int j = 0; j < header.length; j++) {
                if (header[j] == HEADER_SPLITTER) {
                    builder.setCharAt(j, ROW_SPLITTER);
                }
            }
            rows[i] = builder.toString();
        }
    }

    private static int findSplittingRow(String[] lines) {
        CharMatcher charMatcher = CharMatcher.anyOf("+-");

        for (int i = 0; i < lines.length; i++) {
            if (charMatcher.matchesAllOf(lines[i])) {
                return i;
            }
        }

        throw new IllegalStateException("Unable to find splitting row");
    }


    public static Matcher<? super String[][]> matchesTable(final String[][] actualTable) {
        return new TypeSafeMatcher<String[][]>() {
            private List<String> mismatches = Lists.newArrayList();
            private final int mismatchesMax = 3;

            @Override
            protected boolean matchesSafely(String[][] expectedTable) {
                if (expectedTable.length != actualTable.length) {
                    mismatches.add(format("Tables has different lengths (Expected %s, actual %s)", expectedTable.length, actualTable.length));
                    return false;
                }
                for (int i = 0; i < expectedTable.length; i++) {
                    if (expectedTable[i].length != actualTable[i].length) {
                        mismatches.add(format("Row %s has different lengths (Expected %s, actual %s)", i, expectedTable[i].length, actualTable[i].length));
                    } else {
                        for (int j = 0; j < expectedTable[i].length; j++) {
                            if (!expectedTable[i][j].equals(actualTable[i][j])) {
                                mismatches.add(format("Cell value \"%s\" at [%s,%s] does not match actual \"%s\"\n", expectedTable[i][j], i, j, actualTable[i][j]));
                            }
                        }
                    }
                }

                return mismatches.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                int mismatchesCnt = mismatches.size();
                for (int i = 0; i < mismatchesCnt && i < mismatchesMax; i++) {
                    description.appendText(mismatches.get(i));
                }

                if (mismatchesCnt > mismatchesMax) {
                    description.appendText(format("And %s more...", mismatchesCnt - mismatchesMax));
                }
            }
        };
    }

    public static String[][] csvToTable(String filename) throws IOException {
        String csv;
        try {
            URI uri = ClassLoader.getSystemResource("cubes/" + filename).toURI();
            csv = new String(Files.readAllBytes(Paths.get(uri)));
        } catch (URISyntaxException | NullPointerException e) {
            throw new IllegalArgumentException("cubes/" + filename + " not found");
        }
        String[] rows = csv.split(System.lineSeparator());
        int columnsCnt = CharMatcher.is(',').countIn(rows[0])+1;

        return splitTable(rows, ',', columnsCnt, 0, 0);
    }
}
