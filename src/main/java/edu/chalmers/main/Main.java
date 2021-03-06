package edu.chalmers.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import edu.chalmers.controller.BuildUIController;
import edu.chalmers.controller.GameMenuType;
import edu.chalmers.controller.InputController;
import edu.chalmers.controller.MenuController;
import edu.chalmers.controller.game.ExitMenuController;
import edu.chalmers.controller.game.GameOverViewController;
import edu.chalmers.controller.main.MainMenuController;
import edu.chalmers.controller.main.PlayMenuController;
import edu.chalmers.controller.main.SettingsMenuController;
import edu.chalmers.model.GenericPlatformer;
import edu.chalmers.model.IObserver;
import edu.chalmers.utilities.Constants;
import edu.chalmers.view.game.BuildView;
import edu.chalmers.view.game.ExitMenu;
import edu.chalmers.view.game.GameOverView;
import edu.chalmers.view.game.GameUI;
import edu.chalmers.view.main.MainMenu;
import edu.chalmers.view.main.PlayMenu;
import edu.chalmers.view.main.SettingsMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Anwarr Shiervani
 * <p>
 * The entrypoint for this game.
 */
public class Main extends GameApplication {
    private static AtomicReference<CountDownLatch> initializedLatch = new AtomicReference<>();

    private List<MenuController> controllerList = new ArrayList<>();
    private AnchorPane backgroundPane;
    private GenericPlatformer game;
    private BuildUIController buildUIController;
    private InputController inputController;
    private BuildView buildView;
    private GameUI gameUI;
    private String currentLevel = "";
    private Boolean gameRunning = false;
    private Boolean gameShutdown = false;
    private Boolean testRunning = false;
    private AtomicReference<CountDownLatch> gameRunningLatch = new AtomicReference<>();

    /**
     * Main method. Called when running the program.
     *
     * @param args Arguments to be passed onto FXGL.
     */
    public static void main(String[] args) {
        System.setProperty("quantum.multithreaded", "false"); // DO NOT REMOVE. Caps FPS at 60 across all computers
        launch(args);
    }

    /**
     * @return The initializedLatch for Main.
     */
    public static CountDownLatch getInitializedLatch() {
        return initializedLatch.get();
    }

    /**
     * Set the initializedLatch for Main.
     *
     * @param countDownLatch The instance of CountDownLatch to set initializedLatch to. This latch will be counted down, if its count is over 0, once that the initUI method has been ran.
     */
    public static void setInitializedLatch(CountDownLatch countDownLatch) {
        initializedLatch.set(countDownLatch);
    }

    /**
     * Set good defaults for our game.
     *
     * @param gameSettings A parameter to be specified by FXGL itself. Contains a reference to the an instance of the GameSettings class.
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setPreserveResizeRatio(true);
        gameSettings.setManualResizeEnabled(false);
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setWidth(Constants.GAME_WIDTH);
        gameSettings.setHeight(Constants.GAME_HEIGHT);
        gameSettings.setTitle("Generic Platformer");
        gameSettings.setVersion("1.0");
        gameSettings.setMenuKey(KeyCode.PAUSE);
    }

    /**
     * Initialize our game. Player input, loading levels, etc.
     */
    @Override
    protected void initGame() {
        game = new GenericPlatformer();
        inputController = new InputController(game, this);
        inputController.initPlayerInput();

        this.createBackground();
        this.showBackground();
    }

    /**
     * Initialize the UI of our game.
     */
    @Override
    protected void initUI() {
        runOnce(() -> {
            synchronized (this.controllerList) {
                if (this.controllerList.isEmpty()) {
                    MainMenuController mainMenuController = new MainMenuController(new MainMenu(), this);

                    SettingsMenuController settingsMenuController = new SettingsMenuController(new SettingsMenu(), this);
                    settingsMenuController.setMainMenuController(mainMenuController);

                    PlayMenuController playMenuController = new PlayMenuController(new PlayMenu(), this);
                    playMenuController.setMainMenuController(mainMenuController);

                    mainMenuController.setPlayMenuController(playMenuController);
                    mainMenuController.setSettingsMenuController(settingsMenuController);

                    GameOverViewController gameOverViewController = new GameOverViewController(new GameOverView(), this, game);

                    ExitMenuController exitMenuController = new ExitMenuController(new ExitMenu(), this);
                    exitMenuController.setInputController(inputController);

                    this.controllerList.add(mainMenuController);
                    this.controllerList.add(settingsMenuController);
                    this.controllerList.add(playMenuController);
                    this.controllerList.add(exitMenuController);
                    this.controllerList.add(gameOverViewController);

                    mainMenuController.show();
                }
            }

            if (getInitializedLatch() != null && getInitializedLatch().getCount() > 0)
                getInitializedLatch().countDown();
        }, Duration.seconds(0.5));
    }

