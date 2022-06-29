package GUI;

import OIP.ImageProcessor;
import PDFProcessor.PassportMaker;
import PDFProcessor.RPMaker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import PDFProcessor.PDF2Image;
import Robot.MyRobot;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Controller {

    @FXML
    private MenuItem combinePhotosButton;

    @FXML
    private MenuItem rebuildRPButton;

    @FXML
    private MenuItem buildRPButton;

    @FXML
    private MenuItem buildPassportButton;

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
    enum EnvCondition {fileChoosing, floorChoosing, preCalibrationMode, calibrationMode, launchMode, afterLaunchMode};

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
            minWallLenField.setText(minWallLen.toString());

            switch (env) {
                case fileChoosing:
                    buildPassportButton.setDisable(false);
                    buildRPButton.setDisable(false);
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
    private String dataFolder = "E:\\YandexDisk\\!!! Проектирование Омск (1)\\Саша\\Новые проекты\\Данные";
    private String readyProjectsFolder  ="E:\\YandexDisk\\!!! Проектирование Омск (1)\\Саша\\Новые проекты\\Готовые";
    private String selectedFilePath;
    private Integer calibrationDifference = 1;
    private Integer DPI = 100;
    private final String extension = ".png";
    private Configuration[] configurations;
    private int currentConfig;

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
            FileChooser fileChooser = new FileChooser();
            String initialDirName = dataFolder;
            try {
                initialDirName = new String(URLDecoder.decode(initialDirName, "UTF-8").getBytes("WINDOWS-1251")) + "\\";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            fileChooser.setInitialDirectory(new File(initialDirName));
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                selectedFilePath = selectedFile.getAbsolutePath();
                try {
                    numberOfPages = PDF2Image.splitPDF(selectedFilePath, 100, extension);
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
                fileNameTxt.setText("No file chosen");
            }
        });

        combinePhotosButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(dataFolder));
            File selectedDir = directoryChooser.showDialog(null);
            if (selectedDir != null) {
                try {
                    String builderDir = selectedDir.getPath() + "\\";
                    fileNameTxt.setText(builderDir);
                    PassportMaker.combinePhotos(builderDir);
                } catch (IOException e) {
                    fileNameTxt.setText("Couldn't open folder");
                }
            }
            else {
                fileNameTxt.setText("No file chosen");
            }
        });

        rebuildRPButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            String initialDirName = readyProjectsFolder;
            try {
                initialDirName = new String(URLDecoder.decode(initialDirName, "UTF-8").getBytes("WINDOWS-1251")) + "\\";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            fileChooser.setInitialDirectory(new File(initialDirName));
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                try {
                    String rebuildDir = selectedFile.getPath();
                    fileNameTxt.setText(rebuildDir);
                    RPMaker.remakeRP(rebuildDir);
                } catch (IOException e) {
                    e.printStackTrace();
                    fileNameTxt.setText("Couldn't open file");
                }
            }
            else {
                fileNameTxt.setText("No file chosen");
            }
        });

        buildRPButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(readyProjectsFolder));
            File selectedDir = directoryChooser.showDialog(null);
            if (selectedDir != null) {
                try {
                    String builderDir = selectedDir.getPath() + "\\";
                    System.out.println(builderDir);
                    fileNameTxt.setText(builderDir);
                    RPMaker.makeRP(builderDir);
                } catch (IOException e) {
                    fileNameTxt.setText("Couldn't open folder");
                }
            }
            else {
                fileNameTxt.setText("No file chosen");
            }
        });

        buildPassportButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(readyProjectsFolder));
            File selectedDir = directoryChooser.showDialog(null);
            if (selectedDir != null) {
                try {
                    String builderDir = selectedDir.getPath() + "\\";
                    System.out.println(builderDir);
                    fileNameTxt.setText(builderDir);
                    PassportMaker.makePassport(builderDir);
                } catch (IOException e) {
                    fileNameTxt.setText("Couldn't open folder");
                }
            }
            else {
                fileNameTxt.setText("No file chosen");
            }
        });

        closeFileButton.setOnAction(event -> {
            disableEnvironment();
        });

        floorChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValueStr) ->  {
                    if (newValueStr != null && !newValueStr.equals(oldValue)) {
                        currentConfig = Integer.parseInt(newValueStr.split(" ")[1]) - 1;
                        if (configurations[currentConfig].env == EnvCondition.floorChoosing) {
                            configurations[currentConfig].env = EnvCondition.preCalibrationMode;
                            configurations[currentConfig].checkbox = ChosenCheckbox.DEFAULT;
                        }

                        if (configurations[currentConfig].env == EnvCondition.launchMode)
                            launchButton.setText("Launch");
                        else
                            launchButton.setText("Process");

                        configurations[currentConfig].update();
                        if (configurations[currentConfig].checkbox == null) {
                            configurations[currentConfig].checkbox = ChosenCheckbox.DEFAULT;
                        }
                    }
                } );

        kernelSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (configurations != null) {
                int kernelSize = (int) kernelSizeSlider.getValue();
                int wallLen = kernelSize * 3;
                configurations[currentConfig].kernelSize = kernelSize;
                configurations[currentConfig].minWallLen = wallLen;
                kernelSizeField.setText(kernelSize == 0 ? "" : Integer.toString(kernelSize));
                minWallLenField.setText(kernelSize == 0 ? "" : Integer.toString(wallLen));
            }
        });

        kernelSizeSlider.setOnMouseReleased(event -> {
            String field = kernelSizeField.getText();
            if (field.equals("")) {
                configurations[currentConfig].env = EnvCondition.preCalibrationMode;
                configurations[currentConfig].checkbox = ChosenCheckbox.DEFAULT;
            }
            else {
                String filePath = projectDir + floorChoiceBox.getValue() + "/";
                ImageProcessor.imageCalibration(filePath + "floor" + extension,
                        Integer.parseInt(field), configurations[currentConfig].fillTable, calibrationDifference);
                configurations[currentConfig].checkbox = ChosenCheckbox.CALIBRATION;
                configurations[currentConfig].env = EnvCondition.calibrationMode;
            }

            configurations[currentConfig].update();
            launchButton.setText("Process");
        });

        kernelSizeField.setOnKeyPressed(event -> {
            if (!event.getCode().equals(KeyCode.ENTER))
                return;

            String newValueStr = kernelSizeField.getText();
            if (newValueStr == null || newValueStr.equals("") || !newValueStr.matches("\\d*"))
                kernelSizeSlider.setValue(0);
            else {
                int newValue = Integer.parseInt(newValueStr);
                kernelSizeSlider.setValue(newValue);

                if (newValue == 0) {
                    configurations[currentConfig].env = EnvCondition.preCalibrationMode;
                    configurations[currentConfig].checkbox = ChosenCheckbox.DEFAULT;
                }
                else {
                    configurations[currentConfig].env = EnvCondition.calibrationMode;
                    String filePath = projectDir + floorChoiceBox.getValue() + "/";
                    ImageProcessor.imageCalibration(filePath + "floor" + extension,
                            newValue, configurations[currentConfig].fillTable, calibrationDifference);
                    configurations[currentConfig].checkbox = ChosenCheckbox.CALIBRATION;
                }
                configurations[currentConfig].update();
                launchButton.setText("Process");
            }
        });

        minWallLenField.setOnKeyPressed(event -> {
            if (!event.getCode().equals(KeyCode.ENTER))
                return;

            String newValueStr = minWallLenField.getText();
            if (newValueStr == null || newValueStr.equals("") || newValueStr.equals("0")  || !newValueStr.matches("\\d*")) {
                minWallLenField.setText("");
                configurations[currentConfig].env = EnvCondition.calibrationMode;
            }
            else {
                String filePath = projectDir + floorChoiceBox.getValue() + "/";
                configurations[currentConfig].minWallLen = Integer.parseInt(newValueStr);
                ImageProcessor.processImage(filePath + "floor" + extension, filePath + "calibration" + extension,
                        configurations[currentConfig].kernelSize,   configurations[currentConfig].minWallLen,
                        configurations[currentConfig].contourExp);
                configurations[currentConfig].env = EnvCondition.launchMode;
                configurations[currentConfig].checkbox = ChosenCheckbox.INNER;
            }
            configurations[currentConfig].update();
        });

        calibrationDifferenceField.setOnAction(event -> {
            String newValueStr = calibrationDifferenceField.getText();

            if (newValueStr == null || newValueStr.equals("") || newValueStr.equals("0") || !newValueStr.matches("\\d*"))
                calibrationDifferenceField.setText("");
            else {
                calibrationDifference = Integer.parseInt(newValueStr);

                String filePath = projectDir + floorChoiceBox.getValue() + "/";
                ImageProcessor.imageCalibration(filePath + "floor" + extension, configurations[currentConfig].kernelSize,
                        configurations[currentConfig].fillTable, calibrationDifference);

                configurations[currentConfig].checkbox = ChosenCheckbox.CALIBRATION;
                configurations[currentConfig].update();
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
                        PDF2Image.splitPDF(selectedFilePath, DPI, extension);
                    } catch (IOException e) {
                        fileNameTxt.setText("Couldn't open PDF file");
                    }

                    configurations[currentConfig].env = EnvCondition.calibrationMode;
                    configurations[currentConfig].checkbox = ChosenCheckbox.DEFAULT;
                    kernelSizeSlider.setValue(0);

                    configurations[currentConfig].update();
                }
            }
        });

        fillTableCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            String field = kernelSizeField.getText();
            if (!field.equals("") && !field.equals("0")) {
                String filePath = projectDir + floorChoiceBox.getValue() + "/";
                ImageProcessor.imageCalibration(filePath + "floor" + extension,
                        Integer.parseInt(field), newValue, calibrationDifference);

                configurations[currentConfig].fillTable = newValue;
                configurations[currentConfig].env = EnvCondition.calibrationMode;
                configurations[currentConfig].checkbox = ChosenCheckbox.CALIBRATION;
                configurations[currentConfig].update();
            }
        });

        calibrationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                defaultCheckbox.setSelected(false);
                outsideCheckbox.setSelected(false);
                innerCheckbox.setSelected(false);

                setFloorImage("calibration" + extension);
                configurations[currentConfig].checkbox = ChosenCheckbox.CALIBRATION;
            }
        });

        defaultCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                calibrationCheckbox.setSelected(false);
                outsideCheckbox.setSelected(false);
                innerCheckbox.setSelected(false);
                setFloorImage("floor" + extension);
                configurations[currentConfig].checkbox = ChosenCheckbox.DEFAULT;
            }
        });

        outsideCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                calibrationCheckbox.setSelected(false);
                defaultCheckbox.setSelected(false);
                innerCheckbox.setSelected(false);
                setFloorImage("outsideCoverage" + extension);
                configurations[currentConfig].checkbox = ChosenCheckbox.OUTSIDE;
            }
        });

        innerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                calibrationCheckbox.setSelected(false);
                defaultCheckbox.setSelected(false);
                outsideCheckbox.setSelected(false);
                setFloorImage("innerCoverage" + extension);
                configurations[currentConfig].checkbox = ChosenCheckbox.INNER;
            }
        });

        launchButton.setOnAction(event -> {
            String filePath = projectDir + floorChoiceBox.getValue() + "/";
            if (configurations[currentConfig].env == EnvCondition.calibrationMode) {
                ImageProcessor.processImage(filePath + "floor" + extension, filePath + "calibration" + extension,
                        configurations[currentConfig].kernelSize,   configurations[currentConfig].minWallLen,
                        configurations[currentConfig].contourExp);

                configurations[currentConfig].env = EnvCondition.launchMode;
                configurations[currentConfig].checkbox = ChosenCheckbox.INNER;
                configurations[currentConfig].update();

                launchButton.setText("Launch");
            }
            else if(configurations[currentConfig].env == EnvCondition.launchMode) {
                try {
                    MyRobot.openPrevWindow();
                    for (int i = currentConfig; i < configurations.length; i++) {
                        if (configurations[i].env != EnvCondition.launchMode)
                            continue;
                        filePath = projectDir + "Floor " + (i+1) +"/";
                        MyRobot.switchFloor(i);
                        MyRobot.putContourOnMap(filePath, extension);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    };

}
