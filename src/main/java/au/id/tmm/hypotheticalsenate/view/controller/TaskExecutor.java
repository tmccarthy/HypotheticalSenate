package au.id.tmm.hypotheticalsenate.view.controller;

import au.id.tmm.hypotheticalsenate.GUIMain;
import au.id.tmm.hypotheticalsenate.controller.commands.HypotheticalSenateTask;
import au.id.tmm.hypotheticalsenate.database.HypotheticalSenateDatabase;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Task;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A class responsible for executing {@link HypotheticalSenateTask}s off the GUI thread.
 *
 * @author timothy
 */
public class TaskExecutor {

    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper();

    public void run(HypotheticalSenateTask hypotheticalSenateTask,
                    File downloadDirectory,
                    File databaseLocation,
                    Election election,
                    AustralianState state,
                    @Nullable Runnable onComplete) {

        Optional<Runnable> onCompleteOptional = Optional.ofNullable(onComplete);

        if (this.running.get()) {
            throw new IllegalStateException("A task is already running");
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                hypotheticalSenateTask.execute(
                        downloadDirectory,
                        new HypotheticalSenateDatabase(databaseLocation),
                        election,
                        state);
                return null;
            }

            @Override
            protected void succeeded() {
                onTaskComplete(this, onCompleteOptional);
            }

            @Override
            protected void cancelled() {
                onTaskComplete(this, onCompleteOptional);
            }

            @Override
            protected void failed() {
                onTaskComplete(this, onCompleteOptional);
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        this.running.set(true);
        thread.start();
    }

    private void onTaskComplete(Future<Void> future, Optional<Runnable> runnable) {
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace(GUIMain.err);
        } finally {
            running.set(false);
            runnable.ifPresent(Runnable::run);
        }
    }


    public boolean getRunning() {
        return running.get();
    }

    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }
}
