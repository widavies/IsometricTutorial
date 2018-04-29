package com.isometric.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;

public class GameStateManager {

    /*
     * Stores the active game states
     */
    private GameState active;

    public static final int MENU = 0, WORLD = 1;

    public GameStateManager() {
        // States
        setState(WORLD);
    }

    public void setState(int state) {
        active = getState(state);

        // Re-point the input
        syncInput(null);
    }

    /*
     * Points the input pipeline to the game state at the top of the stack.
     * InputProcessors can also be added onto the input pipeline if wanted
     */
    public void syncInput(InputProcessor processor) {
        InputMultiplexer im = new InputMultiplexer();

        /*
         * This is the order in which input is processed. If the input method
         * returns false, then the next processor down the line will get a chance
         * at processing the event
         */
        if(processor != null) im.addProcessor(processor);
        im.addProcessor(new GestureDetector(active));
        im.addProcessor(active);
        Gdx.input.setInputProcessor(im);
    }

    private GameState getState(int state) {
        if(state == WORLD) return new World(this);
        return null;
    }

    /*
     * Pass through methods
     */
    public void render(float delta) {if(active != null) active.render(delta);}
    public void resize(int width, int height) {if(active != null) active.resize(width, height);}
    public void dispose() {if(active != null) active.dispose();}
}
