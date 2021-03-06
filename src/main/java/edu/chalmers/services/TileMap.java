package edu.chalmers.services;

import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.entity.level.tiled.TiledMap;
import edu.chalmers.model.building.IBlock;
import edu.chalmers.model.building.blocks.PermanentBlock;
import edu.chalmers.utilities.Constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * @author Malte Åkvist
 * <p>
 * TileMap service, loads an array (map of a level) from TMX file and converts it to a HashMap.
 */
public class TileMap {
    private String dataLayer = "Tile Layer 1";
    private int emptyTile = 0;

    /**
     * Method converts an int array from a certain level to a hash map containing blocks and coordinates of the blocks
     *
     * @param levelName name of the level
     * @return hash map containing coordinates of the map and the corresponding block present on coordinate
     */
    public HashMap<Coords, IBlock> getBlockMapFromLevel(String levelName) {
        HashMap<Coords, IBlock> blockMap = new HashMap<>();
        List<Integer> dataList = getDataFromLevel(levelName);

        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i) != emptyTile) {    // contains permanent that cant be removed (platforms)
                blockMap.put(new Coords(i % Constants.TILEMAP_WIDTH, i / Constants.TILEMAP_WIDTH), new PermanentBlock());
            }
        }

        return blockMap;
    }

    // Method gets an int array of all blocks on the level
    private List<Integer> getDataFromLevel(String levelName) {
        TiledMap tileMap = getTileMap(levelName);

        return tileMap.getLayerByName(dataLayer).getData();
    }

    private TiledMap getTileMap(String levelName) {
        return new TMXLevelLoader().parse(getClass().getResourceAsStream("/assets/levels/" + levelName));
    }
}
