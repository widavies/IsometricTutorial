package com.isometric.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.isometric.engine.IsometricRenderer;

public class World extends GameState {

    /*
     * Map related
     */
    private TiledMap tileMap;
    private IsometricRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    /*
     * Map information
     */
    private float tileWidth, tileHeight;
    private float numTilesX, numTilesY;
    private float pxWidth, pxHeight;

    /*
     * Camera movement variables
     */
    private float velocityX = 0, velocityY = 0;
    private float scale;
    private boolean flinging;
    private float zoomUpperLimit, zoomLowerLimit;
    private float zoomSoftLowerLimit;
    private boolean tapping;
    private long lastZoom;

    // Viewport
    private ExtendViewport viewport;

    public World(GameStateManager gsm) {
        super(gsm);

        AtlasTmxMapLoader.AtlasTiledMapLoaderParameters params = new AtlasTmxMapLoader.AtlasTiledMapLoaderParameters();

        tileMap = new AtlasTmxMapLoader().load("", params);

        // Setup map
        numTilesX = 30;
        numTilesY = 30;
        tileWidth = ((TiledMapTileLayer) tileMap.getLayers().get("Background")).getTileWidth();
        tileHeight = ((TiledMapTileLayer) tileMap.getLayers().get("Background")).getTileHeight();
        pxWidth = numTilesX * tileWidth;
        pxHeight = numTilesY * tileHeight;
        scale = 1f;

        // Setup camera
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(pxWidth, pxHeight, camera);
        viewport.apply(true);

        camera.position.x = (((TiledMapTileLayer) tileMap.getLayers().get("Background")).getWidth() * tileWidth) / 2;
        camera.position.y = 0;
        camera.zoom = 0.7f;
        camera.update();

    }

    @Override
    public void render(float delta) {
        // Update
        makeCameraPositionInBounds();
        makeCameraZoomInBounds();

        camera.update();

        if(flinging) {
            velocityX *= 0.9f;
            velocityY *= 0.9f;
            camera.translate(-velocityX, velocityY);
            makeCameraPositionInBounds();
            if(Math.abs(velocityX) < 0.01f) velocityX = 0;
            if(Math.abs(velocityY) < 0.01f) velocityY = 0;
        }

        if(camera.zoom < zoomSoftLowerLimit && !tapping) {
            camera.zoom += 0.003;
        }

        // Rendering
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        tiledMapRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
       // makeCameraZoomInBounds();

    }

    private void makeCameraPositionInBounds() {
        if(Float.isInfinite(camera.zoom)) return;

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
        float scaledViewportWidthHalfExtent = effectiveViewportWidth * 0.5f;
        float scaledViewportHeightHalfExtent = effectiveViewportHeight * 0.5f;
        float halfTileHeight = ((TiledMapTileLayer) tileMap.getLayers().get("Background")).getTileHeight() / 2;

        pxWidth = 256 * 30;
        if (camera.position.x < pxWidth / 2 + scaledViewportWidthHalfExtent + tileWidth) camera.position.x = pxWidth / 2 + scaledViewportWidthHalfExtent + tileWidth;
        if (camera.position.x > pxWidth / 2 + pxWidth - scaledViewportWidthHalfExtent + tileWidth) camera.position.x = pxWidth / 2 + pxWidth - scaledViewportWidthHalfExtent + tileWidth;

        if (camera.position.y < -pxHeight / 2 + effectiveViewportHeight * 0.5f + tileHeight / 2) camera.position.y = -(pxHeight / 2) + effectiveViewportHeight * 0.5f + tileHeight / 2;
        else if (camera.position.y > pxHeight / 2 - effectiveViewportHeight * 0.5f + tileHeight / 2) camera.position.y = pxHeight / 2 - effectiveViewportHeight * 0.5f + tileHeight / 2;
    }

    private void makeCameraZoomInBounds() {
        zoomUpperLimit = pxHeight / (camera.viewportHeight * Math.abs(camera.up.y) + camera.viewportWidth * Math.abs(camera.up.x));
        zoomLowerLimit = tileHeight * 3 / (camera.viewportHeight * Math.abs(camera.up.y) + camera.viewportWidth * Math.abs(camera.up.x));
        zoomSoftLowerLimit = zoomLowerLimit * 1.5f;

        if(camera.zoom > zoomUpperLimit) camera.zoom = zoomUpperLimit;
        if(camera.zoom < zoomLowerLimit) camera.zoom = zoomLowerLimit;
    }

    protected Vector3 screenToTileCoords(float x, float y) {
        Vector3 touch = new Vector3(x, y, 0);
        camera.unproject(touch);
        touch.x /= tileWidth;
        touch.y = (touch.y - tileHeight / 2) / tileHeight + touch.x;
        touch.x -=  touch.y - touch.x;
        return touch;
    }

    protected Vector3 tileToScreenCoords(float x, float y) {
        Vector3 touch = new Vector3(x, y, 0);
        touch.x = touch.x + (touch.x - touch.y) * tileWidth;
        touch.y = touch.y * (tileHeight + touch.x / tileWidth) + tileHeight / 2;
        camera.project(touch);
        return touch;
    }

    private double distance(Vector2 object1, Vector2 object2){
        return Math.sqrt(Math.pow((object2.x - object1.x), 2) + Math.pow((object2.y - object1.y), 2));
    }

    private void zoomCamera(Vector3 location, float scale) {
        //make sure changes to position and zoom are applied
        camera.update();
        Vector3 oldPoint = camera.unproject(location.cpy()).cpy();
        camera.zoom = scale;
        makeCameraZoomInBounds();
        camera.update();
        Vector3 newPoint = camera.unproject(location.cpy()).cpy();
        Vector3 camMovement = oldPoint.add(newPoint.scl(-1f));
        camera.position.add(camMovement);
        makeCameraPositionInBounds();
    }

    /*
     *
     * INPUT
     *
     */
    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 loc = screenToTileCoords(x, y);

        System.out.println("x: "+loc.x+"y: "+loc.y);

        return false;
    }
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.position.add(-deltaX * (pxWidth / Gdx.graphics.getWidth()) * camera.zoom, deltaY * (pxHeight / Gdx.graphics.getHeight()) * camera.zoom, 0);
        makeCameraPositionInBounds();

        return true;
    }
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        flinging = true;
        this.velocityX = camera.zoom * velocityX * (pxWidth / Gdx.graphics.getWidth()) * 0.04f;
        this.velocityY = camera.zoom * velocityY * (pxHeight / Gdx.graphics.getHeight()) * 0.04f;

        return true;
    }
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        // Calculate the distance
        float initialDistance = (float)distance(initialPointer1, initialPointer2);
        float distance = (float)distance(pointer1, pointer2);

        float x = (pointer1.x + initialPointer2.x)/2;
        float y = (pointer1.y + initialPointer2.y)/2;

        Vector3 location = new Vector3(x, y, 0);

        zoomCamera(location, this.scale * (initialDistance / distance));
        lastZoom = System.nanoTime();

        return true;
    }
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        flinging = false;
        scale = camera.zoom;
        tapping = true;

        return true;
    }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        tapping = false;
        return true;
    }
    @Override
    public boolean scrolled(int amount) {
        camera.zoom += amount*0.05f;
        makeCameraZoomInBounds();
        return true;
    }
    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public void dispose() {
        tileMap.dispose();
        tiledMapRenderer.dispose();
    }
}
