package au.id.tmm.hypotheticalsenate.view.controller;

import javafx.scene.control.TextInputControl;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author timothy
 */
public class WriteToTextComponentOutputStream extends OutputStream {

    private final TextInputControl textComponent;

    public WriteToTextComponentOutputStream(TextInputControl textComponent) {
        this.textComponent = textComponent;
    }

    @Override
    public void write(int b) throws IOException {
        this.textComponent.appendText(new String(new char[]{(char) b}));
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.textComponent.appendText(new String(b));
    }
}
