package com.isometric.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class IsometricRenderer {

    // Contains a 2D array of all the tiles on map
    private TiledMap map;

    // Rendering
    private SpriteBatch batch;

    // View-bounds
    private Rectangle viewBounds;
    private Matrix4 isoTransform;
    private Matrix4 invIsotransform;
    private Vector3 screenPos;
    private Vector2 topRight = new Vector2();
    private Vector2 bottomLeft = new Vector2();
    private Vector2 topLeft = new Vector2();
    private Vector2 bottomRight = new Vector2();
    private OrthographicCamera camera;

    // Bulge
    private int x, y;
    private int bulge;
    private boolean down;

    public IsometricRenderer(TiledMap map, OrthographicCamera camera) {
        this.map = map;
        this.camera = camera;

        batch = new SpriteBatch();

        // create the isometric transform
        isoTransform = new Matrix4();
        isoTransform.idt();

        // isoTransform.translate(0, 32, 0);
        isoTransform.scale((float)(Math.sqrt(2.0) / 2.0), (float)(Math.sqrt(2.0) / 4.0), 1.0f);
        isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);

        // ... and the inverse matrix
        invIsotransform = new Matrix4(isoTransform);
        invIsotransform.inv();

        viewBounds = new Rectangle();
    }

    /*
     * Called to render the map.
     * Render tiles, layers, image layers, entities
     */
    public void render() {
        batch.setProjectionMatrix(camera.combined);
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);

        AnimatedTiledMapTile.updateAnimationBaseTime();
        batch.begin();
        for (MapLayer layer : map.getLayers()) {
            renderMapLayer((TiledMapTileLayer)layer);
        }
        batch.end();
    }

    private void renderMapLayer(TiledMapTileLayer layer) {
        // General map info
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();
        float halfTileWidth = tileWidth * 0.5f;
        float halfTileHeight = tileHeight * 0.5f;
        /*
         * Figure out coordinates of on screen tiles
         */
        final float layerOffsetX = layer.getRenderOffsetX();
        // offset in tiled is y down, so we flip it
        final float layerOffsetY = -layer.getRenderOffsetY();
        topRight.set(viewBounds.x + viewBounds.width, viewBounds.y - layerOffsetY);
        bottomLeft.set(viewBounds.x - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);
        topLeft.set(viewBounds.x - layerOffsetX, viewBounds.y - layerOffsetY);
        bottomRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);

        // transforming screen coordinates to iso coordinates
        int row1 = (int)(translateScreenToIso(topLeft).y / tileWidth) - 5;
        int row2 = (int)(translateScreenToIso(bottomRight).y / tileWidth) + 5;
        int col1 = (int)(translateScreenToIso(bottomLeft).x / tileWidth) - 5;
        int col2 = (int)(translateScreenToIso(topRight).x / tileWidth) + 5;

        if(row1 < 25) row1 = 25;
        if(row2 > 55) row2 = 55;
        if(col1 < 25) col1 = 25;
        if(col2 > 55) col2 = 55;

        if(x != -1 && y != -1) {
            bulge = down ? bulge-4 : bulge+4;
            if(bulge > 0.2 * tileWidth) {
                down = true;
            }
            if(bulge < 0) x = y = -1;
        }

        for(int row = row2; row >= row1; row--) {
            for(int col = col1; col <= col2; col++) {
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);
                if(cell == null) continue;
                final TiledMapTile tile = cell.getTile();
                if(tile == null) continue;
                final TextureRegion region = tile.getTextureRegion();

                if(row == y && col == x) {
                    batch.draw(region, (col * halfTileWidth) + (row * halfTileWidth) + tile.getOffsetX() + layer.getRenderOffsetX() - bulge / 2,
                            (row * halfTileHeight) - (col * halfTileHeight) + tile.getOffsetY() + -layer.getRenderOffsetY() - bulge / 2, tileWidth + bulge, tileHeight + bulge);
                }
                else {
                    batch.draw(region, (col * halfTileWidth) + (row * halfTileWidth) + tile.getOffsetX() + layer.getRenderOffsetX(),
                            (row * halfTileHeight) - (col * halfTileHeight) + tile.getOffsetY() + -layer.getRenderOffsetY(), tileWidth, tileHeight);
                }

            }
        }

    }

    public void dispose() {
        batch.dispose();
    }

    private void renderImageLayer(TiledMapImageLayer layer) {

    }

    public void renderObjects(MapLayer layer) {}
    public void renderObject(MapObject object) {}

    private Vector3 translateScreenToIso (Vector2 vec) {
        screenPos.set(vec.x, vec.y, 0);
        screenPos.mul(invIsotransform);

        return screenPos;
    }
}
