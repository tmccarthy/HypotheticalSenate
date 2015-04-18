package au.id.tmm.hypotheticalsenate;

import au.id.tmm.hypotheticalsenate.view.controller.GUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.PrintStream;

/**
 * @author timothy
 */
public class GUIMain extends Application {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintStream out = System.out;
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static PrintStream err = System.err;

    public static void main(String[] args) {
        launch(GUIMain.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(GUIMain.class.getResource("/gui.fxml").openStream());

        GUIController controller = fxmlLoader.getController();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
