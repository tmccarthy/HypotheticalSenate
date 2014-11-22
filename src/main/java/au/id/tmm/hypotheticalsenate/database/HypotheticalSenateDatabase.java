package au.id.tmm.hypotheticalsenate.database;

import au.com.bytecode.opencsv.CSVReader;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import static au.id.tmm.hypotheticalsenate.database.DataSource.GROUP_VOTING_TICKETS;
import static au.id.tmm.hypotheticalsenate.database.DataSource.SENATE_CANDIDATES;

/**
 * @author timothy
 */
public class HypotheticalSenateDatabase {

    private static final String SQLITE_DRIVER_NAME = "org.sqlite.JDBC";
    private static final String CREATE_TABLES_SCRIPT_LOCATION = "/setupDatabase.sql";

    private String databaseUrl;

    public HypotheticalSenateDatabase(String location) {
        this.databaseUrl = "jdbc:sqlite:" + location;

        try {
            Class.forName(SQLITE_DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find the SQLite driver on the classpath", e);
        }
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

    public void loadPartiesAndCandidates() {
        if (!SENATE_CANDIDATES.isDownloaded()) {
            SENATE_CANDIDATES.download();
        }

        String insertCandidate =
                "INSERT INTO Candidates (candidateID, partyID, givenName, surname) VALUES (?, ?, ?, ?)";

        String insertParty =
                "INSERT OR IGNORE INTO Party (partyID, partyName) VALUES (?, ?)";

        this.runWithConnection(connection -> {

            connection.setAutoCommit(false);

            try (CSVReader csvReader = SENATE_CANDIDATES.getCSVReader()) {
                csvReader.readNext(); // Read info line
                csvReader.readNext(); // Read column header line

                String[] nextLine;

                while ((nextLine = csvReader.readNext()) != null) {
                    PreparedStatement partyStatement = connection.prepareStatement(insertParty);

                    partyStatement.setString(1, nextLine[1]);
                    partyStatement.setString(2, nextLine[2]);
                    partyStatement.execute();

                    PreparedStatement candidateStatement = connection.prepareStatement(insertCandidate);

                    candidateStatement.setInt(1, Integer.valueOf(nextLine[3]));
                    candidateStatement.setString(2, nextLine[1]);
                    candidateStatement.setString(3, nextLine[5]);
                    candidateStatement.setString(4, nextLine[4]);
                    candidateStatement.execute();
                }

                connection.commit();
            } catch (IOException e) {
                throw new RuntimeException("An exception occurred while reading the downloaded input file", e);
            }
        });
    }

    public void loadGroupVotingTickets() {
        if (!GROUP_VOTING_TICKETS.isDownloaded()) {
            GROUP_VOTING_TICKETS.download();
        }

        String infoInsert =
                "INSERT OR IGNORE INTO GroupTicketInfo (stateCode, groupID, ownerParty) VALUES (?, ?, ?)";
        String preferenceInsert =
                "INSERT INTO GroupTicketPreference (stateCode, ownerGroup, preference, preferencedGroup) " +
                        "VALUES (?, ?, ?, ?)";



    }

    public void loadAboveTheLineVotes() {

    }

    public void loadBelowTheLinePreferences(Collection<? extends AustralianState> states) {

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
