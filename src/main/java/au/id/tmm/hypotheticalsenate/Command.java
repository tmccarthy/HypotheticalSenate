package au.id.tmm.hypotheticalsenate;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author timothy
 */
public enum Command {
    COMMANDS("commands", "Displays a list of commands"),
    DELETE("delete", "Deletes the database"),
    CREATE_SCHEMA("createSchema", "Creates the schema for the database"),
    LOAD_STATES("loadStates", "Loads the states and territories into the database"),
    LOAD_CANDIDATES_AND_PARTIES("loadPartiesAndCandidates", "Loads the parties and countries into the database"),
    LOAD_GROUP_VOTING_TICKETS("loadGroupVotingTickets", "Loads the group voting tickets into the database"),
    LOAD_ATL_VOTES("loadATLVotes", "Loads the above-the-line tallies into the database"),
    LOAD_EASY("loadEasy", "Performs the easy loading commands",
            LOAD_STATES,
            LOAD_CANDIDATES_AND_PARTIES,
            LOAD_GROUP_VOTING_TICKETS,
            LOAD_ATL_VOTES),
    LOAD_BTL_VOTES_ACT("loadBTLVotesACT", "Loads the below-the-line votes for the ACT"),
    LOAD_BTL_VOTES_NSW("loadBTLVotesNSW", "Loads the below-the-line votes for NSW"),
    LOAD_BTL_VOTES_NT("loadBTLVotesNT", "Loads the below-the-line votes for the NT"),
    LOAD_BTL_VOTES_QLD("loadBTLVotesQLD", "Loads the below-the-line votes for QLD"),
    LOAD_BTL_VOTES_SA("loadBTLVotesSA", "Loads the below-the-line votes for SA"),
    LOAD_BTL_VOTES_TAS("loadBTLVotesTAS", "Loads the below-the-line votes for TAS"),
    LOAD_BTL_VOTES_VIC("loadBTLVotesVIC", "Loads the below-the-line votes for VIC"),
    LOAD_BTL_VOTES_WA("loadBTLVotesWA", "Loads the below-the-line votes for WA"),
    LOAD_BTL_VOTES_ALL("loadBTLVotesAll", "Loads the below-the-line votes for all states and territories",
            LOAD_BTL_VOTES_ACT,
            LOAD_BTL_VOTES_NSW,
            LOAD_BTL_VOTES_NT,
            LOAD_BTL_VOTES_QLD,
            LOAD_BTL_VOTES_SA,
            LOAD_BTL_VOTES_TAS,
            LOAD_BTL_VOTES_VIC,
            LOAD_BTL_VOTES_WA),
    COUNT_ACT("countACT", "Counts all the votes for the ACT"),
    COUNT_NSW("countNSW", "Counts all the votes for NSW"),
    COUNT_NT("countNT", "Counts all the votes for the NT"),
    COUNT_QLD("countQLD", "Counts all the votes for QLD"),
    COUNT_SA("countSA", "Counts all the votes for SA"),
    COUNT_TAS("countTAS", "Counts all the votes for TAS"),
    COUNT_VIC("countVIC", "Counts all the votes for VIC"),
    COUNT_WA("countWA", "Counts all the votes for WA"),
    COUNT_ALL("countAll", "Counts the votes for all states and territories",
            COUNT_ACT,
            COUNT_NSW,
            COUNT_NT,
            COUNT_QLD,
            COUNT_SA,
            COUNT_TAS,
            COUNT_VIC,
            COUNT_WA);

    private String name;
    private String description;
    private Optional<List<Command>> constituentCommands;

    private Command(String name, String description, Command... constituentCommands) {
        this.name = name;
        this.description = description;
        this.constituentCommands = Optional.of(Arrays.asList(constituentCommands)).filter(list -> !list.isEmpty());
    }

    private Command(String name, String description) {
        this(name, description, new Command[0]);
    }

    public Optional<List<Command>> getConstituentCommands() {
        return constituentCommands;
    }

    public String getName() {
        return this.name;
    }

    public String getSummary() {
        return this.name + ": " + this.description
                + this.constituentCommands
                .map(commands ->
                        ". Made up of "
                                + StringUtils.join(
                                commands
                                        .stream()
                                        .map(Command::getName)
                                        .collect(Collectors.toList()), ", "))
                .orElse("");
    }

    public static String allSummaries() {
        return "\t" + StringUtils.join(EnumSet.allOf(Command.class)
                        .stream()
                        .map(Command::getSummary)
                        .collect(Collectors.toList()),
                System.lineSeparator() + "\t");
    }

    public static Command from(String name) {
        return EnumSet.allOf(Command.class)
                .stream()
                .filter(command -> command.getName().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unrecognised command \"" + name + "\""));
    }
}
