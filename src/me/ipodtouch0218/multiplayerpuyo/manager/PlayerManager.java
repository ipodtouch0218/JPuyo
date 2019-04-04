package me.ipodtouch0218.multiplayerpuyo.manager;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

import org.joyconLib.Joycon;
import org.joyconLib.JoyconConstant;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.GameWindow;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteAnimation;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.input.InputHandler;
import me.ipodtouch0218.java2dengine.object.GameObject;

public class PlayerManager extends GameObject {
	
	private static final Controls[] keyboardControls = {
			new Controls(KeyEvent.VK_N, KeyEvent.VK_M, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_UP),
			new Controls(KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_W),
			new Controls(KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_F, KeyEvent.VK_H, KeyEvent.VK_G, KeyEvent.VK_T),
			new Controls(KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_I)
	};
	
	private static final SpriteSheet batteryIndicators = new SpriteSheet("ui/board/controller/battery-indicators.png", 10, 16);
	private static final SpriteSheet playerIndicators = new SpriteSheet("ui/board/controller/player-indicators.png", 16, 16);
	private static final GameSprite connectScreen = new GameSprite("ui/board/controller/connectscreen.png", false);
	private static final GameSprite connectScreenBig = new GameSprite("ui/board/controller/connectscreenlarge.png", false);
	
	private static Object[] plsprites = {
		playerIndicators.getSprite(1, 0),
		playerIndicators.getSprite(2, 0),
		playerIndicators.getSprite(3, 0),
		playerIndicators.getSprite(4, 0),
		null, null, null, null
	};
	public static GameSprite[] batterySprites = {
		batteryIndicators.getSprite(0, 0),
		batteryIndicators.getSprite(1, 0),
		batteryIndicators.getSprite(2, 0),
		batteryIndicators.getSprite(3, 0),
		batteryIndicators.getSprite(4, 0),
		batteryIndicators.getSprite(5, 0)
	};
	
	private static final GameSprite player0 = playerIndicators.getSprite(0, 0);
	{
		plsprites[4] = new SpriteAnimation(new GameSprite[]{(GameSprite) plsprites[0],player0}, 3);
		plsprites[5] = new SpriteAnimation(new GameSprite[]{(GameSprite) plsprites[1],player0}, 3);
		plsprites[6] = new SpriteAnimation(new GameSprite[]{(GameSprite) plsprites[2],player0}, 3);
		plsprites[7] = new SpriteAnimation(new GameSprite[]{(GameSprite) plsprites[3],player0}, 3);
	}
	
	private static final GameSprite full = new GameSprite("ui/board/controller/joycon-full.png", false);
	private static final GameSprite left = new GameSprite("ui/board/controller/joycon-left.png", false);
	private static final GameSprite right = new GameSprite("ui/board/controller/joycon-right.png", false);
	private static final GameSprite keyboard = new GameSprite("ui/board/controller/keyboard.png", false);
	private static final GameSprite keyboardInverted = new GameSprite("ui/board/controller/keyboard_inverted.png", false);
	
	private static final Controls dummy = new Controls(0,0,0,0,0,0);
	private static PlayerManager instance;
	
	private ArrayList<Controls> connectedControllers = new ArrayList<>();
	private ArrayList<Controls> players = new ArrayList<>();
	private Runnable onComplete;
	
	public PlayerManager() {
		
		instance = this;
		boolean continueLoop = true;
		while (continueLoop) {
			continueLoop = false;
			Joycon con = new Joycon(JoyconConstant.JOYCON_RIGHT);
			if (con.isOpen()) {
				Controls newCont = new JoyconControls(con, 0);
				connectedControllers.add(newCont);
				continueLoop = true;
			}
		}
		continueLoop = true;
		while (continueLoop) {
			continueLoop = false;
			Joycon con = new Joycon(JoyconConstant.JOYCON_LEFT);
			if (con.isOpen()) {
				Controls newCont = new JoyconControls(con, 0);
				connectedControllers.add(newCont);
				continueLoop = true;
			}
		}
		
		for (Controls c : keyboardControls) {
			GameEngine.addGameObject(c);
		}
		for (Controls cont : connectedControllers) {
			GameEngine.addGameObject(cont);
		}
	}
	
	private VolatileImage lastImg; 
	private int lastPlayers;
	private boolean render;
	@Override
	public void render(Graphics2D g) {
		if (!configure) { return; }
		if (render || lastImg == null || lastPlayers != players.size()) {
			VolatileImage newImg = GameRenderer.createVolatile(GameWindow.getSetWidth(), GameWindow.getSetHeight(), false);
			renderImg(newImg.getGraphics());
			lastImg = newImg;
			lastPlayers = players.size();
			render = false;
		}

		g.drawImage(lastImg, 0, 0, null);
	}

