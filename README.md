# Hypothetical Senate

A project that provides a couple of utilities to work with
<a href="http://results.aec.gov.au/17496/Website/SenateDownloadsMenu-17496-tab.htm">data</a> provided by the
Australian Electoral Commission (AEC) about the 2013 Australian Senate election.

## Running

The utility is executed by invoking the run.sh bash script.

The -d switch is optional, and specifies the location of a SQLite database to be used. The default is to use a database
"data.db" in the current directory.

The -a switch is optional, and specifies the directory into which the AEC resources will be downloaded. The default is
to use "aecData" in the current directory.

Alternately, you can download a release from the releases page. navigate to the bin directory and execute one of the
executable scripts there, depending on your platform.

## Commands

Once the database has been specified, one or more commands can be specified that perform certain tasks. They are not
case-sensitive.

* commands: Displays a list of commands
* delete: Deletes the database
* createSchema: Creates the schema for the database
* loadStates: Loads the states and territories into the database
* loadPartiesAndCandidates: Loads the parties and countries into the database
* loadGroupVotingTickets: Loads the group voting tickets into the database
* loadATLVotes: Loads the above-the-line tallies into the database
* loadEasy: Performs the easy loading commands. This command is made up of:
  * loadStates,
  * loadPartiesAndCandidates,
  * loadGroupVotingTickets
* loadBTLVotesACT: Loads the below-the-line votes for the ACT
* loadBTLVotesNSW: Loads the below-the-line votes for NSW
* loadBTLVotesNT: Loads the below-the-line votes for the NT
* loadBTLVotesQLD: Loads the below-the-line votes for QLD
* loadBTLVotesSA: Loads the below-the-line votes for SA
* loadBTLVotesTAS: Loads the below-the-line votes for TAS
* loadBTLVotesVIC: Loads the below-the-line votes for VIC
* loadBTLVotesWA: Loads the below-the-line votes for WA
* loadBTLVotesAll: Loads the below-the-line votes for all states and territories. This command is made up of:
  * loadBTLVotesACT,
  * loadBTLVotesNSW,
  * loadBTLVotesNT,
  * loadBTLVotesQLD,
  * loadBTLVotesSA,
  * loadBTLVotesTAS,
  * loadBTLVotesVIC,
  * loadBTLVotesWA
* setupDatabase: Sets up the database. This command is made up of:
  * createSchema,
  * loadEasy,
  * loadBTLVotesAll
* countACT: Counts all the votes for the ACT
* countNSW: Counts all the votes for NSW
* countNT: Counts all the votes for the NT
* countQLD: Counts all the votes for QLD
* countSA: Counts all the votes for SA
* countTAS: Counts all the votes for TAS
* countVIC: Counts all the votes for VIC
* countWA: Counts all the votes for WA
* countAll: Counts the votes for all states and territories. This command is made up of:
  * countACT,
  * countNSW,
  * countNT,
  * countQLD,
  * countSA,
  * countTAS,
  * countVIC,
  * countWA

Note that the LOAD commands will download reasonably large files from the AEC website.

## Requirements

This application requires JDK 8 to build.

## The database

Note that upon execution of the LOAD commands (and those that it depends on), all data regarding ballots and candidates
will be stored in the specified SQLLite database. With an SQLite client, this database can be queried independent of
the rest of this project. The database schema is outlined in src/main/resources/setupDatabase.sql, and the table and
column names are pretty self-explanatory.