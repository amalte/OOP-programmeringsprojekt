package edu.chalmers.model;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import edu.chalmers.model.building.BuildManager;
import edu.chalmers.model.building.MapManager;
import edu.chalmers.model.wave.WaveManager;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;


/**
 * Aggregate root class for the game.
 */
public class GenericPlatformer {

    private Entity player;
    private GameWorldFactory gameWorldFactory;
    private WaveManager waveManager;
    private MapManager mapManager;
    private BuildManager buildManager;
    private CollisionDetection collisionDetection;

    public GenericPlatformer() {
        this.gameWorldFactory = new GameWorldFactory();
        this.collisionDetection = new CollisionDetection();

        initBuildManager();
    }



    /**
     * Get method that creates a new player if no player is already created.
     * @return A player object.
     */
    public Entity getPlayer(){
        if(player == null){
            createPlayer();
        }
        return player;
    }

    /**
     * Initiates mapManager.
     */
    //ToDo Malte fix
    public void initMapManager(){
        this.buildManager = new BuildManager(player.getComponent(PlayerComponent.class).getBuildRangeTiles());
    }

    /**
     * Initiates buildManager.
     */
    //ToDo Malte fix
    public void initBuildManager(){
        //this.buildManager = new BuildManager(getPlayer().getComponent(PlayerComponent.class).getBuildRangeTiles());
        this.buildManager = new BuildManager(player.getComponent(PlayerComponent.class).getBuildRangeTiles());
    }

    /**
     * Get method for buildManager.
     * @return buildManager.
     */
    public BuildManager getBuildManager(){
        return buildManager;
    }

    /**
     * Initiates waveManger.
     */
    public void initWaveManager(){
        this.waveManager = new WaveManager(getPlayer());
    }

    /**
     * Get method for gameWorldFactory.
     * @return gameWorldFactory.
     */
    public GameWorldFactory getGameWorldFactory(){
        return gameWorldFactory;
    }

    /**
     * Creates a player at position 0,0.
     */
    private void createPlayer(){
        Point2D spawnPoint = getGameWorld().getEntitiesByType(EntityType.PLAYERSPAWNPOINT).get(0).getPosition();
        player = spawn("player", spawnPoint.getX(), spawnPoint.getY());
    }

    /**
     * Get method for waveManager.
     * @return waveManager.
     */
    public WaveManager getWaveManager(){
        return waveManager;
    }

    /**
     * Initiates collision detections for the game worlds entities.
     */
    public void initCollisionDetection(){
        collisionDetection.initCollisionHandler(getPlayer().getComponent(PlayerComponent.class));
    }
}
