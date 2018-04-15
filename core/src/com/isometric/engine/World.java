package com.isometric.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.isometric.gamestates.GameState;
import com.isometric.gamestates.GameStateManager;

public class World extends GameState {

    private ExtendViewport viewport;

    /*
     * Map related
     */
    private TiledMap tiledMap;
    private RenderEngine tiledMapRenderer;
    private OrthographicCamera camera;

    // Map information
    private float tileWidth, tileHeight, playableTilesX, playableTilesY, pxWidth, pxHeight;

    /*
     * Movement variables
     */
    private float velocityX = 0;
    private float velocityY = 0;
    private float scale;
    private boolean flinging;
    private float zoomUpperLimit, zoomLowerLimit;
    private float zoomSoftLowerLimit;
    private long lastZoom;
    private boolean tapping;
    private boolean moving;

    public World(GameStateManager gsm) {
        super(gsm);

        // Load the map
        tiledMap = new TmxMapLoader().load("maps/mars.tmx");
        tiledMapRenderer = new RenderEngine(tiledMap);

        playableTilesX = 30;
        playableTilesY = 30;
        tileWidth = ((TiledMapTileLayer) tiledMap.getLayers().get("Background")).getTileWidth();
        tileHeight = ((TiledMapTileLayer) tiledMap.getLayers().get("Background")).getTileHeight();
        pxWidth = playableTilesX * tileWidth;
        pxHeight = playableTilesY * tileHeight;

        // Setup camera
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(pxWidth, pxHeight, camera);
        viewport.apply(true);

        // Setup
        scale = 1f;
        makeCameraZoomInBounds();
        camera.update();
    }

    @Override
    public void render(float delta) {
        /*
         * Update
         */
        makeCameraPositionInBounds();
        makeCameraZoomInBounds();

        camera.update();

        if (flinging) {
            velocityX *= 0.9f;
            velocityY *= 0.9f;
            camera.translate(-velocityX, velocityY);
            makeCameraPositionInBounds();
            if (Math.abs(velocityX) < 0.01f) velocityX = 0;
            if (Math.abs(velocityY) < 0.01f) velocityY = 0;
        }

        if(camera.zoom < zoomSoftLowerLimit && !tapping) {
            camera.zoom += 0.003;
        }

        /*
         * Rendering
         */
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
    }

    @Override
    public void pan(float x, float y, float deltaX, float deltaY) {
        camera.position.add(-deltaX * (pxWidth / Gdx.graphics.getWidth()) * camera.zoom, deltaY * (pxHeight / Gdx.graphics.getHeight()) * camera.zoom, 0);
        makeCameraPositionInBounds();
    }

    @Override
    public void fling(float velocityX, float velocityY, int button) {
        if((System.nanoTime() - lastZoom) / 1000000 <= 200 || moving) return; // prevents confusion of zoom and pan

        flinging = true;
        this.velocityX = camera.zoom * velocityX * (pxWidth / Gdx.graphics.getWidth()) * 0.04f;
        this.velocityY = camera.zoom * velocityY * (pxHeight / Gdx.graphics.getHeight()) * 0.04f;
    }

    @Override
    public void pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        // Calculate the distance
        float initialDistance = (float)distance(initialPointer1, initialPointer2);
        float distance = (float)distance(pointer1, pointer2);

        float x = (pointer1.x + initialPointer2.x)/2;
        float y = (pointer1.y + initialPointer2.y)/2;

        Vector3 location = new Vector3(x, y, 0);

        zoomCamera(location, this.scale * (initialDistance / distance));
        lastZoom = System.nanoTime();
    }

    @Override
    public void scrolled(int amount) {
        camera.zoom += amount * 0.05f;
        makeCameraZoomInBounds();
    }

    @Override
    public void touchDown(float x, float y, int pointer, int button) {
        flinging = false;
        scale = camera.zoom;
        tapping = true;
    }

    @Override
    public void touchUp(int screenX, int screenY, int pointer, int button) {
        tapping = false;
        moving = false;
    }

    private void makeCameraPositionInBounds() {
        if(Float.isInfinite(camera.zoom)) return;

        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
        float scaledViewportWidthHalfExtent = camera.viewportWidth * camera.zoom * 0.5f;

        pxWidth = 256 * 40;
        if (camera.position.x < pxWidth / 2 + scaledViewportWidthHalfExtent + tileWidth) camera.position.x = pxWidth / 2 + scaledViewportWidthHalfExtent + tileWidth;
        if (camera.position.x > pxWidth / 2 + pxWidth - scaledViewportWidthHalfExtent + tileWidth) camera.position.x = pxWidth / 2 + pxWidth - scaledViewportWidthHalfExtent + tileWidth;
        if (camera.position.y < -pxHeight / 2 + effectiveViewportHeight * 0.5f + tileHeight / 2) camera.position.y = -(pxHeight / 2) + effectiveViewportHeight * 0.5f + tileHeight / 2;
        else if (camera.position.y > pxHeight / 2 - effectiveViewportHeight * 0.5f + tileHeight / 2) camera.position.y = pxHeight / 2 - effectiveViewportHeight * 0.5f + tileHeight / 2;
    }

    private void makeCameraZoomInBounds() {
        zoomUpperLimit = pxHeight / (camera.viewportHeight * Math.abs(camera.up.y) + camera.viewportWidth * Math.abs(camera.up.x));
        zoomLowerLimit = tileHeight * 3 / (camera.viewportHeight * Math.abs(camera.up.y) + camera.viewportWidth * Math.abs(camera.up.x));

        if(camera.zoom > zoomUpperLimit) camera.zoom = zoomUpperLimit;
        if(camera.zoom < zoomLowerLimit) camera.zoom = zoomLowerLimit;
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

    private double distance(Vector2 object1, Vector2 object2){
        return Math.sqrt(Math.pow((object2.x - object1.x), 2) + Math.pow((object2.y - object1.y), 2));
    }

    protected Vector3 screenToTileCoords(float x, float y) {
        Vector3 touch = new Vector3(x, y, 0);
        camera.unproject(touch);
        touch.x /= tileWidth;
        touch.y = (touch.y - tileHeight / 2) / tileHeight + touch.x;
        touch.x -=  touch.y - touch.x;
        return touch;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        makeCameraZoomInBounds();
    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        tiledMapRenderer.dispose();
    }
}
