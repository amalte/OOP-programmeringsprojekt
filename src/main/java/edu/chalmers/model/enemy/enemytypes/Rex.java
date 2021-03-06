package edu.chalmers.model.enemy.enemytypes;

/**
 * @author Sam Salek
 * <p>
 * Rex class. A type of Enemy.
 */
public class Rex implements IEnemyType {

    @Override
    public String getName() {
        return "Rex";
    }

    @Override
    public int getHealth() {
        return 350;
    }

    @Override
    public int getDamage() {
        return 25;
    }

    @Override
    public int getBlockDamage() {
        return 50;
    }

    @Override
    public int getMoveSpeed() {
        return 70;
    }

    @Override
    public int getJumpHeight() {
        return 255;
    }

    @Override
    public String getTextureIdle() {
        return "DinoSpriteIdle.png";
    }

    @Override
    public String getTextureWalk() {
        return "DinoSpriteWalk.png";
    }

    @Override
    public String getTextureJump() {
        return "DinoSpriteJump.png";
    }
}
