package au.id.tmm.hypotheticalsenate;

import au.id.tmm.hypotheticalsenate.database.HypotheticalSenateDatabase;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @author timothy
 */
public class AppInstance implements Runnable {
    private ImmutableMap<Command, AustralianState> BTL_COMMAND_MAP = ImmutableMap.
            <Command, AustralianState>builder()
            .put(Command.LOAD_BTL_VOTES_ACT, AustralianState.ACT)
            .put(Command.LOAD_BTL_VOTES_NSW, AustralianState.NSW)
            .put(Command.LOAD_BTL_VOTES_NT, AustralianState.NT)
            .put(Command.LOAD_BTL_VOTES_QLD, AustralianState.QLD)
            .put(Command.LOAD_BTL_VOTES_SA, AustralianState.SA)
            .put(Command.LOAD_BTL_VOTES_TAS, AustralianState.TAS)
            .put(Command.LOAD_BTL_VOTES_VIC, AustralianState.VIC)
            .put(Command.LOAD_BTL_VOTES_WA, AustralianState.WA)
            .build();

    private final File databaseLocation;
    private final File downloadDirectory;
    private final List<Command> commands;

    private HypotheticalSenateDatabase database;

    public AppInstance(File databaseLocation, File downloadDirectory, List<Command> commands) {
        this.databaseLocation = databaseLocation;
        this.downloadDirectory = downloadDirectory;
        this.commands = commands;
    }

    @Override
    public void run() {
        // First check if "commands" was in the commands list, if so just run it and return.
        if (this.commands.contains(Command.COMMANDS)) {
            this.runCommand(Command.COMMANDS);
        }

        // Next we perform necessary checks
        this.check();

        this.database = new HypotheticalSenateDatabase(this.databaseLocation);

        // Finally we run the commands
        this.commands.forEach(this::runCommand);

    }

    private void check() {
        Objects.requireNonNull(this.databaseLocation, "The database location cannot be null");
        Objects.requireNonNull(this.downloadDirectory, "The download directory cannot be null");

        if (!this.downloadDirectory.exists()) {
            this.downloadDirectory.mkdirs();
        } else if (this.downloadDirectory.exists() && !this.downloadDirectory.isDirectory()) {
            throw new RuntimeException("The specified download directory \"" + downloadDirectory.getPath()
                    + "\" exists but is not a directory");
        }
    }

    private void runCommand(Command command) {
        if (command.getConstituentCommands().isPresent()) {
            command.getConstituentCommands().get()
                    .forEach(this::runCommand);
        } else if (BTL_COMMAND_MAP.containsKey(command)) {
            this.database.loadBelowTheLinePreferences(this.downloadDirectory, BTL_COMMAND_MAP.get(command));
        } else {
            switch (command) {
                case COMMANDS:
                    Main.out.println(Command.allSummaries());
                    break;
                case DELETE:
                    this.database.delete();
                    break;
                case CREATE_SCHEMA:
                    this.database.createTables();
                    break;
                case LOAD_STATES:
                    this.database.loadStates();
                    break;
                case LOAD_CANDIDATES_AND_PARTIES:
                    this.database.loadPartiesAndCandidates(this.downloadDirectory);
                    break;
                case LOAD_GROUP_VOTING_TICKETS:
                    this.database.loadGroupVotingTickets(this.downloadDirectory);
                    break;
                case LOAD_ATL_VOTES:
                    this.database.loadAboveTheLineVotes(this.downloadDirectory);
                    break;
            }
        }
    }
}
