package au.id.tmm.hypotheticalsenate.controller.commands;

import au.id.tmm.hypotheticalsenate.controller.Count;
import au.id.tmm.hypotheticalsenate.database.HypotheticalSenateDatabase;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;

import javax.annotation.Nullable;
import java.io.File;

/**
 * @author timothy
 */
public enum HypotheticalSenateTask {

    CLEAR("Clear",
            "Clears the database",
            (downloadDirectory, database, election, state) -> database.clear(),
            false,
            false),
    SETUP("Setup",
            "Sets up the database with its schema, elections and states",
            (downloadDirectory, database, election, state) -> database.setup(),
            false,
            false),
    LOAD_CANDIDATES("Load parties and candidates",
            "Loads the parties and candidates for an election into the database",
            (downloadDirectory, database, election, state) -> database.loadPartiesAndCandidates(downloadDirectory, election),
            true,
            false),
    LOAD_GROUP_VOTING_TICKETS("Load group voting tickets",
            "Loads the information about the group voting tickets for an election into the database",
            (downloadDirectory, database, election, state) -> database.loadGroupVotingTickets(downloadDirectory, election),
            true,
            false),
    LOAD_ATL_VOTES("Load above the line votes",
            "Loads the above the line votes for an election into the database",
            (downloadDirectory, database, election, state) -> database.loadAboveTheLineVotes(downloadDirectory, election),
            true,
            false),
    LOAD_BTL_VOTES("Load below the line votes",
            "Loads the below the line votes for a given state and election into the database",
            (downloadDirectory, database, election, state) -> database.loadBelowTheLinePreferences(downloadDirectory, state, election),
            true,
            true),
    COUNT("Count",
            "Performs a count of the votes in a given state and election",
            (downloadDirectory, database, election, state) -> new Count(database, election, state).perform(),
            true,
            true),
    ALTERNATIVE_COUNT("Alternative Count",
            "Performs a count of the votes in a given state and election with different rules",
            (downloadDirectory, database, election, state) -> new Count(database, election, state, BTLPreferenceBallotCollector::new).perform(),
            true,
            true)
    ;

    private final String name;
    private final String description;
    private final CommandExecutor commandExecutor;
    private final boolean electionRequired;
    private final boolean stateRequired;

    private HypotheticalSenateTask(String name, String description, CommandExecutor commandExecutor,
                                   boolean electionRequired, boolean stateRequired) {
        this.name = name;
        this.description = description;
        this.commandExecutor = commandExecutor;
        this.electionRequired = electionRequired;
        this.stateRequired = stateRequired;
    }

    public void execute(File downloadDirectory,
                        HypotheticalSenateDatabase database,
                        Election election,
                        AustralianState state) {
        this.commandExecutor.execute(downloadDirectory, database, election, state);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public static void combineAndExecute(File downloadDirectory,
                                         HypotheticalSenateDatabase database,
                                         @Nullable Election election,
                                         @Nullable AustralianState state,
                                         HypotheticalSenateTask... hypotheticalSenateTasks) {
        for(HypotheticalSenateTask hypotheticalSenateTask : hypotheticalSenateTasks) {
            hypotheticalSenateTask.execute(downloadDirectory, database, election, state);
        }
    }

    public boolean isElectionRequired() {
        return electionRequired;
    }

    public boolean isStateRequired() {
        return stateRequired;
    }

    @FunctionalInterface
    private static interface CommandExecutor {
        public void execute(File downloadDirectory, HypotheticalSenateDatabase database, Election election,
                            AustralianState state);
    }
}
