package au.id.tmm.hypotheticalsenate.database;

import au.com.bytecode.opencsv.CSVReader;
import au.id.tmm.hypotheticalsenate.Main;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static au.id.tmm.hypotheticalsenate.database.AECResource.*;

/**
 * @author timothy
 */
public class HypotheticalSenateDatabase {

    private static final String SQLITE_DRIVER_NAME = "org.sqlite.JDBC";
    private static final String CREATE_TABLES_SCRIPT_LOCATION = "/setupDatabase.sql";
    public static final int COMMIT_EVERY = 1000;

    private final String databaseUrl;
    private final File dbFile;

    public HypotheticalSenateDatabase(File dbFile) {
        this.dbFile = dbFile;
        this.databaseUrl = "jdbc:sqlite:" + dbFile.getPath();

        try {
            Class.forName(SQLITE_DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find the SQLite driver on the classpath", e);
        }
    }

    public void delete() {
        Main.out.println("Deleting old database...");
        if (!this.dbFile.delete()) {
            throw new RuntimeException("Unable to delete the file at " + dbFile.getPath());
        }
    }

    public void createTables() {
        Main.out.println("Creating database tables...");
        this.runWithConnection(connection -> {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(IOUtils.toString(
                        HypotheticalSenateDatabase.class.getResourceAsStream(CREATE_TABLES_SCRIPT_LOCATION)));

                return Arrays.asList(statement);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the table creation script", e);
            }
        });
    }

    public void loadStates() {
        Main.out.println("Loading states into database...");
        String insertStatement = "INSERT INTO State (stateCode, stateName) VALUES (?, ?);";

        this.runWithConnection(connection -> {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);

            for (AustralianState state : AustralianState.values()) {
                preparedStatement.setString(1, state.getCode());
                preparedStatement.setString(2, state.getName());
                preparedStatement.execute();
            }

            connection.commit();

            return Arrays.asList(preparedStatement);
        });
    }

    public void loadPartiesAndCandidates(File downloadDirectory) {
        Main.out.println("Loading parties and candidates into database...");

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

        this.loadFromDataSource(new DataSource(SENATE_CANDIDATES, downloadDirectory), map);
    }

    public void loadGroupVotingTickets(File downloadDirectory) {
        Main.out.println("Loading group voting tickets into database...");

        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(2);

        map.put("INSERT OR IGNORE INTO GroupTicketInfo (stateCode, groupID, ownerParty) VALUES (?, ?, ?)",
                row -> {
                    if (Integer.valueOf(row[12]) == 1) {
                        // The first preference of the ticket is the ticket owner.
                        return Arrays.asList(
                                row[0],
                                row[3],
                                row[10]
                        );
                    } else {
                        return null;
                    }
                });
        map.put("INSERT INTO GroupTicketPreference (stateCode, ownerGroup, ticket, preference, preferencedCandidate) " +
                        "VALUES (?, ?, ?, ?, ?)",
                row -> Arrays.asList(
                        row[0],
                        row[3],
                        Integer.valueOf(row[4]),
                        Integer.valueOf(row[12]),
                        Integer.valueOf(row[5])
                ));

        this.loadFromDataSource(new DataSource(GROUP_VOTING_TICKETS, downloadDirectory), map);
    }

    public void loadAboveTheLineVotes(File downloadDirectory) {
        Main.out.println("Loading above the line votes into database...");

        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(1);

        map.put("INSERT INTO AboveTheLineVotes (stateCode, groupID, votes) VALUES (?, ?, ?)",
                row -> Arrays.asList(
                        row[0],
                        row[1],
                        row[4]
                ));

        this.loadFromDataSource(new DataSource(GROUP_FIRST_PREFERENCES, downloadDirectory), map);
    }

    public void loadBelowTheLinePreferences(File downloadDirectory, Collection<? extends AustralianState> states) {
        states.forEach(state -> loadBelowTheLinePreferences(downloadDirectory, state));
    }

    public void loadBelowTheLinePreferences(File downloadDirectory, AustralianState state) {
        Main.out.println("Loading below the line preferences for " + state.render() + " into database...");
        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(1);

        map.put("INSERT INTO BelowTheLineBallot (stateCode, ballotID, preference, candidateID) " +
                "VALUES (?, ?, ?, ?)",
                row -> Arrays.asList(
                        state.getCode(),
                        row[2].trim() + "," + row[3],
                        row[1],
                        row[0]
                ));

        this.loadFromDataSource(new DataSource(AECResource.BTL_DATA_MAP.get(state), downloadDirectory), map);
    }

    /**
     * Note that the {@link Function}s in the map may return null, which means that we should not execute a query this
     * time.
     */
    private void loadFromDataSource(DataSource dataSource,
                                    Map<String, Function<String[], List<Object>>> sqlInsertsAndValueExtractors) {
        if (!dataSource.isDownloaded()) {
            dataSource.download();
        }

        this.runWithConnection(connection -> {
            connection.setAutoCommit(false);

            Map<PreparedStatement, Function<String[], List<Object>>> statementValueExtractorMap = new LinkedHashMap<>();

            for (String sql : sqlInsertsAndValueExtractors.keySet()) {
                statementValueExtractorMap.put(connection.prepareStatement(sql),
                        sqlInsertsAndValueExtractors.get(sql));
            }

            try (CSVReader csvReader = dataSource.getCSVReader()) {
                csvReader.readNext(); // Read info line
                csvReader.readNext(); // Read column header line

                String[] nextLine;

                while ((nextLine = csvReader.readNext()) != null) {
                    for (Map.Entry<PreparedStatement, Function<String[], List<Object>>> currentEntry
                            : statementValueExtractorMap.entrySet()) {

                        PreparedStatement preparedStatement = currentEntry.getKey();
                        Optional<List<Object>> paramValues = Optional.ofNullable(currentEntry.getValue().apply(nextLine));

                        if (paramValues.isPresent()) {
                            for (int paramIndex = 0; paramIndex < paramValues.get().size(); paramIndex++) {
                                Object value = paramValues.get().get(paramIndex);
                                if (value instanceof String) {
                                    preparedStatement.setString(paramIndex + 1, (String) value);
                                } else if (value instanceof Integer) {
                                    preparedStatement.setInt(paramIndex + 1, (Integer) value);
                                } else {
                                    throw new RuntimeException("Unrecognised data type " + value.getClass());
                                }
                            }
                        }

                        preparedStatement.execute();
                    }
                }

                connection.commit();

                return statementValueExtractorMap.keySet();
            } catch (IOException e) {
                throw new RuntimeException("An exception occurred while reading the downloaded input file", e);
            }
        });
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
        Collection<? extends Statement> preparedStatements = null;
        try {
            connection = this.getConnection();

            preparedStatements = connectionConsumer.useConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException("An error occurred while accessing the database", e);
        } finally {
            if (preparedStatements != null) {
                for (Statement preparedStatement : preparedStatements) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        // We continue
                    }
                }
            }
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
        public Collection<? extends Statement> useConnection(Connection connection) throws SQLException;
    }
}
