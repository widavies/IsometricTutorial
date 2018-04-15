package com.isometric.engine;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.isometric.gamestates.GameState;

public class Input implements GestureDetector.GestureListener, InputProcessor {

    private GameState active;

    public void setActive(GameState gameState) {
        this.active = gameState;
    }

    @Override
    public boolean keyDown(int keycode) {
        active.keyDown(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        active.keyUp(keycode);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        active.keyTyped(character);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        active.touchDown(screenX, screenY, pointer, button);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        System.out.println("Touced!");
        active.touchUp(screenX, screenY, pointer, button);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        active.touchDragged(screenX, screenY, pointer);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        active.mouseMoved(screenX, screenY);
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        active.scrolled(amount);
        return true;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        active.touchDown(x, y, pointer, button);
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        active.tap(x, y, count, button);
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        active.longPress(x, y);
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        active.fling(velocityX, velocityY, button);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        active.pan(x, y, deltaX, deltaY);
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        active.panStop(x, y, pointer, button);
        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        active.zoom(initialDistance, distance);
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        active.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        return true;
    }

    @Override
    public void pinchStop() {
        active.pinchStop();

    }
}
