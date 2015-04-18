package au.id.tmm.hypotheticalsenate.database;

import au.com.bytecode.opencsv.CSVReader;
import au.id.tmm.hypotheticalsenate.GUIMain;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author timothy
 */
public class HypotheticalSenateDatabase {

    private static final String SQLITE_DRIVER_NAME = "org.sqlite.JDBC";
    private static final String CREATE_TABLES_SCRIPT_LOCATION = "/setupDatabase.sql";

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

    public void clear() {
        GUIMain.out.println("Clearing database...");
        if (this.dbFile.exists()) {
            this.runWithConnection((connection, resources) -> {
                Statement statement = connection.createStatement();

                resources.add(statement);

                statement.executeUpdate("PRAGMA writable_schema = 1;");
                statement.executeUpdate("DELETE FROM sqlite_master WHERE type IN ('table', 'index', 'trigger')");
                statement.executeUpdate("PRAGMA writable_schema = 0;");
                statement.executeUpdate("VACUUM;");
            });
        }
    }

    public void setup() {
        this.createTables();
        this.loadStates();
        this.loadElections();
    }

    public void createTables() {
        GUIMain.out.println("Creating database tables...");
        this.runWithConnection((connection, resources) -> {
            Statement statement = connection.createStatement();

            resources.add(statement);

            statement.executeUpdate(IOUtils.toString(
                    HypotheticalSenateDatabase.class.getResourceAsStream(CREATE_TABLES_SCRIPT_LOCATION)));
        });
    }

    public void loadStates() {
        GUIMain.out.println("Loading states into database...");
        String insertStatement = "INSERT INTO State (stateCode, stateName) VALUES (?, ?);";

        this.runWithConnection((connection, resources) -> {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
            resources.add(preparedStatement);

            for (AustralianState state : AustralianState.values()) {
                preparedStatement.setString(1, state.getCode());
                preparedStatement.setString(2, state.getName());
                preparedStatement.execute();
            }

            connection.commit();
        });
    }

    public void loadElections() {
        GUIMain.out.println("Loading elections into database...");
        String insertStatement = "INSERT INTO Election (electionID, date, name) VALUES (?, ?, ?);";

        this.runWithConnection((connection, resources) -> {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
            resources.add(preparedStatement);

            for (Election election : Election.values()) {
                preparedStatement.setInt(1, election.getID());
                preparedStatement.setDate(2, new Date(election.getDate().getTime()));
                preparedStatement.setString(3, election.getDescription());
                preparedStatement.execute();
            }

            connection.commit();
        });
    }

    public void loadPartiesAndCandidates(File downloadDirectory, Election election) {
        GUIMain.out.println("Loading parties and candidates into database...");

        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(2);

        map.put("INSERT OR IGNORE INTO Party (electionID, partyID, partyName) VALUES (?, ?, ?)",
                row -> Arrays.asList(
                        election.getID(),
                        row[1],
                        row[2]
                ));
        map.put("INSERT INTO Candidate (electionID, candidateID, partyID, givenName, surname) VALUES (?, ?, ?, ?, ?)",
                row -> Arrays.asList(
                        election.getID(),
                        Integer.valueOf(row[3]),
                        row[1],
                        row[5],
                        row[4]
                ));

        this.loadFromDataSource(new DataSource(AECResource.candidates(election), downloadDirectory), map);
    }

    public void loadGroupVotingTickets(File downloadDirectory, Election election) {
        GUIMain.out.println("Loading group voting tickets into database...");

        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(2);

        map.put("INSERT OR IGNORE INTO GroupTicketInfo (electionID, stateCode, groupID, ownerParty) " +
                        "VALUES (?, ?, ?, ?)",
                row -> {
                    if (Integer.valueOf(row[12]) == 1) {
                        // The first preference of the ticket is the ticket owner.
                        return Arrays.asList(
                                election.getID(),
                                row[0],
                                row[3],
                                row[10]
                        );
                    } else {
                        return null;
                    }
                });
        map.put("INSERT INTO GroupTicketPreference " +
                        "(electionID, stateCode, ownerGroup, ticket, preference, preferencedCandidate) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                row -> Arrays.asList(
                        election.getID(),
                        row[0],
                        row[3],
                        Integer.valueOf(row[4]),
                        Integer.valueOf(row[12]),
                        Integer.valueOf(row[5])
                ));