	private boolean configure = false;
	private int keyboardPlayers = 0;
	private int requiredPlayers = 1;
	private boolean addDown;

	@Override
	public void tick(double delta) { 
		if (!configure) { return; }
		
		checkForConfirm();
		
		checkForJoyconPlayers();
		checkForKeyboardPlayers();
	}
	private void checkForConfirm() {
		if (getPlayers() >= requiredPlayers) {
			if (getMainControl().menuEnter) {
				configure = false;
				if (onComplete != null) {
					onComplete.run();
					onComplete = null;
				}
			}
		}
	}
	private void checkForJoyconPlayers() {
		JoyconControls pairPartner = null;
		boolean readyForPair = false, left = false;
		bigloop:
		for (Controls cont : connectedControllers) {
			if (!(cont instanceof JoyconControls)) { continue; }
			JoyconControls jc = (JoyconControls) cont;
			if (jc.btnSl && jc.btnSr) {
				jc.setRotated(true);
				check:
				for (Controls c : players) {
					if (c == jc) {
						continue bigloop;
					}
					if (c instanceof DualJoyconControls) {
						DualJoyconControls dc = (DualJoyconControls) c;
						if (dc.leftJoycon == jc || dc.rightJoycon == jc) {
							removePlayer(dc);
							break check;
						}
					}
				}
				addPlayer(jc);
				continue;
			}
			if (jc.btnR || jc.btnL || jc.btnZr || jc.btnZl) {
				
				if (readyForPair) {
					check:
					for (Controls c : players) {
						if (c == jc) {
							removePlayer(jc);
							break check;
						}
						if (c instanceof DualJoyconControls) {
							DualJoyconControls dc = (DualJoyconControls) c;
							if ((dc.leftJoycon == jc || dc.rightJoycon == jc) && (dc.leftJoycon == pairPartner || dc.rightJoycon == pairPartner)) {
								pairPartner = null;
								readyForPair = false;
								continue bigloop;
							}
							if (dc.leftJoycon == jc || dc.rightJoycon == jc) {
								removePlayer(dc);
								break check;
							}
						}
					}
					addPlayer(GameEngine.addGameObject(new DualJoyconControls((left ? pairPartner : jc), (left ? jc : pairPartner))));
					readyForPair = false;
					pairPartner = null;
				} else {
					if (jc.getJoycon().getJoyconSide() == JoyconConstant.JOYCON_LEFT) {
						left = true;
					}
					readyForPair = true;
					pairPartner = jc;
				}
				
			}
			
		}
	}
	private void checkForKeyboardPlayers() {
		if (keyboardPlayers >= 4) { return; }
		
		boolean invert = InputHandler.isKeyPressed(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
		boolean add = InputHandler.isKeyPressed(KeyEvent.VK_SPACE) || invert;
		
		if (add) {
			if (addDown) {
				return;
			}
			Controls newC = keyboardControls[keyboardPlayers];
			newC.setInverted(invert);
			addPlayer(newC);
			addDown = true;
			keyboardPlayers++;
		} else {
			addDown = false;
		}
	}
	private void addPlayer(Controls pl) {
		if (players.contains(pl)) { return; }
		pl.setPlayer(players.size()+1);
		players.add(pl);
		render = true;
	}
	private void removePlayer(Controls pl) {
		if (!players.contains(pl)) { return; }
		for (int i = pl.getPlayer(); i < players.size(); i++) {
			players.get(i).setPlayer(i-1);
		}
		players.remove(pl);
		render = true;
	}
	
	public void configure(int i) {
		configure = true;
		requiredPlayers = i;
		keyboardPlayers = 0;
		for (Controls cons : players) {
			if (cons instanceof DualJoyconControls) {
				GameEngine.removeGameObject(cons);
				continue;
			} else if (cons instanceof JoyconControls) {
				((JoyconControls) cons).setRotated(true);
			} else {
				cons.setInverted(false);
			}
			cons.setPlayer(0);
		}
		players.clear();
	}
	public void configure(int value, Runnable runnable) {
		configure(value);
		onComplete = runnable;
	}
	
	//getplayer controls
	public Controls getPlayer(int player) {
		for (Controls cons : players) {
			if (cons.getPlayer() == player) { return cons; }
		}
		return dummy;
	}
	public Controls getMainControl() { 
		if (getPlayer(1) != dummy) {
			return getPlayer(1);
		}
		if (connectedControllers.size() > 0) {
			return connectedControllers.get(0);
		}
		return dummy;
	}
	public static PlayerManager getInstance() { return instance; }
	public boolean isConfiguring() { return configure; }

	public int getPlayers() {
		return players.size();
	}
	
	private void renderImg(Graphics imgG) {
		int spacer = 0;
		if (getPlayers() >= 4) {
			imgG.drawImage(connectScreenBig.getImage().getScaledInstance(GameWindow.getSetWidth(), GameWindow.getSetHeight(), Image.SCALE_FAST), 0, 0, null);
			
			for (int i = 0; i < getPlayers(); i++) {
				VolatileImage temp = GameRenderer.createVolatile(100, 120, false);
				Graphics2D tempG = temp.createGraphics();
				Controls pl = getPlayer(i+1);
				if (pl == null) { continue; }
				
				{
					Object sprite = plsprites[Math.min(8, pl.getPlayer())-1];
					GameSprite finalSprite = null;
					if (sprite instanceof GameSprite) {
						finalSprite = (GameSprite) sprite;
					} else {
						SpriteAnimation spr = (SpriteAnimation) sprite;
						if (spr.isStopped()) {
							spr.start(true);
						}
						finalSprite = spr.getCurrentFrame();
					}
					tempG.drawImage(finalSprite.getImage(), 46, 8, 32, 32, null);
				}
				{
					GameSprite sprite = null;
					if (pl instanceof JoyconControls) {
						short side = ((JoyconControls) pl).getJoycon().getJoyconSide();
						if (side == JoyconConstant.JOYCON_LEFT) {
							sprite = left;
						} else {
							sprite = right;
						}
					} else if (pl instanceof DualJoyconControls) {
						sprite = full;
					} else {
						if (pl.isInverted()) {
							sprite = keyboardInverted;
						} else {
							sprite = keyboard;
						}
					}
					tempG.drawImage(sprite.getImage(), 32, 32, 64, 64, null);
				}
				if (pl instanceof JoyconControls) {
					GameSprite sprite = batterySprites[((JoyconControls) pl).getBattery()%2];
					tempG.drawImage(sprite.getImage(), 58, 90, null);
				}
				int x = 75+(spacer*123), y = 70;
				if (spacer >= 4) {
					x = 75+((spacer-4)*123); 
					y = 180;
				}
				imgG.drawImage(temp, x, y, null);
				spacer++;
			}
			
		} else {
			imgG.drawImage(connectScreen.getImage().getScaledInstance(GameWindow.getSetWidth(), GameWindow.getSetHeight(), Image.SCALE_FAST), 0, 0, null);
			
			for (int i = 0; i < getPlayers(); i++) {
				VolatileImage temp = GameRenderer.createVolatile(100, 120, false);
				Graphics tempG = temp.getGraphics();
				Controls pl = getPlayer(i+1);
				if (pl == null) { continue; }
				
				{
					Object sprite = plsprites[Math.min(8, pl.getPlayer())-1];
					GameSprite finalSprite = null;
					if (sprite instanceof GameSprite) {
						finalSprite = (GameSprite) sprite;
					} else {
						SpriteAnimation spr = (SpriteAnimation) sprite;
						if (spr.isStopped()) {
							spr.start(true);
						}
						finalSprite = spr.getCurrentFrame();
					}
					tempG.drawImage(finalSprite.getImage().getScaledInstance(32, 32, Image.SCALE_FAST), 46, 8, null);
				}
				{
					GameSprite sprite = null;
					if (pl instanceof JoyconControls) {
						short side = ((JoyconControls) pl).getJoycon().getJoyconSide();
						if (side == JoyconConstant.JOYCON_LEFT) {
							sprite = left;
						} else {
							sprite = right;
						}
					} else if (pl instanceof DualJoyconControls) {
						sprite = full;
					} else {
						if (pl.isInverted()) {
							sprite = keyboardInverted;
						} else {
							sprite = keyboard;
						}
					}
					Image image = null;
					if (pl instanceof JoyconControls) {
						JoyconControls plj = (JoyconControls) pl;
						if (plj.btnSr && plj.btnSl) {
							image = sprite.getImage().getScaledInstance(70, 70, Image.SCALE_FAST);
						} else {
							image = sprite.getImage().getScaledInstance(64, 64, Image.SCALE_FAST);
						}
					} else {
						image = sprite.getImage().getScaledInstance(64, 64, Image.SCALE_FAST);
					}
					tempG.drawImage(image, 32, 32, null);
				}
				if (pl instanceof JoyconControls) {
					GameSprite sprite = batterySprites[((JoyconControls) pl).getBattery()%2];
					tempG.drawImage(sprite.getImage(), 58, 90, null);
				}
				imgG.drawImage(temp, 55+(spacer*135), 160, null);
				spacer++;
			}
		}
	}
}
