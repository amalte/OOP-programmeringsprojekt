package edu.chalmers.model.enemy.ai;

import com.almasb.fxgl.entity.Entity;
import edu.chalmers.model.EntityType;
import edu.chalmers.utilities.EntityPos;
import edu.chalmers.utilities.RaycastCalculations;

import java.util.*;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

/**
 * @author Sam Salek
 * <p>
 * PlatformAI. Contains all variables and methods used by Enemy AI regarding platforms.
 */
class PlatformAI {

    // The most recent platform the Player has been in contact with. Is the same across all Enemy entities.
    private static Entity playerRecentPlatformContact = null;
    private EnemyAIComponent AI;
    private List<Entity> platforms = new ArrayList<>();                         // List witt all platforms.
    private List<Double> platformYDeltaList = new ArrayList<>();                // List with all Y-pos delta between thisEnemy and platforms.
    private HashMap<Double, Entity> platformAndYDeltaMap = new HashMap<>();     // HashMap with Y-Pos deltas and their corresponding platform.

    public PlatformAI(EnemyAIComponent enemyAIComponent) {
        this.AI = enemyAIComponent;
    }

    /**
     * Method finds all platforms in the level and adds them to the "platforms" list.
     */
    public void updatePlatforms() {
        platforms = getGameWorld().getEntitiesByType(EntityType.PLATFORM);

        // Return if no platforms are found.
        if (platforms == null || platforms.size() == 0) {
            return;
        }

        int worldPlatformIndex = -1;     // Index of the world platform in the platforms list.

        // Find list index of the world platform.
        for (Entity p : platforms) {
            // If platform is att X=0 (the world 'platform').
            if (p.getX() == 0) {
                worldPlatformIndex = platforms.indexOf(p);
                break;
            }
        }

        // Remove world from platform list.
        if (worldPlatformIndex != -1) {
            platforms.remove(platforms.get(worldPlatformIndex));
        }
    }

    /**
     * Method finds the closest platform to the Enemy.
     *
     * @return The closest platform.
     */
    public Entity getClosestPlatform() {
        if (platforms.size() <= 0) {
            return null;
        }

        // Resets lists and variables for new closest platform search.
        platformYDeltaList.clear();
        platformAndYDeltaMap.clear();
        boolean twoPlatformsFound = false;

        for (Entity p : platforms) {

            // Skip platform if Enemy is standing on it
            if (getPlatformBelowEnemy() != null) {
                if (getPlatformBelowEnemy().equals(p)) {

                    // If only one platform exists and it is the one Enemy is standing on:
                    if (platforms.size() == 1) {
                        return null;
                    }
                    // Else, continue.
                    continue;
                }
            }

            // Y-Position delta between current platform and Enemy. Add delta to list and to map along with the platform.
            Double yDelta = Double.valueOf(Math.abs(p.getY() - AI.getThisEnemy().getY()));

            // Increase the yDelta if a platform with that specific yDelta already exist (so platform doesn't get replaced in HashMap by the same key).
            // Stupid way to fix the issue but not enough time for a proper fix.
            if (platformAndYDeltaMap.get(yDelta) != null) {
                yDelta++;
            }
            platformYDeltaList.add(yDelta);
            platformAndYDeltaMap.put(yDelta, p);
        }

        double smallestYDelta = Collections.min(platformYDeltaList);
        Collections.sort(platformYDeltaList);
        Entity closestPlatform = platformAndYDeltaMap.get(smallestYDelta);

        // If two or more platforms exist: check if closest platform and second closest has a Y-pos difference of less than 5.
        if (platformYDeltaList.size() >= 2) {
            if (Math.abs(smallestYDelta - platformYDeltaList.get(1)) < 5) {
                twoPlatformsFound = true;
            }
        }

        if (twoPlatformsFound) {
            closestPlatform = getClosestPlatformOfTwo();
        }

        return closestPlatform;
    }