        this.loadFromDataSource(new DataSource(AECResource.groupVotingTickets(election), downloadDirectory), map);
    }

    public void loadAboveTheLineVotes(File downloadDirectory, Election election) {
        GUIMain.out.println("Loading above the line votes into database...");

        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(1);

        map.put("INSERT INTO AboveTheLineVotes (electionID, stateCode, groupID, votes) VALUES (?, ?, ?, ?)",
                row -> Arrays.asList(
                        election.getID(),
                        row[0],
                        row[1],
                        row[4]
                ));

        this.loadFromDataSource(new DataSource(AECResource.groupFirstPreferences(election), downloadDirectory), map);
    }

    public void loadBelowTheLinePreferences(File downloadDirectory, Collection<? extends AustralianState> states, Election election) {
        states.forEach(state -> loadBelowTheLinePreferences(downloadDirectory, state, election));
    }

    public void loadBelowTheLinePreferences(File downloadDirectory, AustralianState state, Election election) {
        GUIMain.out.println("Loading below the line preferences for " + state.render() + " into database...");
        Map<String, Function<String[], List<Object>>> map = new LinkedHashMap<>(1);

        map.put("INSERT INTO BelowTheLineBallot " +
                        "(electionID, stateCode, ballotID, batch, paper, preference, candidateID) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                row -> {
                    Integer batch = Integer.valueOf(row[2]);
                    Integer paper = Integer.valueOf(row[3]);
                    return Arrays.asList(
                            election.getID(),
                            state.getCode(),
                            pairingFunction(batch, paper),
                            batch,
                            paper,
                            row[1],
                            row[0]
                    );
                });

        this.loadFromDataSource(new DataSource(AECResource.btlPreferences(election, state), downloadDirectory), map);
    }

    private int pairingFunction(int a, int b) {
        return (a + b) * (a + b + 1) / 2 + a;
    }

    /**
     * Loads data from the given {@link DataSource} into the database, using the given map of SQL insert statements
     * and associated functions. Each {@link Function} extracts values from a row of the csv data source. These values
     * are then inserted into the parametrised SQL insert string.
     * <p>
     * Note that the {@link Function}s in the map may return null, which means that we should not execute an inser this
     * time.
     */
    private void loadFromDataSource(DataSource dataSource,
                                    Map<String, Function<String[], List<Object>>> sqlInsertsAndValueExtractors) {
        if (!dataSource.isDownloaded()) {
            dataSource.download();
        }

        this.runWithConnection((connection, resources) -> {
            connection.setAutoCommit(false);

            Map<PreparedStatement, Function<String[], List<Object>>> statementValueExtractorMap = new LinkedHashMap<>();

            for (String sql : sqlInsertsAndValueExtractors.keySet()) {
                statementValueExtractorMap.put(connection.prepareStatement(sql),
                        sqlInsertsAndValueExtractors.get(sql));
            }

            CSVReader csvReader = dataSource.getCSVReader();
            resources.add(csvReader);
            csvReader.readNext(); // Read info line
            csvReader.readNext(); // Read column header line

            String[] nextLine;

            while ((nextLine = csvReader.readNext()) != null) {
                for (Map.Entry<PreparedStatement, Function<String[], List<Object>>> currentEntry
                        : statementValueExtractorMap.entrySet()) {

                    PreparedStatement preparedStatement = currentEntry.getKey();
                    resources.add(preparedStatement);
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
        });
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(databaseUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create connection to database", e);
        }
    }

    /**
     * Provides the given {@link ConnectionConsumer} with a connection, and handles the closing of any of its resources.
     */
    @SuppressWarnings("ThrowFromFinallyBlock")
    public void runWithConnection(ConnectionConsumer connectionConsumer) {
        Connection connection = null;
        Collection<AutoCloseable> resources = new HashSet<>();
        try {
            connection = this.getConnection();

            connectionConsumer.useConnection(connection, resources);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("An error occurred while accessing the database", e);
        } finally {
            if (!resources.isEmpty()) {
                for (AutoCloseable closeable : resources) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
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

    /**
     * Functional interface passed into {@link #runWithConnection(ConnectionConsumer)}. The connection is provided,
     * as long as a collection to which any {@link AutoCloseable} resources can be added. The closing of these
     * resources will be handled by the {@link #runWithConnection(ConnectionConsumer)} method.
     */
    @FunctionalInterface
    public static interface ConnectionConsumer {
        public void useConnection(Connection connection, Collection<AutoCloseable> resources)
                throws SQLException, IOException;
    }
}
