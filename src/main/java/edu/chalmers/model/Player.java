package edu.chalmers.model;

import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player extends Entity {

    private PhysicsComponent physics = new PhysicsComponent();
    private EntityBuilder builder = new EntityBuilder();

    public Player(double x, double y) {
        physics.setBodyType(BodyType.DYNAMIC);
        Entity player = builder.at(x,y).viewWithBBox(new Rectangle(50, 50, Color.BLUE)).with(physics).buildAndAttach();

    }

    /**
     * Method moves players Entity left (negative x).
     */
    public void moveLeft(){
        physics.setVelocityX(-150);
    }

    /**
     * Method moves players Entity right (positive x).
     */
    public void moveRight(){
        physics.setVelocityX(150);
    }

    /**
     * Method moves players Entity up (negative y).
     */
    public void jump(){
        physics.setVelocityY(-300);
    }

    /**
     * Method stop players Entity in the x direction.
     */
    public void stop(){
        physics.setVelocityX(0);
    }
}
