package com.rkddl.examples.springexample;


import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@IfProfileValue(name = "spring.profiles.active", value = "integration")
@MockBean(classes = {})
public class DatabaseStructureTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    public void verifyTableNutzer() {
        Object expectedMetaData[][] = {
                {"angelegt_zeitpunkt", "timestamp with time zone", null, "NO"},
                {"email", "character varying", "''::character varying", "YES"},
                {"geburtsdatum", "date", null, "YES"},
                {"hausnummer", "character varying", null, "YES"},
                {"id", "bigint", null, "NO"},
                {"mail_gesendet", "boolean", "false", "YES"},
                {"nachname", "character varying", null, "YES"},
                {"ort", "character varying", null, "YES"},
                {"plz", "character varying", null, "YES"},
                {"strasse", "character varying", null, "YES"},
                {"telefon", "character varying", "''::character varying", "YES"},
                {"telefon_verifiziert", "boolean", null, "YES"},
                {"vorname", "character varying", null, "YES"}
        };
        verifyTable("nutzer", expectedMetaData);
    }

    @Test
    public void executeNutzerSelectStatement() {
        executeSelectQuery("select id, angelegt_zeitpunkt from nutzer", new Object[]{"id", "angelegt_zeitpunkt"});
    }

    /**
     * Executes a native SQL select query and nicely prints out the result.
     *
     * @param selectQuery       the query to execute
     * @param resultColumnNames the header row (the number of column must match the ones of the select result)
     */
    private void executeSelectQuery(String selectQuery, Object[] resultColumnNames) {
        Query query = entityManager.createNativeQuery(selectQuery);
        List<Object[]> results = query.getResultList();

        int columnWitdths[] = new int[resultColumnNames.length];
        extractColumnWidths(resultColumnNames, columnWitdths);
        for (Object[] row : results) {
            extractColumnWidths(row, columnWitdths);
        }

        printRow(resultColumnNames, columnWitdths);
        for (Object[] row : results) {
            printRow(row, columnWitdths);
        }
    }

    private static void extractColumnWidths(Object[] headerNames, int[] columnWitdths) {
        for (int i = 0; i < headerNames.length; i++) {
            columnWitdths[i] = Math.max(columnWitdths[i], String.valueOf(headerNames[i]).length());
        }
    }

    private static void printRow(Object[] row, int[] columnWitdths) {
        System.out.print("|");
        for (int i = 0; i < row.length; i++) {
            String columnValue = String.valueOf(row[i]);
            System.out.print(StringUtils.repeat(' ', columnWitdths[i] - columnValue.length() + 1) + "'" + columnValue + "' |");
        }
        System.out.println();
    }

    /**
     * Verify the meta data of a database table.
     * <p>
     * Extract the expected meta data from the database by executing "SELECT column_name, data_type, column_default, is_nullable FROM
     * information_schema.columns WHERE table_name='<table name>' ORDER BY column_name". Save it as CSV and perform following REGEX
     * replacements:
     * 1. ^(.+)$ => {(\1\},
     * 2. NULL => null
     *
     * @param tableName        the name of the table to verify, e.g. "nutzer"
     * @param expectedMetaData the expected meta data
     */
    private void verifyTable(String tableName, Object[][] expectedMetaData) {
        Query query = entityManager.createNativeQuery("SELECT column_name, data_type, column_default, is_nullable FROM information_schema"
                + ".columns WHERE table_name=? ORDER BY column_name");
        query.setParameter(1, tableName);
        List<Object[]> columns = query.getResultList();

        String receivedDeepToString = deepToString(columns);
        String queryResultColumnNames[] = {"column_name", "data_type", "column_default", "is_nullable"};

        for (int i = 0; i < expectedMetaData.length; i++) {
            for (int j = 0; j < expectedMetaData[i].length; j++) {
                 assertEquals("Value '"
                        + queryResultColumnNames[j]
                        + "' of column '"
                        + expectedMetaData[i][0]
                        + "' of table '"
                        + tableName
                        + "' is not as expected. See also complete meta data: "
                        + receivedDeepToString, expectedMetaData[i][j], columns.get(i)[j]);
            }
        }
    }

    private static String deepToString(List<Object[]> columns) {
        StringBuffer sb = new StringBuffer();
        for (Object[] column : columns) {
            sb.append(Arrays.deepToString(column));
        }
        return sb.toString();
    }
}
