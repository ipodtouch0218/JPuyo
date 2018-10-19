package me.ipodtouch0218.multiplayerpuyo.manager;

import java.awt.event.KeyEvent;

import me.ipodtouch0218.java2dengine.input.InputHandler;
import me.ipodtouch0218.java2dengine.object.GameObject;

public class Controls extends GameObject {

	protected int player;
	protected boolean ingame;

	public boolean menuLeft, menuRight, menuUp, menuDown, menuEnter = false;

	private int keyTurnLeft, keyTurnRight, keyMoveLeft, keyMoveRight, keyFastDrop, keyInstaDrop;
	protected boolean turningLeft, turningRight, movingLeft, movingRight, fastDrop, instaDrop, pause;
	private boolean inverted = false;

	public Controls() {
		this(KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_UP);
	}

	public Controls(int turnLeft, int turnRight, int moveLeft, int moveRight, int fastDrop, int instaDrop) {
		keyTurnLeft = turnLeft;
		keyTurnRight = turnRight;
		keyMoveLeft = moveLeft;
		keyMoveRight = moveRight;
		keyFastDrop = fastDrop;
		keyInstaDrop = instaDrop;
	}

	public Controls(int[] controls) {
		this(controls[0], controls[1], controls[2], controls[3], controls[4], controls[5]);
	}

	@Override
	public void tick(double delta) {
		if (player == 0) { return; }
		menuLeft = InputHandler.isKeyPressed(keyMoveLeft);
		menuRight = InputHandler.isKeyPressed(keyMoveRight);
		menuDown = InputHandler.isKeyPressed(keyFastDrop);
		menuUp = InputHandler.isKeyPressed(keyInstaDrop);
		menuEnter = InputHandler.isKeyPressed(keyTurnLeft);
		if (ingame) {
			if (inverted) {
				turningLeft = InputHandler.isKeyPressed(keyTurnRight);
				turningRight = InputHandler.isKeyPressed(keyTurnLeft);
			} else {
				turningLeft = InputHandler.isKeyPressed(keyTurnLeft);
				turningRight = InputHandler.isKeyPressed(keyTurnRight);
			}
			
			movingLeft = menuLeft;
			movingRight = menuRight;
			fastDrop = menuDown;
			instaDrop = menuUp;
		}
		pause = InputHandler.isKeyPressed(KeyEvent.VK_ESCAPE);
	}

	public void setPlayer(int player) {
		this.player = player;
	}
	
	public void setInverted(boolean value) {
		inverted = value;
	}

	// ---Getters---//
	public int getPlayer() {
		return player;
	}
	
	public boolean isInverted() {
		return inverted;
	}

	public boolean isTurnLeftDown() {
		return turningLeft;
	}

	public boolean isTurnRightDown() {
		return turningRight;
	}

	public boolean isMovingLeftDown() {
		return movingLeft;
	}

	public boolean isMovingRightDown() {
		return movingRight;
	}

	public boolean isFastDropDown() {
		return fastDrop;
	}

	public boolean isInstaDropDown() {
		return instaDrop;
	}
	
	public boolean isPaused() {
		return pause;
	}

	// ---Others---//
	public void invertControls() {
		inverted = !inverted;
	}

	public void setInGame(boolean b) {
		ingame = b;
	}
}
