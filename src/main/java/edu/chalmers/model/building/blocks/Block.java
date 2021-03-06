package edu.chalmers.model.building.blocks;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.time.TimerAction;
import edu.chalmers.model.EntityType;
import edu.chalmers.model.building.IBlock;
import edu.chalmers.model.building.IBlockObservable;
import edu.chalmers.model.building.IMapObserver;
import edu.chalmers.utilities.CoordsCalculations;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.runOnce;

/**
 * @author Malte Åkvist, Sam Salek
 * <p>
 * A class for Blocks that the player can build.
 */
public class Block implements IBlock, IBlockObservable {

    private Entity currentBlock;

    private boolean testing = false; //Boolean used for testing

    private int health = 100;
    private TimerAction damageDelayTimer;

    public Block(Point2D mousePos) {
        Point2D blockPosition = CoordsCalculations.posToTilePos(mousePos);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        physics.setFixtureDef(new FixtureDef().friction(0.0f));
        currentBlock = FXGL.entityBuilder()
                .type(EntityType.BLOCK)
                .at(((int) blockPosition.getX()), ((int) blockPosition.getY()))
                .viewWithBBox(FXGL.getAssetLoader().loadTexture("BuildingBlock.png"))
                .with(physics)
                .with(new CollidableComponent(true))
                .with("this", this)         // Adds a property with value of this class and the key String "this". Can be used to reach Block class when simply working with Entity's.
                .buildAndAttach();

        initDamageDelayTimer();
    }

    /**
     * Method to check if object is destroyable
     *
     * @return boolean
     */
    @Override
    public boolean canBeDestroyed() {
        return true;
    }

    /**
     * Remove entity from world
     */
    @Override
    public void remove() {
        if (canBeDestroyed()) {
            FXGL.getGameWorld().removeEntity(currentBlock);
        }
    }

    /**
     * Lower Block health with damage.
     *
     * @param damage Amount of health points to be inflicted to the Block.
     */
    public void inflictDamage(int damage) {
        if(!testing) {
            if (damageDelayTimer.isExpired()) {
                int damageDelayMilliseconds = 500;   // How often it can be inflicted damage
                damageDelayTimer = runOnce(() -> health -= damage, Duration.millis(damageDelayMilliseconds));
            }
        }
        else {
            health-= damage;
        }

        checkHealth();
    }

    /**
     * Getter for Blocks health.
     *
     * @return int of Blocks health.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Notifies observers of this blocks position
     */
    @Override
    public void notifyObservers() {
        for (IMapObserver IMapObserver : IMapObservers) {
            IMapObserver.update(CoordsCalculations.posToTile(currentBlock.getPosition()));
        }
    }

    /**
     * Adds an observer to the mapObserver list
     *
     * @param IMapObserver a mapObserver
     */
    @Override
    public void addObserver(IMapObserver IMapObserver) {
        IMapObservers.add(IMapObserver);
    }

    //Method checks the block's health and takes appropriate course of action based on health left.
    private void checkHealth() {
        // Remove block if its health becomes 0 or lower
        if (health <= 0) {
            destroyAndNotify();
        } else if (health <= 30) {
            changeTexture("BuildingBlockDamageStage2.png");
        } else if (health <= 70) {
            changeTexture("BuildingBlockDamageStage1.png");
        }
    }

    //Method removes block and notifies its observers.
    private void destroyAndNotify() {
        FXGL.getGameWorld().removeEntity(currentBlock);
        notifyObservers();
    }

    //Method changes the block's texture to a texture file with the given name.
    //@param textureName Name of texture file
    private void changeTexture(String textureName) {
        currentBlock.getViewComponent().clearChildren();
        currentBlock.getViewComponent().addChild(FXGL.getAssetLoader().loadTexture(textureName));
    }

    //Method initiates damage delay timer.
    private void initDamageDelayTimer() {
        damageDelayTimer = runOnce(() -> {
        }, Duration.seconds(0));
    }

    /**
     * Setter for testing variable used to test time based methods.
     *
     * @param state True or False.
     */
    public void setTesting(boolean state) {
        testing = state;
    }
}
