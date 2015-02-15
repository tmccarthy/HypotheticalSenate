# Hypothetical Senate

A project that provides a couple of utilities to work with
<a href="http://results.aec.gov.au/17496/Website/SenateDownloadsMenu-17496-tab.htm">data</a> provided by the
Australian Electoral Commission (AEC) about the 2013 Australian Senate election.

## Running

The utility is executed by invoking the run.sh bash script.

The -d switch is required, and specifies the location of a SQLite database to be used.

The -a switch is optional, and specifies the directory into which the AEC resources will be downloaded.

## Commands

Once the database has been specified, one or more commands can be specified that perform certain tasks. They are not
case-sensitive.

* COMMANDS: Displays a list of commands
* DELETE: Deletes the database
* CREATE_SCHEMA: Creates the schema for the database
* LOAD_STATES: Loads the states and territories into the database
* LOAD_CANDIDATES_AND_PARTIES: Loads the parties and countries into the database
* LOAD_GROUP_VOTING_TICKETS: Loads the group voting tickets into the database
* LOAD_ATL_VOTES: Loads the above-the-line tallies into the database
* LOAD_EASY: Performs the easy loading commands. This command is equivalent to executing:
  * LOAD_STATES,
  * LOAD_CANDIDATES_AND_PARTIES,
  * LOAD_GROUP_VOTING_TICKETS,
  * LOAD_ATL_VOTES),
* LOAD_BTL_VOTES_ACT: Loads the below-the-line votes for the ACT
* LOAD_BTL_VOTES_NSW: Loads the below-the-line votes for NSW
* LOAD_BTL_VOTES_NT: Loads the below-the-line votes for the NT
* LOAD_BTL_VOTES_QLD: Loads the below-the-line votes for QLD
* LOAD_BTL_VOTES_SA: Loads the below-the-line votes for SA
* LOAD_BTL_VOTES_TAS: Loads the below-the-line votes for TAS
* LOAD_BTL_VOTES_VIC: Loads the below-the-line votes for VIC
* LOAD_BTL_VOTES_WA: Loads the below-the-line votes for WA
* LOAD_BTL_VOTES_ALL: Loads the below-the-line votes for all states and territories. This command is equivalent to executing:
  * LOAD_BTL_VOTES_ACT
  * LOAD_BTL_VOTES_NSW
  * LOAD_BTL_VOTES_NT
  * LOAD_BTL_VOTES_QLD
  * LOAD_BTL_VOTES_SA
  * LOAD_BTL_VOTES_TAS
  * LOAD_BTL_VOTES_VIC
  * LOAD_BTL_VOTES_WA
* COUNT_ACT: Counts all the votes for the ACT
* COUNT_NSW: Counts all the votes for NSW
* COUNT_NT: Counts all the votes for the NT
* COUNT_QLD: Counts all the votes for QLD
* COUNT_SA: Counts all the votes for SA
* COUNT_TAS: Counts all the votes for TAS
* COUNT_VIC: Counts all the votes for VIC
* COUNT_WA: Counts all the votes for WA
* COUNT_ALL: Counts the votes for all states and territories. This command is equivalent to executing:
  * COUNT_ACT
  * COUNT_NSW
  * COUNT_NT
  * COUNT_QLD
  * COUNT_SA
  * COUNT_TAS
  * COUNT_VIC
  * COUNT_WA

Note that the LOAD commands will download reasonably large files from the AEC website.

## Requirements

This application requires JDK 8 to be installed.