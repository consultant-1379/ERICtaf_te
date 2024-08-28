package com.ericsson.cifwk.taf.metrics;

import com.ericsson.cifwk.taf.metrics.base.OlapServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;

import java.util.List;

import static com.ericsson.cifwk.taf.metrics.base.OlapTestUtils.csvToTable;
import static com.ericsson.cifwk.taf.metrics.base.OlapTestUtils.execute;
import static com.ericsson.cifwk.taf.metrics.base.OlapTestUtils.matchesTable;
import static com.ericsson.cifwk.taf.metrics.base.OlapTestUtils.olapToTable;
import static com.ericsson.cifwk.taf.metrics.base.OlapTestUtils.printResults;
import static org.hamcrest.MatcherAssert.assertThat;

public class OlapTest {

    public static final String CUBE_NAME = "Performance Testing";
    public static final String DIMENSION_MEASURE = "Measures";
    public static final String DIMENSION_TARGET = "Target";

    OlapServer olapServer;

    @Before
    public void setUp() {
        olapServer = new OlapServer();
        olapServer.connect();
    }

    @Test
    public void queryExecution() throws Exception {
        CellSet query = olapServer.mdx("SELECT\n" +
                "NON EMPTY {Hierarchize({[Measures].[Avg Response Time]})} ON COLUMNS,\n" +
                "NON EMPTY {Hierarchize({[VUser.VUser Hierarchy].[VUser].Members})} ON ROWS\n" +
                "FROM [Performance Testing]");

        assertThat(new String[][]{
                {"", "Avg Response Time"},
                {"vuser1", "1,055"}
        }, matchesTable(
                olapToTable(query)
        ));
    }

    @Test
    public void complexQueryExecution() throws Exception {
        CellSet query = olapServer.mdx("SELECT\n" +
                "NON EMPTY {Hierarchize({{[Measures].[Avg Response Time], [Measures].[Success Rate]}})} ON COLUMNS,\n" +
                "NON EMPTY {Hierarchize({{[Test.Test Hierarchy].[Test Suite].Members}, {[Test.Test Hierarchy].[Test Case].Members}})} ON ROWS\n" +
                "FROM [Performance Testing]");

        String[][] expectedTable = csvToTable("complex_query.csv");
        String[][] actualTable = olapToTable(query);
        assertThat(expectedTable, matchesTable(actualTable));
    }

    @Test
    @Ignore
    public void standardDeviation() throws Exception {
        CellSet query = olapServer.mdx("SELECT\n" +
                "NON EMPTY {[Measures].[Standard Deviation]} ON COLUMNS,\n" +
                "NON EMPTY {[Test.Test Hierarchy].[All Test.Test Hierarchys]} ON ROWS\n" +
                "FROM [Performance Testing]");

        assertThat(new String[][]{
                {"", "Standard Deviation"},
                {"All Test.Test Hierarchys", "567"}
        }, matchesTable(
                olapToTable(query)
        ));
    }

    @Test
    public void rptReport() throws Exception {
        CellSet query = olapServer.mdx(
                "SELECT\n" +
                "NON EMPTY {" +
                        "[Measures].[Success Count]," +
                        "[Measures].[Error Count]," +
                        "[Measures].[Avg Response Time]," +
                        "[Measures].[Min Response Time]," +
                        "[Measures].[Max Response Time]" +
                        "} ON COLUMNS,\n" +
                "NON EMPTY {{[Target.Target Hierarchy].[URI].Members}} ON ROWS\n" +
                "FROM [Performance Testing]");

        String[][] expectedTable = csvToTable("rpt_report.csv");
        String[][] actualTable = olapToTable(query);
        assertThat(expectedTable, matchesTable(actualTable));
    }

    @Test
    public void basicQuery() throws Exception {
        OlapConnection connection = olapServer.getOlapConnection();
        Cube cube = connection.getOlapSchema().getCubes().get(CUBE_NAME);

        Query query = new Query("q", cube);

        QueryDimension measuresDim = query.getDimension(DIMENSION_MEASURE);
        query.getAxis(Axis.COLUMNS).addDimension(measuresDim);

        QueryDimension targetDim = query.getDimension(DIMENSION_TARGET);
        //targetDim.include(Selection.Operator.MEMBER, IdentifierNode.ofNames("URI").getSegmentList());
        List<Member> members = cube.getHierarchies().get("Target.Target Hierarchy").getLevels().get("URI").getMembers();
        for (Member member : members) {
            targetDim.include(member);
        }

        query.getAxis(Axis.ROWS).addDimension(targetDim);

        query.validate();
        CellSet result = query.execute();

        assertThat(new String[][]{
                {"", "", "Avg Response Time"},
                {"http", "http://example.com/bar", "1,052"},
                {"", "http://example.com/baz", "1,105"},
                {"", "http://example.com/foo", "1,005"},
        }, matchesTable(
                olapToTable(result)
        ));
    }

    @Test
    @Ignore
    public void timeBasedQuery() throws Exception {
        OlapConnection connection = olapServer.getOlapConnection();
        Cube cube = connection.getOlapSchema().getCubes().get(CUBE_NAME);

        Query query = new Query("q", cube);

        QueryDimension measuresDim = query.getDimension(DIMENSION_MEASURE);
        QueryAxis columnAxis = query.getAxis(Axis.COLUMNS);
        columnAxis.addDimension(measuresDim);

        Hierarchy timeHierarchy = cube.getHierarchies().get("Time.Time Hierarchy");
        Member member = timeHierarchy.getLevels().get("Day").getMembers().get(0);
        QueryDimension targetDim = query.getDimension(DIMENSION_TARGET);
        targetDim.include(member);

        query.getAxis(Axis.ROWS).addDimension(targetDim);

        execute(query);
    }


    @After
    public void tearDown() {
        olapServer.disconnect();
    }

}
