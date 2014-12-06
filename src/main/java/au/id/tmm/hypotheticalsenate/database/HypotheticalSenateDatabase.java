package au.id.tmm.hypotheticalsenate.database;

import au.com.bytecode.opencsv.CSVReader;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static au.id.tmm.hypotheticalsenate.database.DataSource.*;

/**
 * @author timothy
 */
public class HypotheticalSenateDatabase {

    private static final String SQLITE_DRIVER_NAME = "org.sqlite.JDBC";
    private static final String CREATE_TABLES_SCRIPT_LOCATION = "/setupDatabase.sql";

    private final String databaseUrl;
    private final String location;

    public HypotheticalSenateDatabase(String location) {
        this.location = location;
        this.databaseUrl = "jdbc:sqlite:" + location;

        try {
            Class.forName(SQLITE_DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find the SQLite driver on the classpath", e);
        }
    }

    public void delete() {
        new File(this.location).delete();
    }

    public void createTables() {
        this.runWithConnection(connection -> {
            try {
                connection.createStatement()
                        .executeUpdate(IOUtils.toString(
                                HypotheticalSenateDatabase.class.getResourceAsStream(CREATE_TABLES_SCRIPT_LOCATION)));
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the table creation script", e);
            }
        });
    }

    public void loadStates() {
        String insertStatement = "INSERT INTO State (stateCode, stateName) VALUES (?, ?);";

        this.runWithConnection(connection -> {
            connection.setAutoCommit(false);

            for (AustralianState state : AustralianState.values()) {
                PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
                preparedStatement.setString(1, state.getCode());
                preparedStatement.setString(2, state.getName());
                preparedStatement.execute();
            }

            connection.commit();
        });
    }

    private void loadFromDataSource(DataSource dataSource, Map<String, Function<String[], List<Object>>> sqlInsertsAndValueExtractors) {
        if (!dataSource.isDownloaded()) {
            dataSource.download();
        }

        this.runWithConnection(connection -> {
            connection.setAutoCommit(false);

            try (CSVReader csvReader = dataSource.getCSVReader()) {
                csvReader.readNext(); // Read info line
                csvReader.readNext(); // Read column header line

                String[] nextLine;

                while ((nextLine = csvReader.readNext()) != null) {
                    for (Map.Entry<String, Function<String[], List<Object>>> currentEntry : sqlInsertsAndValueExtractors.entrySet()) {
                        PreparedStatement preparedStatement = connection.prepareStatement(currentEntry.getKey());

                        List<Object> paramValues = currentEntry.getValue().apply(nextLine);

                        for (int paramIndex = 0; paramIndex < paramValues.size(); paramIndex++) {
                            Object value = paramValues.get(paramIndex);
                            if (value instanceof String) {
                                preparedStatement.setString(paramIndex + 1, (String) value);
                            } else if (value instanceof Integer) {
                                preparedStatement.setInt(paramIndex + 1, (Integer) value);
                            } else {
                                throw new RuntimeException("Unrecognised data type " + value.getClass());
                            }
                        }

                        preparedStatement.execute();
                    }
                }

                connection.commit();
            } catch (IOException e) {
                throw new RuntimeException("An exception occurred while reading the downloaded input file", e);
            }

        });
    }

    public void loadPartiesAndCandidates() {
        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(2);

        map.put("INSERT OR IGNORE INTO Party (partyID, partyName) VALUES (?, ?)",
                row -> Arrays.asList(
                        row[1],
                        row[2]
                ));
        map.put("INSERT INTO Candidate (candidateID, partyID, givenName, surname) VALUES (?, ?, ?, ?)",
                row -> Arrays.asList(
                        Integer.valueOf(row[3]),
                        row[1],
                        row[5],
                        row[4]
                ));

        this.loadFromDataSource(SENATE_CANDIDATES, map);
    }

    public void loadGroupVotingTickets() {
        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(2);

        map.put("INSERT OR IGNORE INTO GroupTicketInfo (stateCode, groupID, ownerParty) VALUES (?, ?, ?)",
                row -> Arrays.asList(
                        row[0],
                        row[3],
                        row[10]
                ));
        map.put("INSERT INTO GroupTicketPreference (stateCode, ownerGroup, ticket, preference, preferencedGroup) " +
                        "VALUES (?, ?, ?, ?, ?)",
                row -> Arrays.asList(
                        row[0],
                        row[3],
                        Integer.valueOf(row[4]),
                        Integer.valueOf(row[12]),
                        row[5]
                ));

        this.loadFromDataSource(GROUP_VOTING_TICKETS, map);
    }

    public void loadAboveTheLineVotes() {
        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(1);

        map.put("INSERT INTO AboveTheLineVotes (stateCode, groupID, votes) VALUES (?, ?, ?)",
                row -> Arrays.asList(
                        row[0],
                        row[1],
                        row[3]
                ));

        this.loadFromDataSource(GROUP_FIRST_PREFERENCES, map);
    }

    public void loadBelowTheLinePreferences(Collection<? extends AustralianState> states) {
        states.forEach(this::loadBelowTheLinePreferences);
    }

    public void loadBelowTheLinePreferences(AustralianState state) {
        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(1);

        map.put("INSERT INTO BelowTheLineBallot (stateCode, ballotBatch, ballotPaper, preference, candidateID) " +
                "VALUES (?, ?, ?, ?, ?)",
                row -> Arrays.asList(
                        state.getCode(),
                        row[2],
                        row[3],
                        row[1],
                        row[0]
                ));

        this.loadFromDataSource(DataSource.BTL_DATA_MAP.get(state), map);
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(databaseUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create connection to database", e);
        }
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public void runWithConnection(ConnectionConsumer connectionConsumer) {
        Connection connection = null;

        try {
            connection = this.getConnection();

            connectionConsumer.useConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException("An error occurred while accessing the database", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Unable to close the database connection", e);
                }
            }
        }
    }

    @FunctionalInterface
    public static interface ConnectionConsumer {
        public void useConnection(Connection connection) throws SQLException;
    }

}
