package edu.chalmers.model.building;

import edu.chalmers.model.building.blocks.Block;
import edu.chalmers.services.Coords;
import edu.chalmers.utilities.Constants;
import edu.chalmers.utilities.CoordsCalculations;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Malte Åkvist
 * <p>
 * BuildManager that handles building in the game.
 */
public class BuildManager {
    private MapManager mapManager;
    private int buildRangeTiles;

    public BuildManager(int buildRangeTiles, MapManager mapManager) {
        this.buildRangeTiles = buildRangeTiles;
        this.mapManager = mapManager;
    }

    /**
     * Places a new block in the game and adds it to the blockMap
     *
     * @param mousePos position of where block should be placed
     */
    public void placeBlock(Point2D mousePos) {
        Block block = new Block(mousePos);
        mapManager.addBlockToMap(CoordsCalculations.posToTile(mousePos), block);
        block.addObserver(mapManager);
    }

    /**
     * Method checks if it's possible to place block on position
     *
     * @param mousePos  position to check if possible to place block on
     * @param playerPos players position to see if in range of placing block
     * @return boolean
     */
    public boolean possibleToPlaceBlockOnPos(Point2D mousePos, Point2D playerPos) {
        Coords buildTile = CoordsCalculations.posToTile(mousePos);
        Coords playerTile = CoordsCalculations.posToTile(playerPos);

        if (!isInBuildRange(playerTile, buildTile) || playerTile.equals(buildTile)) {    // Can't build if not in range or if trying to place block on player
            return false;
        }

        // Possible to build if tile is empty and tile is connected to another tile
        return mapManager.isTileEmpty(buildTile) && mapManager.isTileConnected(buildTile);
    }

    /* Method gets the tiles player can reach (depending on buildrange of player)
    @param playerTile position of player
    @return list of reachable tiles */
    private List<Coords> getReachableTiles(Coords playerTile) {
        Coords startTile = new Coords(playerTile.getX() - buildRangeTiles, playerTile.getY() - buildRangeTiles);

        List<Coords> reachableTiles = new ArrayList<>();

        int tileInRangeSize = buildRangeTiles * 2 + 1;      // The width/height of total tiles in range
        int tilesInRange = tileInRangeSize * tileInRangeSize;   // Width * Height (width = height)

        for (int i = 0; i < tilesInRange; i++) {

            Coords tile = new Coords(startTile.getX() + (i / tileInRangeSize), startTile.getY() + i % tileInRangeSize);   // reachableTile

            if (tileIsInsideMap(tile)) {  // Tile cant be outside of map
                reachableTiles.add(tile);
            }
        }

        return reachableTiles;
    }

    /**
     * Method checks gets the unpopulated tiles the player can reach
     *
     * @param playerTile position of player
     * @return list of unpopulated reachable tiles
     */
    public List<Coords> getEmptyReachableTiles(Coords playerTile) {
        List<Coords> emptyReachableTiles = new ArrayList<>();
        List<Coords> reachableTiles = getReachableTiles(playerTile);

        for (int i = 0; i < reachableTiles.size(); i++) {
            if (mapManager.isTileEmpty(reachableTiles.get(i))) {
                emptyReachableTiles.add(reachableTiles.get(i));
            }
        }
        return emptyReachableTiles;
    }

    /**
     * Method checks if a certain tile is in range of the player's buildrange
     *
     * @param buildTile  tile to check if it is in the build range of player
     * @param playerTile position of player
     * @return boolean
     */
    public boolean isInBuildRange(Coords buildTile, Coords playerTile) {
        if (Math.abs(buildTile.getX() - playerTile.getX()) > buildRangeTiles) return false;
        if (Math.abs(buildTile.getY() - playerTile.getY()) > buildRangeTiles) return false;

        return true;
    }

    private boolean tileIsInsideMap(Coords tile) {
        return 0 <= tile.getX() && tile.getY() < Constants.TILEMAP_WIDTH && 0 <= tile.getY() && tile.getY() < Constants.TILEMAP_HEIGHT;
    }
}
