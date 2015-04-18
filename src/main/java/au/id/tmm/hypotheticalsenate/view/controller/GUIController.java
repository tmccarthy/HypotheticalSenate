package au.id.tmm.hypotheticalsenate.view.controller;

import au.id.tmm.hypotheticalsenate.GUIMain;
import au.id.tmm.hypotheticalsenate.controller.commands.HypotheticalSenateTask;
import au.id.tmm.hypotheticalsenate.model.AustralianState;
import au.id.tmm.hypotheticalsenate.model.Election;
import au.id.tmm.hypotheticalsenate.view.model.GUIModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author timothy
 */
public class GUIController {

    private final GUIModel guiModel = new GUIModel();
    private final TaskExecutor taskExecutor = new TaskExecutor();

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Label downloadsDirLbl;
    @FXML private TextField downloadsDirTxtFld;
    @FXML private Button downloadsDirPickerBtn;
    @FXML private Label taskLbl;
    @FXML private ChoiceBox<HypotheticalSenateTask> taskPicker;
    @FXML private Label electionLbl;
    @FXML private ChoiceBox<Election> electionPicker;
    @FXML private Label stateLbl;
    @FXML private ChoiceBox<AustralianState> statePicker;
    @FXML private Button runButton;
    @FXML private TextArea consoleTextArea;
    @FXML private Label databaseLbl;
    @FXML private TextField databaseTxtFld;
    @FXML private Button databasePickerBtn;

    @FXML
    void initialize() {
        this.initComponents();
        this.initBindings();
        this.initListeners();
    }

    private void initComponents() {
        setupChoiceBox(this.taskPicker, HypotheticalSenateTask::getName, HypotheticalSenateTask.values());
        setupChoiceBox(this.electionPicker, Election::getDescription, Election.values());
        setupChoiceBox(this.statePicker, AustralianState::getName, AustralianState.values());

        this.downloadsDirTxtFld.setEditable(false);
        this.databaseTxtFld.setEditable(false);

        this.refreshStateAndElectionRequired();
    }

    private void initBindings() {
        Bindings.bindBidirectional(guiModel.taskProperty(), this.taskPicker.valueProperty());
        Bindings.bindBidirectional(guiModel.electionProperty(), this.electionPicker.valueProperty());
        Bindings.bindBidirectional(guiModel.stateProperty(), this.statePicker.valueProperty());

        bindWithConversion(this.downloadsDirTxtFld.textProperty(), this.guiModel.downloadDirectoryProperty(),
                file -> file != null ? file.getAbsolutePath() : null);

        bindWithConversion(this.databaseTxtFld.textProperty(), this.guiModel.databaseLocationProperty(),
                file -> file != null ? file.getAbsolutePath() : null);
    }

    private void initListeners() {
        this.guiModel.taskProperty().addListener((observable, oldValue, newValue) -> refreshStateAndElectionRequired());

        this.downloadsDirPickerBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select downloads directory");

            // Only update it if they've picked a new one
            Optional.ofNullable(directoryChooser.showDialog(null))
                    .ifPresent(this.guiModel::setDownloadDirectory);
        });

        this.databasePickerBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select database location");

            Optional.ofNullable(fileChooser.showOpenDialog(null))
                    .ifPresent(this.guiModel::setDatabaseLocation);
        });

        this.runButton.setOnAction(event -> {
            try {
                HypotheticalSenateTask hypotheticalSenateTask = this.guiModel.getTaskOptional()
                        .orElseThrow(missingParameterExceptionSupplier("Task"));
                Election election = this.guiModel.getElectionOptional()
                        .orElseThrow(missingParameterExceptionSupplier("Election"));
                AustralianState state = this.guiModel.getStateOptional()
                        .orElseThrow(missingParameterExceptionSupplier("State"));
                File downloadDirectory = this.guiModel.getDownloadDirectoryOptional()
                        .orElseThrow(missingParameterExceptionSupplier("Download directory"));
                File databaseLocation = this.guiModel.getDatabaseLocationOptional()
                        .orElseThrow(missingParameterExceptionSupplier("Database location"));

                Alert blocker = new Alert(AlertType.INFORMATION, "Running task " + hypotheticalSenateTask.getName());
                blocker.initModality(Modality.APPLICATION_MODAL);
                blocker.getButtonTypes().clear();
                blocker.setResult(ButtonType.OK);
                blocker.show();

                this.taskExecutor.run(
                        hypotheticalSenateTask,
                        downloadDirectory,
                        databaseLocation,
                        election,
                        state,
                        blocker::close);

            } catch (MissingParameterException e) {
                e.printStackTrace(GUIMain.err);
            }
        });
    }

    private void refreshStateAndElectionRequired() {
        Optional<HypotheticalSenateTask> task = this.guiModel.getTaskOptional();

        if (task.isPresent()) {
            this.electionPicker.setDisable(!task.get().isElectionRequired());
            this.statePicker.setDisable(!task.get().isStateRequired());
        } else {
            this.electionPicker.setDisable(true);
            this.statePicker.setDisable(true);
        }
    }

    private static Supplier<MissingParameterException> missingParameterExceptionSupplier(String parameterName) {
        return () -> new MissingParameterException("Parameter \"" + parameterName + "\" has not been defined");
    }

    private static <T, V> void bindWithConversion(Property<T> property,
                                                  ObservableValue<V> value,
                                                  Function<V, T> conversion) {
        property.setValue(conversion.apply(value.getValue()));
        value.addListener((observable, oldValue, newValue) -> property.setValue(conversion.apply(newValue)));
    }

    private static <T> void setupChoiceBox(ChoiceBox<T> choiceBox, Collection<T> values, Function<T, String> toString) {
        choiceBox.setItems(FXCollections.observableArrayList(values));
        choiceBox.setValue(choiceBox.getItems().stream().findFirst().orElse(null));
        choiceBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : toString.apply(object);
            }

            @Override
            public T fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
    }

    @SafeVarargs
    private static <T> void setupChoiceBox(ChoiceBox<T> choiceBox, Function<T, String> toString, T... values) {
        setupChoiceBox(choiceBox, Arrays.asList(values), toString);
    }
}
