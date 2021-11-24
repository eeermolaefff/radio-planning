package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import PDFProcessor.PDF2Image;
import Robot.MyRobot;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Controller {

    @FXML
    private CheckBox defaultCheckbox;

    @FXML
    private CheckBox calibrationCheckbox;

    @FXML
    private CheckBox outsideCheckbox;

    @FXML
    private CheckBox innerCheckbox;

    @FXML
    private Text fileNameTxt;

    @FXML
    private TextField calibrationField;

    @FXML
    private MenuItem closeFileButton;

    @FXML
    private ChoiceBox<String> floorChoiceBox;

    @FXML
    private ImageView floorImage;

    @FXML
    private TextField kernelSizeField;

    @FXML
    private Slider kernelSizeSlider;

    @FXML
    private Button launchButton;

    @FXML
    private TextField minWallLenField;

    @FXML
    private TextField offsetXField;

    @FXML
    private TextField offsetYField;

    @FXML
    private MenuItem openFileButton;

    private class Configuration {
        public boolean _defaultCheckbox, _calibrationCheckbox, _outsideCheckbox, _innerCheckbox;
        public boolean _calibrationField, _kernelSizeField, _minWallLenField, _offsetXField, _offsetYField;
        public boolean _floorChoiceBox;
        public boolean _kernelSizeSlider;
        public boolean _launchButton;

        public Configuration() {

        }

        public void update() {
            defaultCheckbox.setDisable(!_defaultCheckbox);
            calibrationCheckbox.setDisable(!_calibrationCheckbox);
            outsideCheckbox.setDisable(!_outsideCheckbox);
            innerCheckbox.setDisable(!_innerCheckbox);
            calibrationField.setDisable(!_calibrationField);
            kernelSizeField.setDisable(!_kernelSizeField);
            minWallLenField.setDisable(!_minWallLenField);
            offsetXField.setDisable(!_offsetXField);
            offsetYField.setDisable(!_offsetYField);
            floorChoiceBox.setDisable(!_floorChoiceBox);
            kernelSizeSlider.setDisable(!_kernelSizeSlider);
            launchButton.setDisable(!_launchButton);
        }
    }


    String projectDir = "D:/MyPerfectApp/";
    MyRobot robot;
    int _kernelSize = 0, _minWallLen = 0, _contourExp = 2, _offsetX = -1, _offsetY = -1, _resolution = -1;
    Configuration[] configurations;
    int config_idx;


    private void clearDir() {
        for (String dirName : floorChoiceBox.getItems()) {
            dirName = projectDir + dirName;
            File dir = new File(dirName);
            for(File currentFile: dir.listFiles())
                currentFile.delete();
            dir.delete();
        }
    }

    private void setFloorImage(String fileName) {
        String filePath = projectDir + floorChoiceBox.getValue() + "/";
        Image croppedImage = new Image(filePath + fileName, floorImage.getFitWidth(), floorImage.getFitHeight(), true, true);
        floorImage.setImage(croppedImage);
        croppedImage.cancel();
    }

    private void disableEnvironment() {
        _kernelSize = 0;
        _minWallLen = 0;
        _contourExp = 2;
        _offsetX = -1;
        _offsetY = -1;
        _resolution = -1;

        configurations = null;
        new Configuration().update();////////////////////////
    }

    @FXML
    void initialize() throws AWTException {
        robot = new MyRobot();

        disableEnvironment();

        openFileButton.setOnAction(event -> {
            int numberOfPages = 0;
            FileChooser fc = new FileChooser();
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                try {
                    numberOfPages = PDF2Image.splitPDF(selectedFile.getAbsolutePath());
                } catch (IOException e) {
                    fileNameTxt.setText("Error");
                }

                configurations = new Configuration[numberOfPages];

                String[] floors = new String[numberOfPages];
                for (int i = 0; i < numberOfPages; i++) {
                    floors[i] = "Floor " + (i+1);
                    configurations[i] = new Configuration();
                    configurations[i]._floorChoiceBox = true;
                }

                configurations[0].update();
                floorChoiceBox.getItems().addAll(floors);
                fileNameTxt.setText(selectedFile.getName());
            }
            else {
                fileNameTxt.setText("Error");
            }
        });

        closeFileButton.setOnAction(event -> {
            floorImage.setImage(null);
            _kernelSize = 0;
            _minWallLen = 0;
            clearDir();
            kernelSizeField.setText("");
            offsetXField.setText("");
            offsetYField.setText("");
            calibrationField.setText("");
            minWallLenField.setText("");
            fileNameTxt.setText("");
            floorChoiceBox.getItems().clear();
            disableEnvironment();
        });

        floorChoiceBox.setOnAction(event -> {
            String floor = floorChoiceBox.getValue();
            if (floor != null) {
                config_idx = Integer.parseInt(floor.split(" ")[1]) - 1;
                configurations[config_idx]._kernelSizeField = true;
                configurations[config_idx]._minWallLenField = true;
                configurations[config_idx]._kernelSizeSlider = true;
                configurations[config_idx].update();

                String imagePath = projectDir + floor + "/floor.jpeg";
                Image croppedImage = new Image(imagePath, floorImage.getFitWidth(), floorImage.getFitHeight(), true, true);
                floorImage.setImage(croppedImage);
                croppedImage.cancel();
            }
        });

        kernelSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Integer kernelSize = (int) kernelSizeSlider.getValue();
                Integer wallLen = kernelSize * 3;
                _kernelSize = kernelSize;
                _minWallLen = wallLen;
                kernelSizeField.setText(kernelSize.toString());
                minWallLenField.setText(wallLen.toString());
                launchButton.setText("Process");
            }
        });

        kernelSizeSlider.setOnMouseReleased(event -> {
            String filePath = projectDir + floorChoiceBox.getValue() + "/";
            robot.imageCalibration(filePath + "floor.jpeg", Integer.parseInt(kernelSizeField.getText()));

            if (launchButton.isDisable()) {
                configurations[config_idx]._launchButton = true;
                configurations[config_idx]._calibrationCheckbox = true;
                configurations[config_idx]._defaultCheckbox = true;
                configurations[config_idx].update();
            }
            calibrationCheckbox.setSelected(false);
            calibrationCheckbox.setSelected(true);
        });

        kernelSizeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                kernelSizeSlider.setValue(0);
            else if (!newValue.matches("\\d*"))
                kernelSizeField.setText(oldValue);
            else
                kernelSizeSlider.setValue(Integer.parseInt(newValue));
        });

        offsetXField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                _offsetX = -1;
            else if (!newValue.matches("\\d*"))
               _offsetX = -1;
            else
                _offsetX = Integer.parseInt(newValue);
        });

        offsetYField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                _offsetY = -1;
            else if (!newValue.matches("\\d*"))
                _offsetY = -1;
            else
                _offsetY = Integer.parseInt(newValue);
        });

        calibrationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                _resolution = -1;
            else if (!newValue.matches("\\d*"))
                _resolution = -1;
            else
                _resolution = Integer.parseInt(newValue);
        });

        calibrationCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    defaultCheckbox.setSelected(!newValue);
                    outsideCheckbox.setSelected(!newValue);
                    innerCheckbox.setSelected(!newValue);

                    setFloorImage("calibration.jpeg");
                }
            }
        });

        defaultCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    calibrationCheckbox.setSelected(!newValue);
                    outsideCheckbox.setSelected(!newValue);
                    innerCheckbox.setSelected(!newValue);
                    setFloorImage("floor.jpeg");
                }
            }
        });

        outsideCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    calibrationCheckbox.setSelected(!newValue);
                    defaultCheckbox.setSelected(!newValue);
                    innerCheckbox.setSelected(!newValue);

                    setFloorImage("outsideCoverage.jpeg");
                }
            }
        });

        innerCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    calibrationCheckbox.setSelected(!newValue);
                    defaultCheckbox.setSelected(!newValue);
                    outsideCheckbox.setSelected(!newValue);
                    setFloorImage("innerCoverage.jpeg");
                }
            }
        });

        launchButton.setOnAction(event -> {
            String filePath = projectDir + floorChoiceBox.getValue() + "/";
            if (launchButton.getText().equals("Process")) {
                robot.processImage(filePath + "floor.jpeg", filePath + "calibration.jpeg", _kernelSize,  _minWallLen, _contourExp);

                configurations[config_idx]._offsetXField = true;
                configurations[config_idx]._offsetYField = true;
                configurations[config_idx]._calibrationField = true;
                configurations[config_idx]._innerCheckbox = true;
                configurations[config_idx]._outsideCheckbox = true;
                configurations[config_idx].update();

                innerCheckbox.setSelected(false);
                innerCheckbox.setSelected(true);
                launchButton.setText("Launch");
            }
            else if(launchButton.getText().equals("Launch")) {
                try {
                    MyRobot.putContourOnMap(filePath, new MyRobot.Point(_offsetX, _offsetY), _resolution);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    };

}
