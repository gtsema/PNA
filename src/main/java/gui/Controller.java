package gui;

import hardware.Executor;
import hardware.PnaController;
import hardware.TableController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plotter.Plotter;
import utils.InputDataChecker;
import utils.PropertyHelper;

import javax.imageio.ImageIO;
import javax.naming.Binding;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private enum programStatus {OFF, READY, WORK, PAUSED, SETZERO};
    private enum pnaStatus {OFF, CONNECT, ON};
    private enum tableStatus {OFF, CONNECT, ON};

    private enum measureType {S11, S12, S13, S14, S21, S22, S23, S24, S31, S32, S33, S34, S41, S42, S43, S44};

    private final String rotateRight = "right";
    private final String rotateLeft = "left";
    private final String connectBtn = "Подключить";
    private final String disconnectBtn = "Отключить";
    private final String connected = "Подключено";
    private final String connect = "Подключение...";
    private final String disconnect = "Отключено";
    private final String tfOk = "";
    private final String tfError = "-fx-border-color: #FF6666;\n" +
                                   "-fx-border-width: 1px;\n" +
                                   "-fx-border-radius: 3px;\n" +
                                   "-fx-background-color: #FFF0F0;";

    private final Image rotateToZeroImg = new Image("/img/setnull.png");
    private final Image rotateToZeroProgressImg = new Image("/img/setnull.gif");
    private final Image rotateRightImg = new Image("/img/right.png");
    private final Image rotateLeftImg = new Image("/img/left.png");
    private final Image setZeroButtonImg = new Image("/img/null.png");

    private Executor pnaExecutor;
    private Executor tableExecutor;

    private Task measureTask;

    private int intermediateAngle;

    private double minScale = -40.0;
    private double maxScale = 0.0;

    private final ObservableList<Map.Entry<Integer, ArrayList<Double>>> measureData = FXCollections.observableArrayList();

    //Main
    @FXML
    private Accordion accordion;

    @FXML
    private TitledPane connectTitledPane;

    @FXML
    private TitledPane measurementTitledPane;

    @FXML
    private TitledPane viewTitledPane;

    @FXML
    private TableView<Map.Entry<Integer, ArrayList<Double>>> dataTableView;

    @FXML
    private TableColumn<Map.Entry<Integer, ArrayList<Double>>, Integer> angleTableColumn;

    @FXML
    private TableColumn<Map.Entry<Integer, ArrayList<Double>>, Double> numberTableColumn;

    @FXML
    private Pane graphPane;

    //Connect
    @FXML
    private TextField pnaIpTextField;

    @FXML
    private TextField pnaPortTextField;

    @FXML
    private Label pnaStatusLbl;

    @FXML
    private Button pnaConnectButton;

    @FXML
    private TextField tableIpTextField;

    @FXML
    private TextField tablePortTextField;

    @FXML
    private Label tableStatusLbl;

    @FXML
    private Button tableConnectButton;

    //Measure
    @FXML
    private ChoiceBox<String> sParamChoiceBox;

    @FXML
    protected TextField startFreqTextField;

    @FXML
    protected TextField stopFreqTextField;

    @FXML
    protected TextField stepFreqTextField;

    @FXML
    protected TextField rbwTextField;

    @FXML
    protected TextField averageTextField;

    @FXML
    protected TextField smoothTextField;

    @FXML
    protected TextField startAngleTextField;

    @FXML
    protected TextField stopAngleTextField;

    @FXML
    protected TextField stepAngleTextField;

    @FXML
    protected Button rotateToZeroButton;

    @FXML
    protected ImageView rotateToZeroButtonImageView;

    @FXML
    protected Button directionOfRotationButton;

    @FXML
    protected ImageView directionOfRotationButtonImageView;

    @FXML
    protected Button setZeroButton;

    @FXML
    protected ImageView setZeroButtonImageView;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;

    //View
    @FXML
    private ChoiceBox<Integer> freqChoiceBox;

    @FXML
    private CheckBox autoScaleCheckBox;

    @FXML
    private TextField minScaleTextField;

    @FXML
    private TextField maxScaleTextField;
    //Bar

    @FXML
    private void pnaConnectButtonPressed() {
        if(pnaStatusProperty.getValue() == pnaStatus.ON) {
            pnaStatusProperty.setValue(pnaStatus.OFF);
        } else {
            if(pnaIpTextField.styleProperty().getValue().equals(tfOk) && pnaPortTextField.styleProperty().getValue().equals(tfOk)) {
                checkPnaConnect();
            } else {
                if(pnaIpTextField.styleProperty().getValue().equals(tfError)) {
                    showPopupTooltip(pnaIpTextField, "Введите правильный IP");
                }

                if(pnaPortTextField.styleProperty().getValue().equals(tfError)) {
                    showPopupTooltip(pnaPortTextField, "Введите правильный порт");
                }
            }
        }
    }

    @FXML
    private void tableConnectButtonPressed() {
        if(tableStatusProperty.getValue() == tableStatus.ON) {
            tableStatusProperty.setValue(tableStatus.OFF);
        } else {
                if(tableIpTextField.styleProperty().getValue().equals(tfOk) && tablePortTextField.styleProperty().getValue().equals(tfOk)) {
                checkTableConnect();
            } else {
                    if(tableIpTextField.styleProperty().getValue().equals(tfError)) {
                    showPopupTooltip(tableIpTextField, "Введите правильный IP");
                }

                    if(tablePortTextField.styleProperty().getValue().equals(tfError)) {
                    showPopupTooltip(tablePortTextField, "Введите правильный порт");
                }
            }
        }
    }

    @FXML
    private void rotateToZeroButtonPressed() {
        if(programStatusProperty.getValue() == programStatus.READY ||
           programStatusProperty.getValue() == programStatus.OFF) programStatusProperty.setValue(programStatus.SETZERO);
        else if(programStatusProperty.getValue() == programStatus.SETZERO &&
                pnaStatusProperty.getValue() == pnaStatus.ON) programStatusProperty.setValue(programStatus.READY);
        else programStatusProperty.setValue(programStatus.OFF);
    }

    @FXML
    private void directionOfRotationButtonPressed() {
        directionOfRotationProperty.setValue(directionOfRotationProperty.getValue().equals(rotateRight) ? rotateLeft : rotateRight);
    }

    @FXML
    private void setZeroButtonPressed() {

    }

    @FXML
    private void startButtonPressed() {
        if(startFreqTextField.styleProperty().getValue().equals(tfOk) && stopFreqTextField.styleProperty().getValue().equals(tfOk) &&
           stepFreqTextField.styleProperty().getValue().equals(tfOk) && rbwTextField.styleProperty().getValue().equals(tfOk) &&
           averageTextField.styleProperty().getValue().equals(tfOk) && smoothTextField.styleProperty().getValue().equals(tfOk) &&
           startAngleTextField.styleProperty().getValue().equals(tfOk) && stopAngleTextField.styleProperty().getValue().equals(tfOk) &&
           stepAngleTextField.styleProperty().getValue().equals(tfOk)) {

                startMeasure(programStatusProperty.getValue());

        } else {
            if(startFreqTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(startFreqTextField, "Введите правильную начальную частоту");
            if(stopFreqTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(stopFreqTextField, "Введите правильную конечную частоту");
            if(stepFreqTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(stepFreqTextField, "Введите правильный шаг частоты");
            if(rbwTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(rbwTextField, "Введите правильное значение rbw");
            if(averageTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(averageTextField, "Введите правильное усреднение");
            if(smoothTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(smoothTextField, "Введите правильное сглаживание");
            if(startAngleTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(startAngleTextField, "Введите правильный начальный угол");
            if(stopAngleTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(stopAngleTextField, "Введите правильный конечный угол");
            if(stepAngleTextField.styleProperty().getValue().equals(tfError)) showPopupTooltip(stepAngleTextField, "Введите правильный шаг угла");
        }
    }

    @FXML
    private void pauseButtonPressed() {
        programStatusProperty.setValue(programStatus.PAUSED);
        measureTask.cancel();
        tableStop();
    }

    @FXML
    private void stopButtonPressed() {
        programStatusProperty.setValue(programStatus.READY);
        measureTask.cancel();
        tableStop();
    }

    //Bindings
    private final Property<programStatus> programStatusProperty = new SimpleObjectProperty<>(programStatus.OFF);

    private final Property<pnaStatus> pnaStatusProperty = new SimpleObjectProperty<>(pnaStatus.OFF);
    
    private final Property<tableStatus> tableStatusProperty = new SimpleObjectProperty<>(tableStatus.OFF);

    private final IntegerProperty startFreqProperty = new SimpleIntegerProperty(PropertyHelper.getStartFreq());
    private final IntegerProperty stopFreqProperty = new SimpleIntegerProperty(PropertyHelper.getStopFreq());
    private final IntegerProperty stepFreqProperty = new SimpleIntegerProperty(PropertyHelper.getStepFreq());
    private final IntegerProperty rbwProperty = new SimpleIntegerProperty(PropertyHelper.getRbw());
    private final IntegerProperty averageProperty = new SimpleIntegerProperty(PropertyHelper.getAverage());
    private final IntegerProperty smoothProperty = new SimpleIntegerProperty(PropertyHelper.getSmooth());
    private final IntegerProperty startAngleProperty = new SimpleIntegerProperty(PropertyHelper.getStartAngle());
    private final IntegerProperty stopAngleProperty = new SimpleIntegerProperty(PropertyHelper.getStopAngle());
    private final IntegerProperty stepAngleProperty = new SimpleIntegerProperty(PropertyHelper.getStepAngle());

    private final StringProperty directionOfRotationProperty = new SimpleStringProperty(rotateRight);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        programStatusProperty.addListener((observable, oldValue, newValue) -> System.out.println(newValue));

        pnaStatusProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == pnaStatus.ON && tableStatusProperty.getValue() == tableStatus.ON) {
                programStatusProperty.setValue(programStatus.READY);
            } else programStatusProperty.setValue(programStatus.OFF);
        });

        tableStatusProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == tableStatus.ON && pnaStatusProperty.getValue() == pnaStatus.ON) {
                programStatusProperty.setValue(programStatus.READY);
            } else programStatusProperty.setValue(programStatus.OFF);
        });

        createContextMenu();

        //Main
        accordion.setExpandedPane(connectTitledPane);

        dataTableView.setItems(measureData);

        angleTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<Integer, ArrayList<Double>>, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Map.Entry<Integer, ArrayList<Double>>, Integer> entryIntegerCellDataFeatures) {
                return new SimpleObjectProperty<Integer>(entryIntegerCellDataFeatures.getValue().getKey());
            }
        });

        numberTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<Integer, ArrayList<Double>>, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Map.Entry<Integer, ArrayList<Double>>, Double> entryDoubleCellDataFeatures) {
                return new SimpleObjectProperty<Double>(entryDoubleCellDataFeatures.getValue().getValue().get(freqChoiceBox.getSelectionModel().getSelectedIndex()));
            }
        });

        createEmptyGraph();

        //Connect
        //PNA
        pnaIpTextField.setText(PropertyHelper.getPnaIp());
        pnaPortTextField.setText(String.valueOf(PropertyHelper.getPnaPort()));

        pnaIpTextField.disableProperty().bind(Bindings.createBooleanBinding(() ->
            pnaStatusProperty.getValue() == pnaStatus.CONNECT ||
            pnaStatusProperty.getValue() == pnaStatus.ON, pnaStatusProperty));

        pnaPortTextField.disableProperty().bind(Bindings.createBooleanBinding(() ->
            pnaStatusProperty.getValue() == pnaStatus.CONNECT ||
            pnaStatusProperty.getValue() == pnaStatus.ON, pnaStatusProperty));

        pnaStatusLbl.textProperty().bind(Bindings.createStringBinding(() -> {
            if (pnaStatusProperty.getValue() == pnaStatus.OFF) return disconnect;
            else if (pnaStatusProperty.getValue() == pnaStatus.CONNECT) return connect;
            else if (pnaStatusProperty.getValue() == pnaStatus.ON) return connected;
            else return "";
        }, pnaStatusProperty));

        pnaConnectButton.textProperty().bind(Bindings.createStringBinding(() -> {
            if (pnaStatusProperty.getValue() == pnaStatus.ON) return disconnectBtn;
            else return connectBtn;
        }, pnaStatusProperty));

        pnaConnectButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            pnaStatusProperty.getValue() == pnaStatus.CONNECT, pnaStatusProperty));

        pnaIpTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9.]+")) {
                pnaIpTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidIp(newValue)) {
                pnaIpTextField.styleProperty().set(tfError);
            } else {
                pnaIpTextField.styleProperty().set(tfOk);
            }
        });

        pnaPortTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                pnaPortTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidPort(newValue)) {
                pnaPortTextField.styleProperty().set(tfError);
            } else {
                pnaPortTextField.styleProperty().set(tfOk);
            }
        });

        pnaStatusProperty.addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == pnaStatus.OFF && pnaExecutor != null) {
                    pnaExecutor.close();
                }
            } catch (IOException e) {
                logger.debug(e.getMessage());
            }
        });
        //TABLE
        tableIpTextField.setText(PropertyHelper.getTableIp());
        tablePortTextField.setText(String.valueOf(PropertyHelper.getTablePort()));

        tableIpTextField.disableProperty().bind(Bindings.createBooleanBinding(() ->
            tableStatusProperty.getValue() == tableStatus.CONNECT ||
            tableStatusProperty.getValue() == tableStatus.ON, tableStatusProperty));

        tablePortTextField.disableProperty().bind(Bindings.createBooleanBinding(() ->
            tableStatusProperty.getValue() == tableStatus.CONNECT ||
            tableStatusProperty.getValue() == tableStatus.ON, tableStatusProperty));

        tableStatusLbl.textProperty().bind(Bindings.createStringBinding(() -> {
            if (tableStatusProperty.getValue() == tableStatus.OFF) return disconnect;
            else if (tableStatusProperty.getValue() == tableStatus.CONNECT) return connect;
            else if (tableStatusProperty.getValue() == tableStatus.ON) return connected;
            else return "";
        }, tableStatusProperty));

        tableConnectButton.textProperty().bind(Bindings.createStringBinding(() -> {
            if (tableStatusProperty.getValue() == tableStatus.ON) return disconnectBtn;
            else return connectBtn;
        }, tableStatusProperty));

        tableConnectButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            tableStatusProperty.getValue() == tableStatus.CONNECT, tableStatusProperty));

        tableIpTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9.]+")) {
                tableIpTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidIp(newValue)) { ;
                tableIpTextField.styleProperty().set(tfError);
            } else {
                tableIpTextField.styleProperty().set(tfOk);
            }
        });

        tablePortTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                tablePortTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidPort(newValue)) {
                tablePortTextField.styleProperty().set(tfError);
            } else {
                tablePortTextField.styleProperty().set(tfOk);
            }
        });

        tableStatusProperty.addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == tableStatus.OFF && tableExecutor != null) {
                    tableExecutor.close();
                }
            } catch (IOException e) {
                logger.debug(e.getMessage());
            }
        });
        //Measurement
        sParamChoiceBox.setItems(FXCollections.observableArrayList(Arrays.stream(measureType.values()).map(Enum::name).collect(Collectors.toList())));
        sParamChoiceBox.setValue(measureType.S11.toString());

        startFreqTextField.textProperty().bindBidirectional(startFreqProperty, strIntConverter);
        stopFreqTextField.textProperty().bindBidirectional(stopFreqProperty, strIntConverter);
        stepFreqTextField.textProperty().bindBidirectional(stepFreqProperty, strIntConverter);
        rbwTextField.textProperty().bindBidirectional(rbwProperty, strIntConverter);
        averageTextField.textProperty().bindBidirectional(averageProperty, strIntConverter);
        smoothTextField.textProperty().bindBidirectional(smoothProperty, strIntConverter);
        startAngleTextField.textProperty().bindBidirectional(startAngleProperty, strIntConverter);
        stopAngleTextField.textProperty().bindBidirectional(stopAngleProperty, strIntConverter);
        stepAngleTextField.textProperty().bindBidirectional(stepAngleProperty, strIntConverter);

        List<Control> measureElements = new ArrayList<>(List.of(sParamChoiceBox, startFreqTextField, stopFreqTextField,
                stepFreqTextField, rbwTextField, averageTextField,
                smoothTextField, startAngleTextField, stopAngleTextField,
                stepAngleTextField));

        measureElements.forEach(e -> e.disableProperty().bind(Bindings.createBooleanBinding(() ->
                programStatusProperty.getValue() == programStatus.WORK ||
                programStatusProperty.getValue() == programStatus.PAUSED, programStatusProperty)));

        startFreqTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                startFreqTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidFreq(newValue)) {
                startFreqTextField.styleProperty().set(tfError);
            } else {
                startFreqTextField.styleProperty().set(tfOk);
            }
        });
        stopFreqTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                stopFreqTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidFreq(newValue)) {
                stopFreqTextField.styleProperty().set(tfError);
            } else {
                stopFreqTextField.styleProperty().set(tfOk);
            }
        });
        stepFreqTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                stepFreqTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidStepFreq(newValue)) {
                stepFreqTextField.styleProperty().set(tfError);
            } else {
                stepFreqTextField.styleProperty().set(tfOk);
            }
        });
        rbwTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                rbwTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidRbw(newValue)) {
                rbwTextField.styleProperty().set(tfError);
            } else {
                rbwTextField.styleProperty().set(tfOk);
            }
        });
        averageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                averageTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidAverage(newValue)) {
                averageTextField.styleProperty().set(tfError);
            } else {
                averageTextField.styleProperty().set(tfOk);
            }
        });
        smoothTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                smoothTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidSmooth(newValue)) {
                smoothTextField.styleProperty().set(tfError);
            } else {
                smoothTextField.styleProperty().set(tfOk);
            }
        });
        startAngleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                startAngleTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidAngle(newValue)) {
                startAngleTextField.styleProperty().set(tfError);
            } else {
                startAngleTextField.styleProperty().set(tfOk);
            }
        });
        stopAngleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                stopAngleTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidAngle(newValue)) {
                stopAngleTextField.styleProperty().set(tfError);
            } else {
                stopAngleTextField.styleProperty().set(tfOk);
            }
        });
        stepAngleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d+")) {
                stepAngleTextField.textProperty().set(oldValue);
            } else if (!InputDataChecker.isValidStepAngle(newValue)) {
                stepAngleTextField.styleProperty().set(tfError);
            } else {
                stepAngleTextField.styleProperty().set(tfOk);
            }
        });

        rotateToZeroButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            tableStatusProperty.getValue() == tableStatus.OFF || tableStatusProperty.getValue() == tableStatus.CONNECT ||
            programStatusProperty.getValue() == programStatus.PAUSED || programStatusProperty.getValue() == programStatus.WORK
                , tableStatusProperty, programStatusProperty));

        directionOfRotationButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            tableStatusProperty.getValue() == tableStatus.OFF || tableStatusProperty.getValue() == tableStatus.CONNECT ||
                programStatusProperty.getValue() == programStatus.PAUSED || programStatusProperty.getValue() == programStatus.WORK ||
                programStatusProperty.getValue() == programStatus.SETZERO, tableStatusProperty, programStatusProperty));

        setZeroButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            tableStatusProperty.getValue() == tableStatus.OFF || tableStatusProperty.getValue() == tableStatus.CONNECT ||
                programStatusProperty.getValue() == programStatus.PAUSED || programStatusProperty.getValue() == programStatus.WORK ||
                programStatusProperty.getValue() == programStatus.SETZERO, tableStatusProperty, programStatusProperty));

        startButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            programStatusProperty.getValue() == programStatus.SETZERO ||
                programStatusProperty.getValue() == programStatus.WORK ||
                programStatusProperty.getValue() == programStatus.OFF, programStatusProperty));

        pauseButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            programStatusProperty.getValue() == programStatus.SETZERO ||
                programStatusProperty.getValue() == programStatus.OFF ||
                programStatusProperty.getValue() == programStatus.READY ||
                programStatusProperty.getValue() == programStatus.PAUSED, programStatusProperty));

        stopButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            programStatusProperty.getValue() == programStatus.SETZERO ||
                programStatusProperty.getValue() == programStatus.READY ||
                    programStatusProperty.getValue() == programStatus.OFF, programStatusProperty));

        rotateToZeroButtonImageView.imageProperty().bind(Bindings.createObjectBinding(() ->
            programStatusProperty.getValue() == programStatus.SETZERO ? rotateToZeroProgressImg : rotateToZeroImg, programStatusProperty));

        directionOfRotationButtonImageView.imageProperty().bind(Bindings.createObjectBinding(() ->
            directionOfRotationProperty.getValue().equals(rotateRight) ? rotateRightImg : rotateLeftImg, directionOfRotationProperty));

        setZeroButtonImageView.setImage(setZeroButtonImg);

        //View
        measureData.addListener((InvalidationListener) change -> {
            if (freqChoiceBox.getItems().isEmpty()) {
                ObservableList<Integer> freq = FXCollections.observableList(
                        Stream.iterate(startFreqProperty.get(), n -> n + stepFreqProperty.get())
                                .limit((stopFreqProperty.get() - startFreqProperty.get()) / stepFreqProperty.get() + 1)
                                .collect(Collectors.toList()));

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        freqChoiceBox.setItems(freq);
                        freqChoiceBox.setValue(freq.get(0));
                    }
                });
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (freqChoiceBox.getSelectionModel().getSelectedIndex() != -1) repaintGraph();
                }
            });
        });

        freqChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
            dataTableView.refresh();
            repaintGraph();
        });

        autoScaleCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> repaintGraph());

        minScaleTextField.setText(String.valueOf(minScale));
        maxScaleTextField.setText(String.valueOf(maxScale));

        minScaleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("^-?\\d*\\.?\\d*")) {
                minScaleTextField.setText(oldValue);
            } else if(newValue.isEmpty() || newValue.matches(".|-") || Double.parseDouble(newValue) >= maxScale) {
                minScaleTextField.styleProperty().set(tfError);
            } else {
                minScaleTextField.styleProperty().set(tfOk);
            }
        });
        maxScaleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("^-?\\d*\\.?\\d*")) {
                maxScaleTextField.setText(oldValue);
            } else if(newValue.isEmpty() || newValue.matches(".|-") || Double.parseDouble(newValue) <= minScale) {
                maxScaleTextField.styleProperty().set(tfError);
            } else {
                maxScaleTextField.styleProperty().set(tfOk);
            }
        });

        minScaleTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().getName().equals("Enter")) {
                    if(minScaleTextField.styleProperty().getValue().equals(tfOk)) {
                        minScale = Double.parseDouble(minScaleTextField.getText());
                        repaintGraph();
                    } else {
                        showPopupTooltip(minScaleTextField, "Введите правильное минимальное значение");
                    }
                }
            }
        });
        maxScaleTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().getName().equals("Enter")) {
                    if(maxScaleTextField.styleProperty().getValue().equals(tfOk)) {
                        maxScale = Double.parseDouble(maxScaleTextField.getText());
                        repaintGraph();
                    } else {
                        showPopupTooltip(maxScaleTextField, "Введите правильное максимальное значение");
                    }
                }
            }
        });

        minScaleTextField.disableProperty().bind(autoScaleCheckBox.selectedProperty());
        maxScaleTextField.disableProperty().bind(autoScaleCheckBox.selectedProperty());

        minScaleTextField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!t1 && minScaleTextField.styleProperty().getValue().equals(tfOk)) {
                minScale = Double.parseDouble(minScaleTextField.getText());
                repaintGraph();
            } else if(!t1 && minScaleTextField.styleProperty().getValue().equals(tfError)) {
                minScaleTextField.setText(String.valueOf(minScale));
            }
        });
        maxScaleTextField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!t1 && maxScaleTextField.styleProperty().getValue().equals(tfOk)) {
                maxScale = Double.parseDouble(maxScaleTextField.getText());
                repaintGraph();
            } else if(!t1 && maxScaleTextField.styleProperty().getValue().equals(tfError)) {
                maxScaleTextField.setText(String.valueOf(maxScale));
            }
        });
    }

    private void showPopupTooltip(Node element, String text) {
        Tooltip t = new Tooltip(text);
        t.setFont(Font.font(14));
        Timeline idlestage = new Timeline(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                t.hide();
            }
        }));
        idlestage.setCycleCount(1);
        Point2D point = element.localToScene(0.0, 0.0);

        t.show(element, point.getX() + element.getScene().getX() + element.getScene().getWindow().getX() + element.getLayoutBounds().getWidth() + 5,
                        point.getY() + element.getScene().getY() + element.getScene().getWindow().getY() - element.getLayoutBounds().getHeight() / 2);
        idlestage.play();
    }

    private void checkPnaConnect() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    pnaExecutor = new Executor(pnaIpTextField.getText(), Integer.parseInt(pnaPortTextField.getText()));
                    pnaExecutor.connect();
                    PnaController pnaController = new PnaController(pnaExecutor);
                    boolean res = pnaController.checkReady();
                    if(!res) throw new IOException("pna not ready");
                } catch (IOException e) {
                    logger.debug(e.getMessage());
                    throw new IOException(e);
                }
                return null;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        task.setOnFailed(event -> {
            pnaStatusProperty.setValue(pnaStatus.OFF);
        });

        task.setOnSucceeded(event -> {
            pnaStatusProperty.setValue(pnaStatus.ON);
        });

        task.setOnCancelled(event -> {
            pnaStatusProperty.setValue(pnaStatus.OFF);
        });

        task.setOnRunning(event -> {
            pnaStatusProperty.setValue(pnaStatus.CONNECT);
        });
    }

    private void checkTableConnect() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
            try {
                tableExecutor = new Executor(tableIpTextField.getText(), Integer.parseInt(tablePortTextField.getText()));
                tableExecutor.connect();
                TableController tableController = new TableController(tableExecutor);
                if(!tableController.checkReady()) throw new IOException("table not ready");
            } catch (IOException e) {
                logger.debug(e.getMessage());
                throw new IOException(e);
            }
            return null;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        task.setOnFailed(event -> {
            tableStatusProperty.setValue(tableStatus.OFF);
        });

        task.setOnSucceeded(event -> {
            tableStatusProperty.setValue(tableStatus.ON);
        });

        task.setOnCancelled(event -> {
            tableStatusProperty.setValue(tableStatus.OFF);
        });

        task.setOnRunning(event -> {
            tableStatusProperty.setValue(tableStatus.CONNECT);
        });
    }

    private void startMeasure(programStatus status) {
        measureTask = new Task() {
            @Override
            protected Object call() throws Exception {
                PnaController pnaController = new PnaController(pnaExecutor);
                TableController tableController = new TableController(tableExecutor);

                if(status == programStatus.READY) {
                    measureData.clear();

                    pnaController.tunePna(sParamChoiceBox.getValue(),
                        startFreqProperty.get(), stopFreqProperty.get(), stepFreqProperty.get(),
                        rbwProperty.get(), averageProperty.get(), smoothProperty.get());

                    intermediateAngle = startAngleProperty.get();

                    tableController.setAngleFast(intermediateAngle); //block thread while rotating

                    if(directionOfRotationProperty.getValue().equals(rotateRight)) tableController.setRightRotation();
                    else tableController.setLeftRotation();
                }

                if(!pnaController.checkReady()) throw new IOException("pna not ready");
                if(!tableController.checkReady()) throw new IOException("table not ready");

                for(; intermediateAngle <= stopAngleProperty.get(); intermediateAngle+=stepAngleProperty.get()) {
                    tableController.setAngle(intermediateAngle);
                    measureData.add(Map.entry(intermediateAngle, pnaController.getDbData()));
                }

                return null;
            }
        };

        Thread th = new Thread(measureTask);
        th.setDaemon(true);
        th.start();

        measureTask.setOnRunning(workerStateEvent -> programStatusProperty.setValue(programStatus.WORK));

        measureTask.setOnSucceeded(workerStateEvent -> programStatusProperty.setValue(programStatus.READY));

        measureTask.setOnCancelled(workerStateEvent -> programStatusProperty.setValue(programStatus.READY));

        measureTask.setOnFailed(workerStateEvent -> {
            programStatusProperty.setValue(programStatus.READY);
            System.out.println(measureTask.getException().getMessage());
        });
    }

    private void tableStop() {
        Task stopTask = new Task() {
            @Override
            protected Object call() throws Exception {
                TableController tableController = new TableController(tableExecutor);
                tableController.stop();
                return null;
            }
        };

        Thread th = new Thread(stopTask);
        th.start();
    }

    private void createEmptyGraph() {
        Plotter plotter = new Plotter();
        Canvas canvas = plotter.getEmptyGraph(minScale, maxScale);

        graphPane.getChildren().add(canvas);
        canvas.widthProperty().bind(graphPane.widthProperty());
        canvas.heightProperty().bind(graphPane.heightProperty());
    }

    private void repaintGraph() {
        Plotter plotter = new Plotter();

        Canvas canvas;
        if(autoScaleCheckBox.isSelected()) {
            minScale = measureData.stream()
                                  .map(e -> e.getValue().get(freqChoiceBox.getSelectionModel().getSelectedIndex()))
                                  .min(Double::compare)
                                  .orElse(minScale);

            maxScale = measureData.stream()
                                  .map(e -> e.getValue().get(freqChoiceBox.getSelectionModel().getSelectedIndex()))
                                  .max(Double::compare)
                                  .orElse(maxScale);

            minScale*=0.9;
            maxScale*=1.1;

            minScaleTextField.setText(String.valueOf(minScale));
            maxScaleTextField.setText(String.valueOf(maxScale));
        }

        canvas = plotter.getGraph(measureData,
                                  freqChoiceBox.getSelectionModel().getSelectedIndex(),
                                  minScale,
                                  maxScale);

        graphPane.getChildren().removeAll();
        graphPane.getChildren().add(canvas);

        canvas.widthProperty().bind(graphPane.widthProperty());
        canvas.heightProperty().bind(graphPane.heightProperty());
    }

    private void createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Сохранить диаграмму");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveGraphToPng();
            }
        });

        MenuItem item2 = new MenuItem("Сохранить данные");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveDataToTxt();
            }
        });

        contextMenu.getItems().addAll(item1, item2);

        graphPane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(graphPane, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void saveGraphToPng() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(null);

        Bounds rectangle = graphPane.getBoundsInLocal();

        if(file != null) {
            try {
                //Pad the capture area
                WritableImage writableImage = new WritableImage((int)rectangle.getWidth(),
                        (int)rectangle.getHeight());

                graphPane.snapshot(null, writableImage);
                //Write the snapshot to the chosen file
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    private void saveDataToTxt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt files (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(null);

        if(file != null) {
            try(PrintWriter out = new PrintWriter(file)) {
                dataTableView.getItems().forEach(item -> out.println(item.getKey() + " " + item.getValue()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private final StringConverter<Number> strIntConverter = new StringConverter<Number>() {
        @Override
        public String toString(Number number) {
            return String.valueOf(number);
        }

        @Override
        public Number fromString(String string) {
            try {
                String number = string.replaceAll("\\D", "");
                return number.isEmpty() ? 0 : Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    };
}
