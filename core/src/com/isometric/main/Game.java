package com.isometric.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.isometric.gamestates.GameStateManager;

public class Game extends ApplicationAdapter {

    private GameStateManager gsm;

	@Override
	public void create () {
	    gsm = new GameStateManager();
	}

	@Override
	public void render () {
        gsm.render(Gdx.graphics.getDeltaTime());
    }

	@Override
	public void dispose() {
	    gsm.dispose();
	}

	@Override
    public void resize(int width, int height) {
	    gsm.resize(width, height);
    }
}
