package com.example.epic.testapplication;

import android.content.res.AssetManager;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides open street map tile images for TileOverlay.
 */
public class OSMTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256; //
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;

    private AssetManager mAssets; //assetmanager for resource files

    /**
     * Creates new OSMTileProvider
     * @param assets application raw asset files
     */
    public OSMTileProvider(AssetManager assets) {
        mAssets = assets;
    }

    /**
     * Gets Tile (byte array containing image data from x, y, zoom parameters
     * @param x tile x coordinate
     * @param y tile y coordinate
     * @param zoom tile zoom level
     * @return Tile
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
    }

    /**
     * Reads in png into buffer and output byte array
     * @param x tile x coordinate
     * @param y tile y coordinate
     * @param zoom tile zoom level
     * @return byte array of tile
     */
    private byte[] readTileImage(int x, int y, int zoom) {
        //input stream to read in images
        InputStream in = null;
        //buffer writing data into byte array
        ByteArrayOutputStream buffer = null;

        try {
            in = mAssets.open(getTileFilename(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            //read data into buffer while input stream still has data
            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            //flush buffer to write
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            //closes buffer
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Gets image file name in assets directory
     * @param x tile x coordinate
     * @param y tile y coordinate
     * @param zoom tile zoom level
     * @return image png file name
     */
    private String getTileFilename(int x, int y, int zoom) {
        return "Tiles/" + zoom + '/' + x + '/' + y + ".png";
    }
}
