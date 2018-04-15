package com.isometric.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.isometric.engine.Input;
import com.isometric.engine.World;

import java.util.Stack;

public class GameStateManager {

    private Stack<GameState> gameStates;

    private Input input;

    public static final int WORLD = 0;

    public GameStateManager() {
        gameStates = new Stack<GameState>();

        input = new Input();

        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(input);
        im.addProcessor(gd);
        im.addProcessor(input);
        Gdx.input.setInputProcessor(im);

        pushGameState(WORLD);
    }

    public void loadState(int state) {
        gameStates.pop().dispose();
    }

    private void pushGameState(int state) {
        gameStates.push(getState(state));
        input.setActive(gameStates.peek());
    }

    private GameState getState(int state) {
        if(state == WORLD) return new World(this);
        return null;
    }
    /*
     * Pass through methods
     */
    public void render(float delta) {
        if(!gameStates.isEmpty()) gameStates.peek().render(delta);
    }

    public void resize(int width, int height) {
        if(!gameStates.isEmpty()) gameStates.peek().resize(width, height);
    }

    public void dispose() {
        if(!gameStates.isEmpty()) gameStates.peek().dispose();
    }
}
