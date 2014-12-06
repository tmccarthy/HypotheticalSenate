package au.id.tmm.hypotheticalsenate;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author timothy
 */
public class Main {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintStream out = System.out;
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintStream err = System.err;

    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = extractCommandLineArgs(args);

        File databaseLocation = commandLineArgs.dbFile;
        File downloadDirectory = commandLineArgs.aecDataDownloadDirectory;
        List<Command> commandLineCommands = commandLineArgs.arguments
                .stream()
                .map(Command::from)
                .collect(Collectors.toList());

        new AppInstance(databaseLocation, downloadDirectory, commandLineCommands).run();
    }

    private static CommandLineArgs extractCommandLineArgs(String[] args) {
        CmdLineParser parser = null;
        CommandLineArgs commandLineArgs;

        try {
            commandLineArgs = new CommandLineArgs();
            parser = new CmdLineParser(commandLineArgs);
            parser.parseArgument(args);

            return commandLineArgs;
        } catch (CmdLineException e) {
            err.println(e.getMessage());
            parser.printUsage(err);
            System.exit(-1);
        }

        return null;
    }

    private static class CommandLineArgs {
        @Option(name = "-d",
                aliases = {"--database", "--db"},
                usage = "The location of the senate results database")
        private File dbFile;

        @Option(name = "-a",
                aliases = "--aecDownloads",
                usage = "The directory into which the raw data files from the AEC will be downloaded")
        private File aecDataDownloadDirectory = new File("aecData");

        @Argument(multiValued = true,
                usage = "Commands for dealing with the database. Commands will be executed in order, " +
                "and are case insensitive. Use the command \"commands\" to see a list of commands.")
        private List<String> arguments;
    }
}