    /**
     * Runs update method that runs every tick
     *
     * @param tpf tpf
     */
    @Override
    protected void onUpdate(double tpf) {
        if (buildUIController != null)
            buildUIController.updateBuildTileUI();   // Constantly update the build UI overlay
    }

    /**
     * Shuts the game down.
     */
    public void shutdown() {
        this.stopGame();

        if (!this.testRunning) {
            this.gameShutdown = true;
            getGameController().exit();
        }
    }

    private void createBackground() {
        if (this.backgroundPane == null) {
            this.backgroundPane = new AnchorPane();
            this.backgroundPane.setLayoutX(0);
            this.backgroundPane.setLayoutY(0);
            this.backgroundPane.setPrefSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
            this.backgroundPane.setStyle("-fx-background-color: #000000;");
        }
    }

    private void showBackground() {
        if (!getGameScene().getUiNodes().contains(this.backgroundPane)) {
            getGameScene().addUINode(this.backgroundPane);
        }
    }

    /**
     * Get a registered controller that has the same specified game menu type.
     *
     * @param gameMenuType The game menu type to search for.
     * @return The controller with the game menu type. May be null, if not found.
     */
    public MenuController getController(GameMenuType gameMenuType) {
        for (MenuController menuController : controllerList) {
            if (menuController.getGameMenuType() == gameMenuType) {
                return menuController;
            }
        }

        return null;
    }

    /**
     * Start the game.
     *
     * @param levelIndex What level index to use when loading the TMX file. Format: "level{levelIndex}.tmx"
     */
    public void startGame(int levelIndex) {
        if (!this.getGameRunning()) {
            String levelName = "level" + levelIndex + ".tmx";

            game.remove();
            game.initializeGame(levelName);

            this.currentLevel = levelName;

            runOnce(() -> {
                getGameScene().clearUINodes();
                this.initExtraViews();

                buildUIController = new BuildUIController(game, buildView);
                game.getWaveManager().addObserver((IObserver) getController(GameMenuType.GameOver));

                this.gameRunning = true;

                if (getGameRunningLatch() != null && getGameRunningLatch().getCount() > 0)
                    getGameRunningLatch().countDown();
            }, Duration.seconds(0.5));
        }
    }

    /**
     * Stop the game, if it is running.
     */
    public void stopGame() {
        if (this.getGameRunning()) {
            game.getWaveManager().removeObserver(gameUI);
            game.getWaveManager().removeObserver((IObserver) getController(GameMenuType.GameOver));
            this.showBackground();
            getController(GameMenuType.Exit).hide();
            getController(GameMenuType.Main).show();
            this.gameRunning = false;
        }
    }

    /**
     * @return Whether or not the game has shutdown.
     */
    public Boolean getGameShutdown() {
        return this.gameShutdown;
    }

    /**
     * @return Whether or not the game is running.
     */
    public Boolean getGameRunning() {
        return this.gameRunning;
    }

    private void initExtraViews() {
        this.gameUI = new GameUI(game);
        this.gameUI.createNodes();
        game.getPlayerComponent().addObserver(gameUI);

        this.buildView = new BuildView(game.getPlayerComponent().getBuildRangeTiles());
    }

    /**
     * @return The instance of the GameUI class associated with our Main class.
     */
    public GameUI getGameUI() {
        return this.gameUI;
    }

    /**
     * @return The instance of the InputController class associated with our Main class.
     */
    public InputController getInputController() {
        return this.inputController;
    }

    /**
     * @return The current, loaded level. Format: level(num).tmx
     */
    public String getCurrentLevel() {
        return this.currentLevel;
    }

    /**
     * @return The gameRunningLatch for Main.
     */
    public CountDownLatch getGameRunningLatch() {
        return this.gameRunningLatch.get();
    }

    /**
     * Set the gameRunningLatch for Main.
     *
     * @param gameRunningLatch The instance of CountDownLatch to set gameRunningLatch to. This latch will be counted down, if its count is over 0, once that gameRunning has been set to true.
     */
    public void setGameRunningLatch(CountDownLatch gameRunningLatch) {
        this.gameRunningLatch.set(gameRunningLatch);
    }

    /**
     * Set whether or not a unit is running for the current session.
     *
     * @param testRunning Whether or not a unit are currently running.
     */
    public void setTestRunning(Boolean testRunning) {
        this.testRunning = testRunning;
    }
}