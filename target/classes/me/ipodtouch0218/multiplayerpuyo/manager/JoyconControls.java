package me.ipodtouch0218.multiplayerpuyo.manager;

import java.util.Map.Entry;

import org.joyconLib.Joycon;
import org.joyconLib.JoyconConstant;
import org.joyconLib.JoyconEvent;
import org.joyconLib.JoyconListener;

import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;

public class JoyconControls extends Controls {

	protected boolean rotated;
	protected Joycon joycon;
	protected byte battery;
	
	public boolean pause = false;
	public boolean btnLeft = false, btnRight = false, btnUp = false, btnDown = false, btnSl = false, btnSr = false;
	public boolean btnL = false, btnZl = false, btnR = false, btnZr = false;
	
	public boolean stickUp = false, stickLeft = false, stickRight = false, stickDown = false;
	public double stickX = 0, stickY = 0;
	private double deadzone = 0.5;
	
	public JoyconControls(Joycon joycon, int player) {
		if (joycon == null) { return; }
		this.joycon = joycon;
		
		joycon.setListener(new JoyconListener() {
			public void handleNewInput(JoyconEvent e) {
				stickX = e.getHorizontal();
				stickY = e.getVertical();

				if (joycon.getJoyconSide() == JoyconConstant.JOYCON_LEFT) {
					for (Entry<String,Boolean> btns : e.getNewInputs().entrySet()) {
						if (rotated) {
							switch (btns.getKey()) {
							case JoyconConstant.LEFT: { btnDown = btns.getValue(); continue; }
							case JoyconConstant.DOWN: { btnRight = btns.getValue(); continue; }
							case JoyconConstant.RIGHT:  { btnUp = btns.getValue(); continue; }
							case JoyconConstant.UP: { btnLeft = btns.getValue(); continue; }
							
							case JoyconConstant.MINUS: { pause = btns.getValue(); continue; }
							
							case JoyconConstant.SR: { btnSr = btns.getValue(); continue; }
							case JoyconConstant.SL: { btnSl = btns.getValue(); continue; }
							case JoyconConstant.CAPTURE: { 
								if (btns.getValue()) {
									PuyoGameMain.screenshot();
								}
							}
							}
						} else {
							switch (btns.getKey()) {
							case JoyconConstant.LEFT: { btnLeft = btns.getValue(); continue; }
							case JoyconConstant.DOWN: { btnDown = btns.getValue(); continue; }
							case JoyconConstant.RIGHT: { btnRight = btns.getValue(); continue; }
							case JoyconConstant.UP: { btnUp = btns.getValue(); continue; }
							
							case JoyconConstant.MINUS: { pause = btns.getValue(); continue; }
							
							case JoyconConstant.SR: { btnSr = btns.getValue(); continue; }
							case JoyconConstant.SL: { btnSl = btns.getValue(); continue; }
							
							case JoyconConstant.L: { btnL = btns.getValue(); continue; }
							case JoyconConstant.ZL: { btnZl = btns.getValue(); continue; }
							case JoyconConstant.CAPTURE: { 
								if (btns.getValue()) {
									PuyoGameMain.screenshot();
								}
							}
							}
						}
					}
					
					if (rotated) {
						stickUp = stickX > deadzone;
						stickRight = stickY < -deadzone;
						stickDown = stickX < -deadzone;
						stickLeft = stickY > deadzone;
					} else {
						stickUp = stickY > deadzone;
						stickRight = stickX > deadzone;
						stickDown = stickY < -deadzone;
						stickLeft = stickX < -deadzone;
					}
				} else {
					for (Entry<String,Boolean> btns : e.getNewInputs().entrySet()) {
						if (rotated) {
							switch (btns.getKey()) {
							case JoyconConstant.Y: { btnUp = btns.getValue(); continue; }
							case JoyconConstant.B: { btnLeft = btns.getValue(); continue; }
							case JoyconConstant.A: { btnDown = btns.getValue(); continue; }
							case JoyconConstant.X: { btnRight = btns.getValue(); continue; }
							case JoyconConstant.PLUS: { pause = btns.getValue(); continue; }
							
							case JoyconConstant.SR: { btnSr = btns.getValue(); continue; }
							case JoyconConstant.SL: { btnSl = btns.getValue(); continue; }
							}
						} else {
							switch (btns.getKey()) {
							case JoyconConstant.Y: { btnLeft = btns.getValue(); continue; }
							case JoyconConstant.B: { btnDown = btns.getValue(); continue; }
							case JoyconConstant.A: { btnRight = btns.getValue(); continue; }
							case JoyconConstant.X: { btnUp = btns.getValue(); continue; }
							case JoyconConstant.PLUS: { pause = btns.getValue(); continue; }
							
							case JoyconConstant.SR: { btnSr = btns.getValue(); continue; }
							case JoyconConstant.SL: { btnSl = btns.getValue(); continue; }
							
							case JoyconConstant.R: { btnR = btns.getValue(); continue; }
							case JoyconConstant.ZR: { btnZr = btns.getValue(); continue; }
							}
						}
					}
					
					if (rotated) {
						stickUp = stickX < -deadzone;
						stickRight = stickY > deadzone;
						stickDown = stickX > deadzone;
						stickLeft = stickY < -deadzone;
					} else {
						stickUp = stickY > deadzone;
						stickRight = stickX > deadzone;
						stickDown = stickY < -deadzone;
						stickLeft = stickX < -deadzone;
					}
				}
				
				battery = e.getBattery();
				updatePos();
			}
		});
		
		this.player = player;
		joycon.setPlayerLights((byte) (1 << player-1));
		
	}
	
	private void updatePos() {
		turningLeft = btnRight;
		turningRight = btnDown;
		
		movingLeft = stickLeft;
		movingRight = stickRight;
		fastDrop = stickDown;
		instaDrop = stickUp;
		
		menuRight = movingRight;
		menuLeft = movingLeft;
		menuDown = fastDrop;
		menuUp = instaDrop;
		menuEnter = btnRight || pause;
	}

	public void setPlayer(int player) { 
		this.player = player; 
		if (player == 0) {
			joycon.setPlayerLights((byte) 240);
			rotated = true;
			return;
		}
		joycon.setPlayerLights((byte) (1 << player-1));
	}
	public void setInGame(boolean ingame) { this.ingame = ingame; }
	public void setRotated(boolean rotated) { this.rotated = rotated; }
	
	//---Getters---//
	public boolean isTurnLeftDown() { return turningLeft; }
	public boolean isTurnRightDown() { return turningRight; }
	public boolean isMovingLeftDown() { return movingLeft; }
	public boolean isMovingRightDown() { return movingRight; }
	public boolean isFastDropDown() { return fastDrop; }
	public boolean isInstaDropDown() { return instaDrop; }
	
	public byte getBattery () { return battery; }
	public Joycon getJoycon() { return joycon; }
}
