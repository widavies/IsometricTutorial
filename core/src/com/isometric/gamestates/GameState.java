package com.isometric.gamestates;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public abstract class GameState implements GestureDetector.GestureListener, InputProcessor {

    protected GameStateManager gsm;

    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    public abstract void render(float delta);
    public abstract void resize(int width, int height);
    public abstract void dispose();

    public boolean keyDown(int keycode) {return false;}
    public boolean keyUp(int keycode) {return false;}
    public boolean keyTyped(char character) {return false;}
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {return false;}
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {return false;}
    public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}
    public boolean mouseMoved(int screenX, int screenY) {return false;}
    public boolean scrolled(int amount) {return false;}
    public boolean touchDown(float x, float y, int pointer, int button) {return false;}
    public boolean tap(float x, float y, int count, int button) {return false;}
    public boolean longPress(float x, float y) {return false;}
    public boolean fling(float velocityX, float velocityY, int button) {return false;}
    public boolean pan(float x, float y, float deltaX, float deltaY) {return false;}
    public boolean panStop(float x, float y, int pointer, int button) {return false;}
    public boolean zoom(float initialDistance, float distance) {return false;}
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {return false;}
    public void pinchStop() {}

}
