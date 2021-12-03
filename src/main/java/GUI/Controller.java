package GUI;

import OIP.ImageProcessor;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
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
    private TextField DPIField;

    @FXML
    private TextField calibrationDifferenceField;

    @FXML
    private CheckBox fillTableCheckbox;

    @FXML
    private CheckBox defaultCheckbox;

    @FXML
    private HBox imageVIewHBOX;

    @FXML
    private CheckBox calibrationCheckbox;

    @FXML
    private CheckBox outsideCheckbox;

    @FXML
    private CheckBox innerCheckbox;

    @FXML
    private Text fileNameTxt;

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
    private MenuItem openFileButton;

    enum ChosenCheckbox {DEFAULT, CALIBRATION, OUTSIDE, INNER};
    enum EnvCondition {fileChoosing, floorChoosing, preCalibrationMode, calibrationMode, launchMode};

    private class Configuration {
        public boolean fillTable;
        public EnvCondition env = EnvCondition.fileChoosing;
        public Integer kernelSize = 0;
        public Integer minWallLen = 0;
        public Integer contourExp = 2;
        ChosenCheckbox checkbox = null;

        public void update() {
            DPIField.setText(DPI.toString());
            calibrationDifferenceField.setText(calibrationDifference.toString());

            kernelSizeSlider.setValue(kernelSize);

            switch (env) {
                case fileChoosing:
                    floorChoiceBox.setDisable(true);
                    kernelSizeSlider.setDisable(true);
                    kernelSizeField.setDisable(true);
                    minWallLenField.setDisable(true);
                    calibrationDifferenceField.setDisable(true);
                    DPIField.setDisable(true);
                    fillTableCheckbox.setDisable(true);
                    calibrationCheckbox.setDisable(true);
                    defaultCheckbox.setDisable(true);
                    outsideCheckbox.setDisable(true);
                    innerCheckbox.setDisable(true);
                    launchButton.setDisable(true);
                    break;
                case floorChoosing:
                    floorChoiceBox.setDisable(false);
                    kernelSizeSlider.setDisable(true);
                    kernelSizeField.setDisable(true);
                    minWallLenField.setDisable(true);
                    calibrationDifferenceField.setDisable(true);
                    DPIField.setDisable(true);
                    fillTableCheckbox.setDisable(true);
                    calibrationCheckbox.setDisable(true);
                    defaultCheckbox.setDisable(true);
                    outsideCheckbox.setDisable(true);
                    innerCheckbox.setDisable(true);
                    launchButton.setDisable(true);
                    break;
                case preCalibrationMode:
                    floorChoiceBox.setDisable(false);
                    kernelSizeSlider.setDisable(false);
                    kernelSizeField.setDisable(false);
                    minWallLenField.setDisable(false);
                    calibrationDifferenceField.setDisable(false);
                    DPIField.setDisable(false);
                    fillTableCheckbox.setDisable(false);
                    calibrationCheckbox.setDisable(true);
                    defaultCheckbox.setDisable(true);
                    outsideCheckbox.setDisable(true);
                    innerCheckbox.setDisable(true);
                    launchButton.setDisable(true);
                    break;
                case calibrationMode:
                    floorChoiceBox.setDisable(false);
                    kernelSizeSlider.setDisable(false);
                    kernelSizeField.setDisable(false);
                    minWallLenField.setDisable(false);
                    calibrationDifferenceField.setDisable(false);
                    DPIField.setDisable(false);
                    fillTableCheckbox.setDisable(false);
                    calibrationCheckbox.setDisable(false);
                    defaultCheckbox.setDisable(false);
                    outsideCheckbox.setDisable(true);
                    innerCheckbox.setDisable(true);
                    launchButton.setDisable(false);
                    break;
                case launchMode:
                    floorChoiceBox.setDisable(false);
                    kernelSizeSlider.setDisable(false);
                    kernelSizeField.setDisable(false);
                    minWallLenField.setDisable(false);
                    calibrationDifferenceField.setDisable(false);
                    DPIField.setDisable(false);
                    fillTableCheckbox.setDisable(false);
                    calibrationCheckbox.setDisable(false);
                    defaultCheckbox.setDisable(false);
                    outsideCheckbox.setDisable(false);
                    innerCheckbox.setDisable(false);
                    launchButton.setDisable(false);
                    break;
            }

            if (checkbox != null)
                switch (checkbox) {
                    case INNER:
                        innerCheckbox.setSelected(false);
                        innerCheckbox.setSelected(true);
                        break;
                    case OUTSIDE:
                        outsideCheckbox.setSelected(false);
                        outsideCheckbox.setSelected(true);
                        break;
                    case CALIBRATION:
                        calibrationCheckbox.setSelected(false);
                        calibrationCheckbox.setSelected(true);
                        break;
                    case DEFAULT:
                        defaultCheckbox.setSelected(false);
                        defaultCheckbox.setSelected(true);
                        break;
                }

        }
    }

    private String projectDir;
    private String selectedFilePath;
    private Integer calibrationDifference = 1;
    private Integer DPI = 100;
    private final String format = ".png";
    private Configuration[] configurations;
    private int current_config;

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
        Image croppedImage = new Image(filePath + fileName,  imageVIewHBOX.getWidth(), imageVIewHBOX.getHeight(), true, true);
        floorImage.setImage(croppedImage);
        croppedImage.cancel();
    }

    private void disableEnvironment() {
        floorImage.setImage(null);
        clearDir();
        fileNameTxt.setText("");
        floorChoiceBox.getItems().clear();
        new Configuration().update();
        configurations = null;
        selectedFilePath = null;
        calibrationDifference = 1;
        DPI = 100;
    }

    @FXML
    void initialize() {
        projectDir = System.getProperty("user.dir") + "/";
        new Configuration().update();

        openFileButton.setOnAction(event -> {
            int numberOfPages = 0;
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("E:/Clean"));
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                selectedFilePath = selectedFile.getAbsolutePath();
                try {
                    numberOfPages = PDF2Image.splitPDF(selectedFilePath, 100, format);
                } catch (IOException e) {
                    fileNameTxt.setText("Couldn't open PDF file");
                }

                configurations = new Configuration[numberOfPages];

                String[] floors = new String[numberOfPages];
                for (int i = 0; i < numberOfPages; i++) {
                    floors[i] = "Floor " + (i+1);
                    configurations[i] = new Configuration();
                    configurations[i].env = EnvCondition.floorChoosing;
                }

                floorChoiceBox.getItems().addAll(floors);
                fileNameTxt.setText(selectedFile.getName());
                configurations[0].update();
            }
            else {
                fileNameTxt.setText("Error");
            }
        });

        closeFileButton.setOnAction(event -> {
            disableEnvironment();
        });

        floorChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) ->  {
                    if (newValue != null && !newValue.equals(oldValue)) {
                        current_config = Integer.parseInt(newValue.split(" ")[1]) - 1;
                        if (configurations[current_config].env == EnvCondition.floorChoosing) {
                            configurations[current_config].env = EnvCondition.preCalibrationMode;
                            configurations[current_config].checkbox = ChosenCheckbox.DEFAULT;
                        }

                        if (configurations[current_config].env == EnvCondition.launchMode)
                            launchButton.setText("Launch");
                        else
                            launchButton.setText("Process");

                        configurations[current_config].update();
                        if (configurations[current_config].checkbox == null) {
                            configurations[current_config].checkbox = ChosenCheckbox.DEFAULT;
                        }
                    }
                } );

        kernelSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (configurations != null) {
                    Integer kernelSize = (int) kernelSizeSlider.getValue();
                    Integer wallLen = kernelSize * 3;
                    configurations[current_config].kernelSize = kernelSize;
                    configurations[current_config].minWallLen = wallLen;
                    kernelSizeField.setText(kernelSize == 0 ? "" : kernelSize.toString());
                    minWallLenField.setText(kernelSize == 0 ? "" : wallLen.toString());
                }
            }
        });

        kernelSizeSlider.setOnMouseReleased(event -> {
            String field = kernelSizeField.getText();
            if (field.equals("")) {
                configurations[current_config].env = EnvCondition.preCalibrationMode;
                configurations[current_config].checkbox = ChosenCheckbox.DEFAULT;
            }
            else {
                String filePath = projectDir + floorChoiceBox.getValue() + "/";
                ImageProcessor.imageCalibration(filePath + "floor" + format,
                        Integer.parseInt(field), configurations[current_config].fillTable, calibrationDifference);
                configurations[current_config].checkbox = ChosenCheckbox.CALIBRATION;
                configurations[current_config].env = EnvCondition.calibrationMode;
            }

            configurations[current_config].update();
            launchButton.setText("Process");
        });

        kernelSizeField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!event.getCode().equals(KeyCode.ENTER))
                    return;

                String newValue = kernelSizeField.getText();
                if (newValue == null || newValue.equals("") || !newValue.matches("\\d*"))
                    kernelSizeSlider.setValue(0);
                else {
                    int val = Integer.parseInt(newValue);
                    kernelSizeSlider.setValue(val);

                    if (val == 0) {
                        configurations[current_config].env = EnvCondition.preCalibrationMode;
                        configurations[current_config].checkbox = ChosenCheckbox.DEFAULT;
                    }
                    else {
                        configurations[current_config].env = EnvCondition.calibrationMode;
                        String filePath = projectDir + floorChoiceBox.getValue() + "/";
                        ImageProcessor.imageCalibration(filePath + "floor" + format,
                                val, configurations[current_config].fillTable, calibrationDifference);
                        configurations[current_config].checkbox = ChosenCheckbox.CALIBRATION;
                    }
                    configurations[current_config].update();
                    launchButton.setText("Process");
                }
            }
        });

        minWallLenField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!event.getCode().equals(KeyCode.ENTER))
                    return;

                String newValue = minWallLenField.getText();
                if (newValue == null || newValue.equals("") || newValue.equals("0")  || !newValue.matches("\\d*")) {
                    minWallLenField.setText("");
                    configurations[current_config].env = EnvCondition.calibrationMode;
                }
                else {
                    String filePath = projectDir + floorChoiceBox.getValue() + "/";
                    configurations[current_config].minWallLen = Integer.parseInt(newValue);
                    ImageProcessor.processImage(filePath + "floor" + format, filePath + "calibration" + format,
                            configurations[current_config].kernelSize,   configurations[current_config].minWallLen,
                            configurations[current_config].contourExp);
                    configurations[current_config].env = EnvCondition.launchMode;
                    configurations[current_config].checkbox = ChosenCheckbox.INNER;
                }
                configurations[current_config].update();
            }
        });

        calibrationDifferenceField.setOnAction(event -> {
            String newValue = calibrationDifferenceField.getText();

            if (newValue == null || newValue.equals("") || newValue.equals("0") || !newValue.matches("\\d*"))
                calibrationDifferenceField.setText("");
            else {
                calibrationDifference = Integer.parseInt(newValue);

                String filePath = projectDir + floorChoiceBox.getValue() + "/";
                ImageProcessor.imageCalibration(filePath + "floor" + format, configurations[current_config].kernelSize,
                        configurations[current_config].fillTable, calibrationDifference);

                configurations[current_config].checkbox = ChosenCheckbox.CALIBRATION;
                configurations[current_config].update();
            }
        });

        DPIField.setOnAction(event -> {
            String newValue = DPIField.getText();
            if (newValue == null || newValue.equals("") || newValue.equals("0") || !newValue.matches("\\d*"))
                DPIField.setText("");
            else {
                DPI = Integer.parseInt(newValue);
                if (selectedFilePath != null) {
                    try {
                        PDF2Image.splitPDF(selectedFilePath, DPI, format);
                    } catch (IOException e) {
                        fileNameTxt.setText("Couldn't open PDF file");
                    }

                    configurations[current_config].env = EnvCondition.calibrationMode;
                    configurations[current_config].checkbox = ChosenCheckbox.DEFAULT;
                    kernelSizeSlider.setValue(0);

                    configurations[current_config].update();
                }
            }
        });

        fillTableCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                String field = kernelSizeField.getText();
                if (!field.equals("") && !field.equals("0")) {
                    String filePath = projectDir + floorChoiceBox.getValue() + "/";
                    ImageProcessor.imageCalibration(filePath + "floor" + format,
                            Integer.parseInt(field), newValue, calibrationDifference);
                    configurations[current_config].fillTable = newValue;
                    configurations[current_config].env = EnvCondition.calibrationMode;
                    configurations[current_config].checkbox = ChosenCheckbox.CALIBRATION;
                    configurations[current_config].update();
                }
            }
        });

        calibrationCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    defaultCheckbox.setSelected(!newValue);
                    outsideCheckbox.setSelected(!newValue);
                    innerCheckbox.setSelected(!newValue);
                    setFloorImage("calibration" + format);
                    configurations[current_config].checkbox = ChosenCheckbox.CALIBRATION;
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
                    setFloorImage("floor" + format);
                    configurations[current_config].checkbox = ChosenCheckbox.DEFAULT;
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
                    setFloorImage("outsideCoverage" + format);
                    configurations[current_config].checkbox = ChosenCheckbox.OUTSIDE;
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
                    setFloorImage("innerCoverage" + format);
                    configurations[current_config].checkbox = ChosenCheckbox.INNER;
                }
            }
        });

        launchButton.setOnAction(event -> {
            String filePath = projectDir + floorChoiceBox.getValue() + "/";
            if (configurations[current_config].env == EnvCondition.calibrationMode) {
                ImageProcessor.processImage(filePath + "floor" + format, filePath + "calibration" + format,
                        configurations[current_config].kernelSize,   configurations[current_config].minWallLen,
                        configurations[current_config].contourExp);

                configurations[current_config].env = EnvCondition.launchMode;
                configurations[current_config].checkbox = ChosenCheckbox.INNER;
                configurations[current_config].update();

                launchButton.setText("Launch");
            }
            else if(configurations[current_config].env == EnvCondition.launchMode) {
                try {
                    MyRobot.openPrevWindow();
                    for (int i = current_config; i < configurations.length; i++) {
                        if (configurations[i].env != EnvCondition.launchMode)
                            continue;

                        filePath = projectDir + "Floor " + (i + 1) + "/";
                        MyRobot.switchLevel(i);
                        MyRobot.putContourOnMap(filePath);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    };

}