    /**
     * Method gets the closest platform if two platforms exist on roughly the same Y-Position.
     *
     * @return The closest platform.
     */
    private Entity getClosestPlatformOfTwo() {
        for (int i = 0; i < platformYDeltaList.size(); i++) {
            for (int j = i + 1; j < platformYDeltaList.size(); j++) {
                Double y1 = platformYDeltaList.get(i);
                Double y2 = platformYDeltaList.get(j);

                // If difference between 2 platforms Y-pos is less than 5
                if (Math.abs(y1 - y2) < 5) {
                    Entity platform1 = platformAndYDeltaMap.get(y1);
                    Entity platform2 = platformAndYDeltaMap.get(y2);

                    // Middle X-Positions for platforms and player
                    double platform1MiddleX = EntityPos.getMiddleX(platform1);
                    double platform2MiddleX = EntityPos.getMiddleX(platform2);
                    double playerMiddleX = EntityPos.getMiddleX(AI.getPlayer());

                    // X-Position delta between platforms and player.
                    Double xDelta_playerPlatform1 = Double.valueOf(Math.abs(platform1MiddleX - playerMiddleX));
                    Double xDelta_playerPlatform2 = Double.valueOf(Math.abs(platform2MiddleX - playerMiddleX));

                    // Return the platform with smallest X-Position delta (closest platform).
                    if (xDelta_playerPlatform1 < xDelta_playerPlatform2) {
                        return platformAndYDeltaMap.get(y1);
                    } else {
                        return platformAndYDeltaMap.get(y2);
                    }
                }
            }
        }

        // None of the platforms were the closest (shouldn't reach this).
        return null;
    }

    /**
     * Method returns the Platform the Enemy is standing on.
     *
     * @return Platform or null (if not standing on platform).
     */
    public Entity getPlatformBelowEnemy() {
        if (AI.getRaycastAI().getLeftDownwardRaycast() == null ||
                AI.getRaycastAI().getRightDownwardRaycast() == null) {
            return null;
        }

        // Platform hit detection
        boolean leftHit = RaycastCalculations.checkRaycastHit(AI.getRaycastAI().getLeftDownwardRaycast(), EntityType.PLATFORM);
        boolean rightHit = RaycastCalculations.checkRaycastHit(AI.getRaycastAI().getRightDownwardRaycast(), EntityType.PLATFORM);

        // leftDownwardRaycast check.
        if (leftHit) {
            // Get platform entity
            Optional<Entity> optionalEntity = AI.getRaycastAI().getLeftDownwardRaycast().getEntity();
            Entity platform = optionalEntity.get();

            if (platforms.contains(platform)) {
                return platform;
            }
        }

        // rightDownwardRaycast check.
        else if (rightHit) {
            // Get platform entity
            Optional<Entity> optionalEntity = AI.getRaycastAI().getRightDownwardRaycast().getEntity();
            Entity platform = optionalEntity.get();

            if (platforms.contains(platform)) {
                return platform;
            }
        }

        // No platform (should not reach this point).
        return null;
    }

    /**
     * Checks if the platform below Enemy is the given targetEntity.
     *
     * @param targetPlatform targetEntity.
     * @return True or False.
     */
    public boolean checkPlatformBelowEnemy(Entity targetPlatform) {
        // If raycasts are null or no platform below Enemy.
        if (AI.getRaycastAI().getLeftDownwardRaycast() == null ||
                AI.getRaycastAI().getRightDownwardRaycast() == null ||
                getPlatformBelowEnemy() == null) {
            return false;
        }

        // Checks if the platform below Enemy is the given targetEntity.
        if (getPlatformBelowEnemy().equals(targetPlatform)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method checks the most recent platform the Player was in contact with. Updates playerRecentPlatformContact variable.
     */
    public void playerRecentPlatformContactCheck() {

        // For each platform in the world...
        for (Entity p : platforms) {

            // If raycast not equal null...
            if (RaycastCalculations.getRaycastHit(AI.getRaycastAI().getLeftPlayerPlatformRaycast()) != null) {
                // Check if leftPlayerPlatformRaycast hit the current platform
                if (RaycastCalculations.getRaycastHit(AI.getRaycastAI().getLeftPlayerPlatformRaycast()).equals(p)) {
                    playerRecentPlatformContact = p;
                    break;
                }
            }

            if (RaycastCalculations.getRaycastHit(AI.getRaycastAI().getRightPlayerPlatformRaycast()) != null) {
                // Check if rightPlayerPlatformRaycast hit the current platform
                if (RaycastCalculations.getRaycastHit(AI.getRaycastAI().getRightPlayerPlatformRaycast()).equals(p)) {
                    playerRecentPlatformContact = p;
                    break;
                }
            }
        }
    }

    /**
     * Getter for playerRecentPlatformContact variable.
     *
     * @return playerRecentPlatformContact.
     */
    public Entity getPlayerRecentPlatformContact() {
        return playerRecentPlatformContact;
    }

    /**
     * Getter for platforms List (all platforms on the level).
     *
     * @return platforms List.
     */
    public List<Entity> getPlatforms() {
        return platforms;
    }
}
