package edu.chalmers.model.weapon.weapontypes;

/**
 * @author Erik Wetter
 * <p>
 * IWeaponType interface for different types of weapons.
 */
public interface IWeaponType {
    int getMagazineSize();

    int getDamage();

    int getReloadTimeMilliseconds();

    int getProjectileSpeed();

    String getName();

}
