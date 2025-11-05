import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceStationGUI extends Application {

    private VBox logArea;
    private ScrollPane logScrollPane;
    private GridPane pumpsGrid;
    private HBox waitingQueueBox;
    private Label waitingCountLabel;
    private Label carsServedLabel;
    private Map<Integer, PumpVisual> pumpVisuals = new ConcurrentHashMap<>();
    private Map<String, CarVisual> carVisuals = new ConcurrentHashMap<>();
    private int carsServed = 0;
    private boolean simulationRunning = false;
    private List<Thread> activeThreads = new ArrayList<>();
    private Button startBtn;
    private Spinner<Integer> waitingSpinner;
    private Spinner<Integer> pumpsSpinner;
    private TextField carsField;
    private Spinner<Double> speedSpinner;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Service Station Simulation");

        // Main container with clean dark background
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Top bar with title and stats
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center content
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        // Configuration panel
        VBox configPanel = createConfigPanel(primaryStage);

        // Pumps area
        VBox pumpsContainer = createPumpsContainer();

        // Waiting queue area
        VBox queueContainer = createQueueContainer();

        // Log area
        VBox logContainer = createLogContainer();

        centerContent.getChildren().addAll(configPanel, pumpsContainer, queueContainer, logContainer);

        ScrollPane mainScrollPane = new ScrollPane(centerContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(mainScrollPane);

        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.TRANSPARENT);
        try {
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        } catch (Exception e) {

        }

        primaryStage.setScene(scene);

        // Premium fade-in animation when window opens
        root.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(30);
        topBar.setPadding(new Insets(20, 32, 20, 32));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        // Clean title
        Label titleLabel = new Label("Service Station");
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 20));
        titleLabel.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Minimal stats badge
        HBox statsBox = new HBox(10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getStyleClass().add("stats-box");

        Label statsIcon = new Label("✓");
        statsIcon.setFont(Font.font(13));
        statsIcon.getStyleClass().add("stats-icon");

        carsServedLabel = new Label("0 served");
        carsServedLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        carsServedLabel.getStyleClass().add("stats-label");

        statsBox.getChildren().addAll(statsIcon, carsServedLabel);
        topBar.getChildren().addAll(titleLabel, spacer, statsBox);
        return topBar;
    }

    private VBox createConfigPanel(Stage stage) {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(28));
        container.getStyleClass().add("config-panel");
        container.setMaxWidth(1000);

        Label configTitle = new Label("Configuration");
        configTitle.setFont(Font.font("System", FontWeight.SEMI_BOLD, 18));
        configTitle.getStyleClass().add("config-title");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // Waiting area capacity (Queue size: 1-10 per assignment requirements)
        Label waitingLabel = new Label("Waiting Capacity (1-10)");
        waitingLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        waitingLabel.getStyleClass().add("input-label");
        this.waitingSpinner = new Spinner<>(1, 10, 5);
        this.waitingSpinner.setEditable(true);
        this.waitingSpinner.setPrefWidth(100);
        styleSpinner(this.waitingSpinner);
        
        // Enforce value constraints
        this.waitingSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal < 1) this.waitingSpinner.getValueFactory().setValue(1);
            if (newVal > 10) this.waitingSpinner.getValueFactory().setValue(10);
        });

        // Number of pumps (Service Bays: 1-10 per assignment requirements)
        Label pumpsLabel = new Label("Service Bays (1-10)");
        pumpsLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        pumpsLabel.getStyleClass().add("input-label");
        this.pumpsSpinner = new Spinner<>(1, 10, 3);
        this.pumpsSpinner.setEditable(true);
        this.pumpsSpinner.setPrefWidth(100);
        styleSpinner(this.pumpsSpinner);
        
        // Enforce value constraints
        this.pumpsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal < 1) this.pumpsSpinner.getValueFactory().setValue(1);
            if (newVal > 10) this.pumpsSpinner.getValueFactory().setValue(10);
        });

        // Car IDs
        Label carsLabel = new Label("Car IDs");
        carsLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        carsLabel.getStyleClass().add("input-label");
        this.carsField = new TextField("C1 C2 C3 C4 C5 C6 C7 C8");
        this.carsField.setPrefWidth(400);
        this.carsField.getStyleClass().add("cars-field");

        grid.add(waitingLabel, 0, 0);
        grid.add(this.waitingSpinner, 1, 0);
        grid.add(pumpsLabel, 2, 0);
        grid.add(this.pumpsSpinner, 3, 0);
        grid.add(carsLabel, 0, 1);
        grid.add(this.carsField, 1, 1, 3, 1);

        // Speed Control
        Label speedLabel = new Label("Simulation Speed (seconds):");
        speedLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        speedSpinner = new Spinner<>();
        SpinnerValueFactory<Double> speedFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 20.0, 1.0, 0.1);
        speedSpinner.setValueFactory(speedFactory);
        speedSpinner.setPrefWidth(100);
        speedSpinner.setEditable(true);
        styleSpinner(speedSpinner);
        
        grid.add(speedLabel, 0, 2);
        grid.add(speedSpinner, 1, 2);

        startBtn = new Button("Start");
        startBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        startBtn.getStyleClass().add("start-button");
        addHoverEffect(startBtn);

        Button resetBtn = new Button("Reset");
        resetBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        resetBtn.getStyleClass().add("reset-button");
        addHoverEffect(resetBtn);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startBtn, resetBtn);

        startBtn.setOnAction(e -> {
            if (!simulationRunning) {
                startSimulation(
                    this.waitingSpinner.getValue(),
                    this.pumpsSpinner.getValue(),
                    this.carsField.getText().trim().split("[,\\s]+")
                );
                startBtn.setDisable(true);
                simulationRunning = true;
            }
        });

        resetBtn.setOnAction(e -> {
            resetSimulation();
            startBtn.setDisable(false);
            simulationRunning = false;
        });

        container.getChildren().addAll(configTitle, grid, buttonBox);
        return container;
    }

    private VBox createPumpsContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(28));
        container.getStyleClass().add("pumps-container");
        container.setMaxWidth(1250);

        Label title = new Label("Service Bays");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 18));
        title.getStyleClass().add("section-title");

        pumpsGrid = new GridPane();
        pumpsGrid.setHgap(18);
        pumpsGrid.setVgap(18);
        pumpsGrid.setAlignment(Pos.CENTER);
        pumpsGrid.setPadding(new Insets(10));

        container.getChildren().addAll(title, pumpsGrid);
        return container;
    }

    private VBox createQueueContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(28));
        container.getStyleClass().add("queue-container");
        container.setMaxWidth(1250);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);

        Label title = new Label("Waiting Queue");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 18));
        title.getStyleClass().add("section-title");

        waitingCountLabel = new Label("0 waiting");
        waitingCountLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        waitingCountLabel.getStyleClass().add("waiting-count-label");

        titleBox.getChildren().addAll(title, waitingCountLabel);

        waitingQueueBox = new HBox(15);
        waitingQueueBox.setAlignment(Pos.CENTER);
        waitingQueueBox.setPadding(new Insets(20));
        waitingQueueBox.setMinHeight(120);

        ScrollPane scrollPane = new ScrollPane(waitingQueueBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setMaxWidth(1100);

        container.getChildren().addAll(titleBox, scrollPane);
        return container;
    }

    private VBox createLogContainer() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(28));
        container.getStyleClass().add("log-container");
        container.setMaxWidth(1250);

        Label title = new Label("Activity Log");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 18));
        title.getStyleClass().add("section-title");

        logArea = new VBox(4);
        logArea.setPadding(new Insets(16));
        logArea.getStyleClass().add("log-area");

        logScrollPane = new ScrollPane(logArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setPrefHeight(260);
        logScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        container.getChildren().addAll(title, logScrollPane);
        return container;
    }

    private void startSimulation(int waitingArea, int pumpNums, String[] carIDs) {
        // Stop any existing simulation
        for (Thread thread : activeThreads) {
            thread.interrupt();
        }
        activeThreads.clear();

        logArea.getChildren().clear();
        pumpsGrid.getChildren().clear();
        waitingQueueBox.getChildren().clear();
        pumpVisuals.clear();
        carVisuals.clear();
        carsServed = 0;
        updateCarsServed();

        // Create pump visuals
        int columns = Math.min(4, pumpNums);
        for (int i = 0; i < pumpNums; i++) {
            PumpVisual pumpVisual = new PumpVisual(i + 1);
            pumpVisuals.put(i + 1, pumpVisual);
            pumpsGrid.add(pumpVisual.getNode(), i % columns, i / columns);
        }

        // Get speed value from spinner
        final double speed = speedSpinner.getValue();

        // Start simulation in background thread
        Thread mainThread = new Thread(() -> {
            Queue<Car> waitingQueue = new LinkedList<>();

            Semaphore mutex = new Semaphore(1);
            Semaphore empty = new Semaphore(waitingArea);
            Semaphore full = new Semaphore(0);
            Semaphore pumps = new Semaphore(pumpNums);

            // Start pump threads
            for (int i = 1; i <= pumpNums; i++) {
                Pump pump = new Pump(i, waitingQueue, mutex, empty, full, pumps, this, speed);
                pump.start();
                activeThreads.add(pump);
            }

            // Start car threads with delays
            for (String id : carIDs) {
                if (Thread.currentThread().isInterrupted() || !simulationRunning) {
                    break;
                }
                Car car = new Car(id, waitingQueue, mutex, empty, full, this, speed);
                car.start();
                activeThreads.add(car);

                try {
                    Thread.sleep((long)(1000 * speed));
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        activeThreads.add(mainThread);
        mainThread.start();
    }

    private void resetSimulation() {
        // Stop all running threads
        simulationRunning = false;
        
        // Interrupt all threads
        for (Thread thread : activeThreads) {
            thread.interrupt();
        }
        
        // Give threads a moment to finish their current operations
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        activeThreads.clear();

        // Reset UI state (don't use Platform.runLater since we're already on JavaFX thread from button click)
        logArea.getChildren().clear();
        pumpsGrid.getChildren().clear();
        waitingQueueBox.getChildren().clear();
        pumpVisuals.clear();
        carVisuals.clear();
        carsServed = 0;
        updateCarsServed();
        updateWaitingCount();
    }

    public void addLog(String message) {
        // Don't add logs if simulation is not running
        if (!simulationRunning) {
            return;
        }
        
        Platform.runLater(() -> {
            // Double check in case reset happened during Platform.runLater delay
            if (!simulationRunning) {
                return;
            }
            
            Label logLabel = new Label("• " + message);
            logLabel.setFont(Font.font("Consolas", 13));

            // Color-coded based on message type
            if (message.contains("arrived") || message.contains("entered")) {
                logLabel.getStyleClass().add("log-entry-arrival");
            } else if (message.contains("begins service")) {
                logLabel.getStyleClass().add("log-entry-service");
            } else if (message.contains("finished service")) {
                logLabel.getStyleClass().add("log-entry-completed");
            } else {
                logLabel.getStyleClass().add("log-entry");
            }

            // Subtle fade in animation
            logLabel.setOpacity(0);

            FadeTransition fade = new FadeTransition(Duration.millis(300), logLabel);
            fade.setFromValue(0);
            fade.setToValue(0.9);
            fade.play();

            logArea.getChildren().add(logLabel);
            logScrollPane.setVvalue(1.0);
        });
    }

    public void carArrived(String carID) {
        Platform.runLater(() -> {
            CarVisual carVisual = new CarVisual(carID);
            carVisuals.put(carID, carVisual);
            waitingQueueBox.getChildren().add(carVisual.getNode());
            updateWaitingCount();

            // Enhanced entrance animation with bounce
            carVisual.getNode().setScaleX(0);
            carVisual.getNode().setScaleY(0);
            carVisual.getNode().setOpacity(0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), carVisual.getNode());
            scale.setFromX(0);
            scale.setFromY(0);
            scale.setToX(1);
            scale.setToY(1);
            scale.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(400), carVisual.getNode());
            fade.setFromValue(0);
            fade.setToValue(1);

            RotateTransition rotate = new RotateTransition(Duration.millis(400), carVisual.getNode());
            rotate.setFromAngle(-15);
            rotate.setToAngle(0);

            ParallelTransition parallel = new ParallelTransition(scale, fade, rotate);
            parallel.play();
        });
    }

    public void carStartsService(String carID, int pumpID) {
        Platform.runLater(() -> {
            CarVisual carVisual = carVisuals.get(carID);
            if (carVisual != null) {
                waitingQueueBox.getChildren().remove(carVisual.getNode());
                updateWaitingCount();
            }

            PumpVisual pumpVisual = pumpVisuals.get(pumpID);
            if (pumpVisual != null) {
                pumpVisual.startService(carID);
            }
        });
    }

    public void carFinishesService(int pumpID) {
        Platform.runLater(() -> {
            PumpVisual pumpVisual = pumpVisuals.get(pumpID);
            if (pumpVisual != null) {
                pumpVisual.finishService();
            }
            carsServed++;
            updateCarsServed();
        });
    }

    private void updateWaitingCount() {
        int count = waitingQueueBox.getChildren().size();
        waitingCountLabel.setText(count + " waiting");
    }

    private void updateCarsServed() {
        carsServedLabel.setText(carsServed + " served");

        // Subtle scale animation on update
        ScaleTransition st = new ScaleTransition(Duration.millis(200), carsServedLabel.getParent());
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.08);
        st.setToY(1.08);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void styleSpinner(Spinner<?> spinner) {
        spinner.getStyleClass().add("custom-spinner");
        spinner.getEditor().getStyleClass().add("spinner-editor");
    }

    private void addHoverEffect(Button button) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#0078D4", 0.6));
        glow.setRadius(20);
        glow.setSpread(0.5);

        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();

            button.setEffect(glow);
        });

        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();

            DropShadow normalShadow = new DropShadow();
            normalShadow.setColor(Color.web("#0078D4", 0.4));
            normalShadow.setRadius(12);
            normalShadow.setSpread(0.4);
            button.setEffect(normalShadow);
        });
    }

    // Inner class for visual representation of pumps
    class PumpVisual {
        private VBox container;
        private Label statusLabel;
        private Label carLabel;
        private Circle statusIndicator;
        private int pumpID;

        public PumpVisual(int id) {
            this.pumpID = id;
            container = new VBox(14);
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(22));
            container.getStyleClass().add("pump-visual");
            container.setPrefSize(180, 160);

            Label pumpLabel = new Label("Bay " + id);
            pumpLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
            pumpLabel.getStyleClass().add("pump-label");

            statusIndicator = new Circle(7);
            statusIndicator.setFill(Color.web("#22c55e"));
            DropShadow indicatorGlow = new DropShadow();
            indicatorGlow.setColor(Color.web("#22c55e", 0.5));
            indicatorGlow.setRadius(8);
            indicatorGlow.setSpread(0.4);
            statusIndicator.setEffect(indicatorGlow);

            statusLabel = new Label("Available");
            statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
            statusLabel.getStyleClass().add("pump-status-available");

            carLabel = new Label("");
            carLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));
            carLabel.getStyleClass().add("pump-car-label");
            
            HBox statusBox = new HBox(8);
            statusBox.setAlignment(Pos.CENTER);
            statusBox.getChildren().addAll(statusIndicator, statusLabel);
            
            container.getChildren().addAll(pumpLabel, statusBox, carLabel);
        }
        
        public VBox getNode() {
            return container;
        }
        
        public void startService(String carID) {
            statusIndicator.setFill(Color.web("#fb923c"));
            statusLabel.setText("Busy");
            statusLabel.getStyleClass().remove("pump-status-available");
            statusLabel.getStyleClass().add("pump-status-busy");
            carLabel.setText(carID);

            // Update container border for busy state
            container.getStyleClass().remove("pump-visual");
            container.getStyleClass().add("pump-visual-busy");
            
            // Subtle pulse animation with glow
            DropShadow busyGlow = new DropShadow();
            busyGlow.setColor(Color.web("#fb923c", 0.6));
            busyGlow.setRadius(10);
            busyGlow.setSpread(0.4);
            statusIndicator.setEffect(busyGlow);
            
            ScaleTransition pulse = new ScaleTransition(Duration.millis(800), statusIndicator);
            pulse.setFromX(1);
            pulse.setFromY(1);
            pulse.setToX(1.3);
            pulse.setToY(1.3);
            pulse.setCycleCount(Timeline.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.setInterpolator(Interpolator.EASE_BOTH);
            pulse.play();
        }
        
        public void finishService() {
            statusIndicator.setFill(Color.web("#22c55e"));
            statusLabel.setText("Available");
            statusLabel.getStyleClass().remove("pump-status-busy");
            statusLabel.getStyleClass().add("pump-status-available");
            carLabel.setText("");

            // Restore available state styling
            container.getStyleClass().remove("pump-visual-busy");
            container.getStyleClass().add("pump-visual");
            
            // Restore available glow
            DropShadow availableGlow = new DropShadow();
            availableGlow.setColor(Color.web("#22c55e", 0.5));
            availableGlow.setRadius(8);
            availableGlow.setSpread(0.4);
            statusIndicator.setEffect(availableGlow);
            
            // Subtle flash animation
            FadeTransition flash = new FadeTransition(Duration.millis(200), container);
            flash.setFromValue(1.0);
            flash.setToValue(0.5);
            flash.setCycleCount(2);
            flash.setAutoReverse(true);
            
            ScaleTransition bounce = new ScaleTransition(Duration.millis(200), container);
            bounce.setFromX(1.0);
            bounce.setFromY(1.0);
            bounce.setToX(1.04);
            bounce.setToY(1.04);
            bounce.setCycleCount(2);
            bounce.setAutoReverse(true);
            bounce.setInterpolator(Interpolator.EASE_BOTH);
            
            ParallelTransition parallel = new ParallelTransition(flash, bounce);
            parallel.play();
        }
    }
    
    // Inner class for visual representation of cars in queue
    class CarVisual {
        private VBox container;
        
        public CarVisual(String carID) {
            container = new VBox(6);
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(16));
            container.getStyleClass().add("car-visual");

            Label carIcon = new Label("🚗");
            carIcon.setFont(Font.font(22));

            Label idLabel = new Label(carID);
            idLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
            idLabel.getStyleClass().add("car-id-label");

            container.getChildren().addAll(carIcon, idLabel);

            // Subtle hover effect
            container.setOnMouseEntered(e -> {
                container.getStyleClass().remove("car-visual");
                container.getStyleClass().add("car-visual-hover");
            });

            container.setOnMouseExited(e -> {
                container.getStyleClass().remove("car-visual-hover");
                container.getStyleClass().add("car-visual");
            });
        }
        
        public VBox getNode() {
            return container;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// Semaphore class
class Semaphore {
    private int value;
    
    public Semaphore(int Value) {
        value = Value;
    }
    
    public synchronized void P(){
        value--;
        if(value < 0){
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }
    
    public synchronized void V(){
        value++;
        if (value <= 0){
            notify();
        }
    }
}

// Car class
class Car extends Thread {
    private String carID;
    private Queue<Car> waitingQueue;
    private Semaphore mutex;
    private Semaphore empty;
    private Semaphore full;
    private ServiceStationGUI gui;
    private double speed;
    
    public Car(String id, Queue<Car> queue, Semaphore m, Semaphore e, Semaphore f, ServiceStationGUI gui, double speed) {
        carID = id;
        waitingQueue = queue;
        mutex = m;
        empty = e;
        full = f;
        this.gui = gui;
        this.speed = speed;
    }
    
    public String getCarID() {
        return carID;
    }
    
    @Override
    public void run() {
        gui.addLog(carID + " arrived");
        empty.P();
        mutex.P();
        waitingQueue.add(this);
        gui.addLog(carID + " arrived and waiting");
        gui.carArrived(carID);
        mutex.V();
        full.V();
        
        // Small delay to show car in queue visually
        try {
            Thread.sleep((long)(500 * speed));
        } catch (InterruptedException e) {}
    }
}

// Pump class
class Pump extends Thread {
    private int pumpID;
    private Queue<Car> waitingQueue;
    private Semaphore mutex;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore pumps;
    private ServiceStationGUI gui;
    private double speed;
    
    public Pump(int id, Queue<Car> queue, Semaphore m, Semaphore e, Semaphore f, Semaphore p, ServiceStationGUI gui, double speed) {
        pumpID = id;
        waitingQueue = queue;
        mutex = m;
        empty = e;
        full = f;
        pumps = p;
        this.gui = gui;
        this.speed = speed;
    }
    
    @Override
    public void run() {
        while (true) {
            full.P();
            pumps.P();
            
            // Small delay to let queue build up visually
            try {
                Thread.sleep((long)(800 * speed));
            } catch (InterruptedException e) {
                return;
            }
            
            mutex.P();
            
            Car car = waitingQueue.poll();
            if (car != null) {
                gui.addLog("Pump " + pumpID + ": " + car.getCarID() + " Occupied");
                gui.addLog("Pump " + pumpID + ": " + car.getCarID() + " login");
                gui.addLog("Pump " + pumpID + ": " + car.getCarID() + " begins service at Bay " + pumpID);
                gui.carStartsService(car.getCarID(), pumpID);
            }
            
            mutex.V();
            empty.V();
            
            try {
                Thread.sleep((long)(3000 * speed));
            } catch (InterruptedException e) {
                return;
            }
            
            if (car != null) {
                gui.addLog("Pump " + pumpID + ": " + car.getCarID() + " finishes service");
                gui.addLog("Pump " + pumpID + ": Bay " + pumpID + " is now free");
                gui.carFinishesService(pumpID);
            }
            
            pumps.V();
        }
    }
}
