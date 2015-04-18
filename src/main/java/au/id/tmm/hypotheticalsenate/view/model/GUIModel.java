package au.id.tmm.hypotheticalsenate.view.model;

import au.id.tmm.hypotheticalsenate.controller.commands.HypotheticalSenateTask;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Optional;

/**
 * @author timothy
 */
public class GUIModel {

    public static final File DEFAULT_DOWNLOAD_DIRECTORY = new File("aecData/");
    public static final File DEFAULT_DATABASE_LOCATION = new File("data.db");

    private ObjectProperty<HypotheticalSenateTask> task = new SimpleObjectProperty<>();
    private ObjectProperty<Election> election = new SimpleObjectProperty<>();
    private ObjectProperty<AustralianState> state = new SimpleObjectProperty<>();

    private ObjectProperty<File> downloadDirectory = new SimpleObjectProperty<>(DEFAULT_DOWNLOAD_DIRECTORY);
    private ObjectProperty<File> databaseLocation = new SimpleObjectProperty<>(DEFAULT_DATABASE_LOCATION);

    public HypotheticalSenateTask getTask() {
        return task.get();
    }

    public Optional<HypotheticalSenateTask> getTaskOptional() {
        return Optional.ofNullable(task.get());
    }

    public ObjectProperty<HypotheticalSenateTask> taskProperty() {
        return task;
    }

    public void setTask(HypotheticalSenateTask hypotheticalSenateTask) {
        this.task.set(hypotheticalSenateTask);
    }

    public Election getElection() {
        return election.get();
    }

    public Optional<Election> getElectionOptional() {
        return Optional.ofNullable(election.get());
    }

    public ObjectProperty<Election> electionProperty() {
        return election;
    }

    public void setElection(Election election) {
        this.election.set(election);
    }

    public AustralianState getState() {
        return state.get();
    }

    public Optional<AustralianState> getStateOptional() {
        return Optional.ofNullable(state.get());
    }

    public ObjectProperty<AustralianState> stateProperty() {
        return state;
    }

    public void setState(AustralianState state) {
        this.state.set(state);
    }

    public File getDownloadDirectory() {
        return downloadDirectory.get();
    }

    public Optional<File> getDownloadDirectoryOptional() {
        return Optional.ofNullable(downloadDirectory.get());
    }

    public ObjectProperty<File> downloadDirectoryProperty() {
        return downloadDirectory;
    }

    public void setDownloadDirectory(File downloadDirectory) {
        this.downloadDirectory.set(downloadDirectory);
    }

    public File getDatabaseLocation() {
        return databaseLocation.get();
    }

    public Optional<File> getDatabaseLocationOptional() {
        return Optional.ofNullable(databaseLocation.get());
    }

    public ObjectProperty<File> databaseLocationProperty() {
        return databaseLocation;
    }

    public void setDatabaseLocation(File databaseLocation) {
        this.databaseLocation.set(databaseLocation);
    }
}
