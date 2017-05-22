/*
 * Copyright (C) 2017 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.app.project.editor.modeler.slabmodel;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import burai.app.QEFXAppController;
import burai.app.QEFXMain;
import burai.app.project.QEFXProjectController;
import burai.app.project.viewer.modeler.slabmodel.SlabModeler;
import burai.com.consts.ConstantStyles;
import burai.com.graphic.svg.SVGLibrary;
import burai.com.graphic.svg.SVGLibrary.SVGData;
import burai.com.keys.KeyName;

public class QEFXSlabEditorController extends QEFXAppController {

    private static final double CTRL_GRAPHIC_SIZE = 20.0;
    private static final String CTRL_GRAPHIC_CLASS = "piclight-button";

    private static final double BUILD_GRAPHIC_SIZE = 20.0;
    private static final String BUILD_GRAPHIC_CLASS = "piclight-button";

    private static final String ERROR_STYLE = ConstantStyles.ERROR_COLOR;

    private QEFXProjectController projectController;

    private SlabModeler modeler;

    @FXML
    private Button screenButton;

    @FXML
    private Button initButton;

    @FXML
    private Button centerButton;

    @FXML
    private Label centerLabel;

    @FXML
    private Button superButton;

    @FXML
    private TextField scaleField1;

    @FXML
    private TextField scaleField2;

    @FXML
    private Slider slabSlider;

    @FXML
    private Slider vacuumSlider;

    @FXML
    private TilePane kindPane;

    public QEFXSlabEditorController(QEFXProjectController projectController, SlabModeler modeler) {
        super(projectController == null ? null : projectController.getMainController());

        if (modeler == null) {
            throw new IllegalArgumentException("modeler is null.");
        }

        this.projectController = projectController;

        this.modeler = modeler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setupScreenButton();
        this.setupInitButton();
        this.setupCenterButton();
        this.setupCenterLabel();

        this.setupSuperButton();
        this.setupScaleField(this.scaleField1);
        this.setupScaleField(this.scaleField2);

        this.setupSlabSlider();
        this.setupVacuumSlider();
        this.setupKindPane();
    }

    private void setupScreenButton() {
        if (this.screenButton == null) {
            return;
        }

        this.screenButton.setText("");
        this.screenButton.setGraphic(
                SVGLibrary.getGraphic(SVGData.CAMERA, CTRL_GRAPHIC_SIZE, null, CTRL_GRAPHIC_CLASS));

        this.screenButton.setOnAction(event -> {
            if (this.projectController != null) {
                this.projectController.sceenShot();
            }
        });
    }

    private void setupInitButton() {
        if (this.initButton == null) {
            return;
        }

        this.initButton.setText("");
        this.initButton.setGraphic(
                SVGLibrary.getGraphic(SVGData.INTO, CTRL_GRAPHIC_SIZE, null, CTRL_GRAPHIC_CLASS));

        this.initButton.setOnAction(event -> {
            if (this.modeler != null) {
                this.modeler.initialize();
            }
        });
    }

    private void setupCenterButton() {
        if (this.centerButton == null) {
            return;
        }

        this.centerButton.setText("");
        this.centerButton.setGraphic(
                SVGLibrary.getGraphic(SVGData.CENTER, CTRL_GRAPHIC_SIZE, null, CTRL_GRAPHIC_CLASS));

        this.centerButton.setOnAction(event -> {
            if (this.modeler != null) {
                this.modeler.center();
            }
        });
    }

    private void setupCenterLabel() {
        if (this.centerLabel == null) {
            return;
        }

        String text = this.centerLabel.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        text = text.replaceAll("Shortcut", KeyName.getShortcut());
        this.centerLabel.setText(text);
    }

    private void setupSuperButton() {
        if (this.superButton == null) {
            return;
        }

        this.superButton.setDisable(true);
        this.superButton.getStyleClass().add(BUILD_GRAPHIC_CLASS);
        this.superButton.setGraphic(
                SVGLibrary.getGraphic(SVGData.GEAR, BUILD_GRAPHIC_SIZE, null, BUILD_GRAPHIC_CLASS));

        String text = this.superButton.getText();
        if (text != null) {
            this.superButton.setText(text + " ");
        }

        this.superButton.setOnAction(event -> {
            if (this.modeler == null) {
                return;
            }

            int n1 = this.getScaleValue(this.scaleField1);
            int n2 = this.getScaleValue(this.scaleField2);
            boolean status = this.modeler.scaleSlabArea(n1, n2);

            if (!status) {
                this.showErrorDialog();
            }

            if (this.scaleField1 != null) {
                this.scaleField1.setText("");
            }
            if (this.scaleField2 != null) {
                this.scaleField2.setText("");
            }
        });
    }

    private void showErrorDialog() {
        Alert alert = new Alert(AlertType.ERROR);
        QEFXMain.initializeDialogOwner(alert);
        alert.setHeaderText("Error has occurred in modering.");
        alert.setContentText("Atoms are too much.");
        alert.showAndWait();
    }

    private boolean isAvailSuper() {
        int n1 = this.getScaleValue(this.scaleField1);
        if (n1 < 1) {
            return false;
        }

        int n2 = this.getScaleValue(this.scaleField2);
        if (n2 < 1) {
            return false;
        }

        return (n1 * n2) > 1;
    }

    private void setupScaleField(TextField textField) {
        if (textField == null) {
            return;
        }

        textField.setText("");
        textField.setStyle("");

        textField.textProperty().addListener(o -> {
            int value = this.getScaleValue(textField);
            if (value > 0) {
                textField.setStyle("");
            } else {
                textField.setStyle(ERROR_STYLE);
            }

            if (this.superButton != null) {
                this.superButton.setDisable(!this.isAvailSuper());
            }
        });

        textField.setOnAction(event -> {
            if (this.superButton != null && !(this.superButton.isDisable())) {
                EventHandler<ActionEvent> handler = this.superButton.getOnAction();
                if (handler != null) {
                    handler.handle(event);
                }
            }
        });
    }

    private int getScaleValue(TextField textField) {
        if (textField == null) {
            return 0;
        }

        String text = textField.getText();
        text = text == null ? null : text.trim();
        if (text == null || text.isEmpty()) {
            return 1;
        }

        int value = 0;

        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }

        return value;
    }

    private void setupSlabSlider() {
        if (this.slabSlider == null) {
            return;
        }

        // TODO
    }

    private void setupVacuumSlider() {
        if (this.vacuumSlider == null) {
            return;
        }

        // TODO
    }

    private void setupKindPane() {
        if (this.kindPane == null) {
            return;
        }

        // TODO
    }
}
