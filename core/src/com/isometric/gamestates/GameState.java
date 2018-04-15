package com.isometric.gamestates;

import com.badlogic.gdx.math.Vector2;

public abstract class GameState {

    protected GameStateManager gsm;

    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    public abstract void render(float delta);
    public abstract void resize(int width, int height);
    public abstract void dispose();

    public void keyDown(int keycode) { }
    public void keyUp(int keycode) { }
    public void keyTyped(char character) { }
    public void touchDown(int screenX, int screenY, int pointer, int button) { }
    public void touchUp(int screenX, int screenY, int pointer, int button) { }
    public void touchDragged(int screenX, int screenY, int pointer) { }
    public void mouseMoved(int screenX, int screenY) { }
    public void scrolled(int amount) { }
    public void touchDown(float x, float y, int pointer, int button) { }
    public void tap(float x, float y, int count, int button) { }
    public void longPress(float x, float y) { }
    public void fling(float velocityX, float velocityY, int button) { }
    public void pan(float x, float y, float deltaX, float deltaY) { }
    public void panStop(float x, float y, int pointer, int button) { }
    public void zoom(float initialDistance, float distance) { }
    public void pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {}
    public void pinchStop() {}

}
