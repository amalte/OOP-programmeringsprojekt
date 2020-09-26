package edu.chalmers.model.enemy.enemytypes;

import javafx.scene.paint.Color;

public interface IEnemyType {
    Color getColor();
    int getHealth();
    int getDamage();
    int getMoveSpeed();
    int getJumpHeight();
}
