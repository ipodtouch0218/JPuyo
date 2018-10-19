package me.ipodtouch0218.multiplayerpuyo.manager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.GameWindow;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.input.InputHandler;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.misc.Gamemodes;
import me.ipodtouch0218.multiplayerpuyo.misc.Gamemodes.Gamemode;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter.CharacterPose;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjCharacterSelect;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjCountdown;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoFeverBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjPuyoIceBoard;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjSPGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.senders.ParticleGarbageSender;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.GameMusic;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class PuyoBoardManager extends GameObject {
	
	// static sprites //
	private static final GameSprite[] pauseSprites = new GameSprite[3];
	private static final PauseButton[] pauseButtons = new PauseButton[5];
	{
		SpriteSheet buttons = new SpriteSheet("ui/board/buttons.png",136,40);
		pauseButtons[0] = new PauseButton(buttons.getSprite(0, 0), buttons.getSprite(1, 0), false) {
			void onClick() {}
		};
		pauseButtons[1] = new PauseButton(buttons.getSprite(0, 1), buttons.getSprite(1, 1), true) {
			void onClick() {
				countDown();
			}
		};
		pauseButtons[2] = new PauseButton(buttons.getSprite(0, 2), buttons.getSprite(1, 2), false) {
			void onClick() {
				gameOver();
			}
		};
		pauseButtons[3] = new PauseButton(buttons.getSprite(0, 3), buttons.getSprite(1, 3), false) {
			void onClick() {
				choosechars = true;
				countDown();
			}
		};
		pauseButtons[4] = new PauseButton(buttons.getSprite(0, 4), buttons.getSprite(1, 4), false) {
			void onClick() {
				gameOver();
				PuyoGameMain.getMenus().openPreviousPanel();
				PuyoGameMain.getMenus().openPreviousPanel();
			}
		};
	}
	{
		SpriteSheet sheet = new SpriteSheet("ui/board/pause.png", 64, 64);
		for (int x = 0; x < 3; x++) {
			pauseSprites[x] = sheet.getSprite(x, 0);
		}
	}
	private static final GameSprite background = new GameSprite("ui/background.png");
	////////////////////
	
	private Gamemode gamemode;
	private static boolean pause;

	//--player vars
	private ArrayList<ObjPuyoBoard> boards = new ArrayList<>();
	private PlayerManager controls;
	
	//--settings vars
	private int width;
	private int height;
	private boolean split;
	private boolean verticalFlip;
	private int puyoColors = 4; 
	private boolean doublenext = true;
	private boolean offset = true;
	private boolean instadrop = true;

	//--ingame round vars
	private boolean gameStarted;
	private ArrayList<PuyoType> nextPuyos;
	private HashMap<ObjPuyoBoard, Integer> validationNeeded = new HashMap<>();
	private double speedTimer = 0;
	private int dropSpeed;
	private boolean inFever;
	
	private ObjGarbageIndicator spIndicator;
	
	//--score vars
	private boolean gameOver;
	private int winsUntilVictory = 2;
	
	//--sound vars
	private GameMusic currentMusic = GameMusic.CLASSIC;
	
	private long musicResumePoint;
	private boolean isPanicing = false;
	
	//--key vars
	private boolean escdown = false;
	private boolean f5down = false;
	
	//////
	public PuyoBoardManager(PlayerManager controls, Gamemode gamemode) {
		this.controls = controls;
		this.gamemode = gamemode;
		GameRenderer.setBackground(background.getImage());
	}
	//////
	@Override
	public void tick(double delta) {
		if (gameOver) { return; }
		if (pause) { 
			sintimer+=delta*1.5d;
			pausedKeyCheck(delta);
			return; 
		}
		keyCheck();
		feverMusicCheck();
		panicMusicCheck();
	
		if (!gameStarted) { return; }
		
		speedTimer+=delta;
		dropSpeed = (int) Math.max(Math.min(30, ((speedTimer*-0.09)+30)), 5);
	
		gameOverCheck();
	}
	
	private void panicMusicCheck() {
		if (!inFever) {
			GameMusic.FEVER.stop();
			
			double highestPanic = 0;
			for (ObjPuyoBoard board : boards) {
				if (board.isGameOver()) { continue; }
				highestPanic = Math.max(board.getPanic(), highestPanic);
			}
			if (!isPanicing && highestPanic >= 0.75) {
				isPanicing = true;
				musicResumePoint = currentMusic.getSound().getFramePosition();
				currentMusic.stop();
				GameMusic.PANIC.start(0);
			}
			if (isPanicing && highestPanic <= 0.55) {
				isPanicing = false;
				GameMusic.PANIC.stop();
				currentMusic.start(musicResumePoint);
			}
		}
		
	}
	
	private void feverMusicCheck() {
		if (gamemode.isFever()) {
			
			if (currentMusic.getSound().isPlaying() || GameMusic.PANIC.getSound().isPlaying()) {
				for (ObjPuyoBoard b :  boards) {
					ObjPuyoFeverBoard fb = (ObjPuyoFeverBoard) b;
					if (fb.getFeverManager().isInFever()) {
						inFever = true;
						
						if (currentMusic.getSound().isPlaying()) {
							musicResumePoint = currentMusic.getSound().getFramePosition();
							currentMusic.stop();
						} else {
							GameMusic.PANIC.stop();
						}
						
						GameMusic.FEVER.start(0);
						break;
					}
				}
			} else {
				for (ObjPuyoBoard b :  boards) {
					ObjPuyoFeverBoard fb = (ObjPuyoFeverBoard) b;
					if (fb.getFeverManager().isInFever()) {
						inFever = true;
						break;
					}
				}
			}
			
		}
	}
	private double inputtimer = 0;
	private int selectedpause = 0;
	private void pausedKeyCheck(double delta) {
		Controls c = PlayerManager.getInstance().getMainControl();
		if (inputtimer > 0) {
			inputtimer -= delta;
			return;
		}
		boolean moved = false;
		if (c.menuUp) {
			selectedpause -= 1;
			rollover();
			while (!gamemode.isSingleplayer() && pauseButtons[selectedpause].singleplayerOnly) {
				selectedpause -= 1;
				rollover();
			}
			moved = true;
		} 
		if (c.menuDown) {
			selectedpause += 1;
			while (!gamemode.isSingleplayer() && pauseButtons[selectedpause].singleplayerOnly) {
				selectedpause += 1;
				rollover();
			}
			moved = true;
		}
		
		if (c.menuEnter) {
			pauseButtons[selectedpause].onClick();
			pause = false;
		}
		
		if (moved) {
			inputtimer = 0.15;
//			MenuSounds.MOVE.getSound().play();
		}
	}
	private void rollover() {
		if (selectedpause >= pauseButtons.length) {
			selectedpause = 0;
		} else if (selectedpause < 0) {
			selectedpause = pauseButtons.length-1;
		}
	}
	
	private void keyCheck() {
		boolean controlpause = PlayerManager.getInstance().getMainControl().pause;
		if (InputHandler.isKeyPressed(KeyEvent.VK_F5) && !f5down) {
			PuyoGameMain.screenshot();
		}
		f5down = InputHandler.isKeyPressed(KeyEvent.VK_F5);
		if (controlpause && !escdown) {
			pause = !pause;
		} 
		escdown = controlpause;
	}
	
	private void gameOverCheck() {
		if (gameStarted && (getRemainingBoards() <= (gamemode.isSingleplayer() ? 0 : 1))) {
			for (ObjPuyoBoard b : boards) {
				if (b.isMidChain() && !b.isGameOver()) {
					return;
					//dont gameover if mid combo
				}
			}
			gameOver = true;
			new Thread() {
				public void run() {
					gamemode.onRoundOver(PuyoBoardManager.this);
					for (ObjPuyoBoard b : boards) {
						if (b.getDropper() == null) { continue; }
						b.getDropper().delete();
						if (!b.isGameOver()) {
							b.poseCharacter(CharacterPose.WIN);
						}
					}
					
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
					boolean cont = true;
					while (cont) {
						cont = false;
						forloop:
						for (ObjPuyoBoard b : boards) {
							if (b.getCharacter().getSound(CharacterSounds.LOSE).isPlaying()) {
								cont = true;
								break forloop;
							}
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
					
					boolean sound = false;
					boolean winner = false;
					for (ObjPuyoBoard b : boards) {
						if (!b.isGameOver()) {
							b.setWins(b.getWins()+1);
							if (b.getWins() >= winsUntilVictory) {
								winner = true;
							}
							if (!sound) {
								sound=true;
								GameSounds.FEVER_METER_FILL.play();
							}
							b.getCharacter().getSound(CharacterSounds.WIN).play();
						}
					}
					
					cont = true;
					while (cont) {
						cont = false;
						forloop:
						for (ObjPuyoBoard b : boards) {
							if (b.getCharacter().getSound(CharacterSounds.WIN).isPlaying()) {
								cont = true;
								break forloop;
							}
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					
					if (winner || gamemode.isSingleplayer()) {
						try {
							Thread.sleep(2*1000);
						} catch (InterruptedException e) {}
						gameOver();
					} else {
						countDown();
					}
				}
			}.start();
		}
	}
	
	private void gameOver() {
		for (ObjPuyoBoard board : boards) {
			PuyoGameMain.getGameEngine().removeGameObject(board);
		}
		PuyoGameMain.getMenus().show();
		PuyoGameMain.getGameEngine().removeGameObject(spIndicator);
		PuyoGameMain.getGameEngine().removeGameObject(this);
		GameRenderer.removeBackground();
		Runtime.getRuntime().gc();
	}
	
	public ObjPuyoBoard createBoard(int width, int height, int player) {
		ObjPuyoBoard board = null;
		if (gamemode.isFever()) {
			board = new ObjPuyoFeverBoard(width, height, controls.getPlayer(player+1), this, player);
		} else if (gamemode == Gamemodes.ICE_PUYO) {
			board = new ObjPuyoIceBoard(width, height, controls.getPlayer(player+1), this, player);
		} else {
			board = new ObjPuyoBoard(width, height, controls.getPlayer(player+1), this, player);
		}
		addBoard(board);
		return board;
	}
	
	public void addBoard(ObjPuyoBoard board) {
		boards.add(board);
		width = board.getWidth();
		height = board.getHeight();
		PuyoGameMain.getGameEngine().addGameObject(board);
	}
	
	public void sendGarbage(ObjPuyoBoard board, int amount, boolean sendParticle, PuyoType type, int consecutive) {
		if (amount <= 0) { return; }
		if (gamemode.isSingleplayer()) {
			if (sendParticle) {
				PuyoGameMain.getGameEngine().addGameObject(new ParticleGarbageSender(spIndicator, amount, consecutive, type), board.getX()+(width*16)/2, board.getY()+(height*16)/2);
			}
			return;
		}
		
		int remainingBoards = getRemainingBoards();
		if (remainingBoards-1 <= 0) { return; } 

		if (split) {
			amount /= (remainingBoards-1);
		}
		if (sendParticle) {
			for (ObjPuyoBoard otherBoard : boards) {
				if (otherBoard == board) continue;
				PuyoGameMain.getGameEngine().addGameObject(new ParticleGarbageSender(otherBoard, amount, consecutive, type), board.getX()+(board.getWidth()*16)/2, board.getY()+(board.getHeight()*16)/2);
			}
		}
		int newamt = amount + validationNeeded.getOrDefault(board, 0);
		validationNeeded.put(board, newamt);
	}
	
	public PuyoType getNextPuyo(ObjPuyoBoard board) {
		boolean removeLast = true;
		for (ObjPuyoBoard entry : boards) {
			if (entry.puyoOrderPos < 1 && !entry.isGameOver()) removeLast = false;
		}
		if (removeLast) {
			nextPuyos.remove(0);
			for (ObjPuyoBoard entry : boards) {
				entry.puyoOrderPos--;
			}
		}
		while (nextPuyos.size() <= board.puyoOrderPos) {
			nextPuyos.add(getRandomPuyo());
		}
		PuyoType next = nextPuyos.get(board.puyoOrderPos);
		board.puyoOrderPos++;
		return next;
	}
	
	
	private PuyoType getRandomPuyo() {
		return PuyoType.values()[(int) Math.floor(Math.random()*puyoColors)];
	}
	
	private int getRemainingBoards() {
		int remainingBoards = boards.size();
		for (ObjPuyoBoard brd : boards) {
			if (brd.isGameOver()) remainingBoards--;
		}
		return remainingBoards;
	}
	
	private static final GameSprite[] countdownSprites = {new GameSprite("ui/board/countdown-3.png"), 
														  new GameSprite("ui/board/countdown-2.png"),
														  new GameSprite("ui/board/countdown-1.png"),
														  new GameSprite("ui/board/countdown-go.png")};
	private boolean choosechars = false;
	public void countDown() {
		if (!currentMusic.getSound().isPlaying()) {
			currentMusic.start(0);
		}
		nextPuyos = new ArrayList<>();
		new Thread() {
			public void run() {
				gameOver = false;
				GameRenderer.setActiveCamera(null);
				int count = 0;
				for (ObjPuyoBoard board : boards) {
					board.setLocation(GameWindow.getSetWidth()/2+(((double)count-boards.size()/2)*((width*16)+50)), 80);
					board.reset();
					board.createObjects();
					count++;
				}
				if (gamemode.isSingleplayer()) {
					ObjPuyoBoard spBoard = boards.get(0);
					spBoard.setLocation(GameWindow.getSetWidth()/2 - (width*16)+50, 80);
					spIndicator = PuyoGameMain.getGameEngine().addGameObject(new ObjSPGarbageIndicator(), GameWindow.getSetWidth()/2 + (width*16+50), 80+height*16);
				}
				if (boards.get(0).getCharacter() == null || choosechars) {
					ObjCharacterSelect sel = PuyoGameMain.getGameEngine().addGameObject(new ObjCharacterSelect(boards));
					while(!sel.allConfirmed()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
					choosechars = false;
					PuyoGameMain.getGameEngine().removeGameObject(sel);
				}

				gamemode.onRoundReset(PuyoBoardManager.this);
				
				ObjCountdown cdObject = PuyoGameMain.getGameEngine().addGameObject(new ObjCountdown(), GameWindow.getSetWidth()/2-16, GameWindow.getSetHeight()/4);
				
				for (int i = 0; i < 4; i++) {
					double second = 1;
					final int j = i;
					cdObject.setSprite(countdownSprites[j]);
					if (i == 3) {
						cdObject.setX(cdObject.getX());
					}
					while (second > 0) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {}
						second -= 0.05;
						while (pause) { 
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {}
						}
					}
				}
				cdObject.shrink();
				startGame();
			}
		}.start();
	}
	private void startGame() {
		gamemode.onRoundStart(this);
		boards.forEach(brd -> brd.setReadyForPuyo(true));
		gameStarted = true;
	}
	
	private double sintimer;
	@Override
	public void render(Graphics2D g) {
		if (pause) {
			g.setColor(new Color(0,0,0,127));
			g.fillRect(0, 0, GameWindow.getSetWidth(), GameWindow.getSetHeight());
			
			int centerX = (GameWindow.getSetWidth()/2), centerY = (GameWindow.getSetHeight()/5);
			g.drawImage(pauseSprites[0].getImage(), centerX-96, (int) (centerY+ (Math.sin(sintimer)*10d)), null);
			g.drawImage(pauseSprites[1].getImage(), centerX-52, (int) (centerY+ (Math.sin(sintimer-0.4)*10d)), null);
			g.drawImage(pauseSprites[2].getImage(), centerX+5, (int) (centerY+ (Math.sin(sintimer-0.8)*10d)), null);
			
			int count = 0;
			for (int i = 0; i < 5; i++) {
				if (!gamemode.isSingleplayer() && pauseButtons[i].singleplayerOnly) {
					continue;
				}
				GameSprite img = pauseButtons[i].sprite;
				if (selectedpause == i) {
					img = pauseButtons[i].selectedSprite;
				}
				g.drawImage(img.getImage(), centerX-70, centerY+100+(count*26), null);
				count++;
			}
		}
	}
	
	
	public boolean isNextDouble() { return doublenext; }
	public int getHeight() { return height; }
	public int getWidth() { return width; }
	public int getDropSpeed() { return dropSpeed; }
	public boolean getVerticalFlip() { return verticalFlip; }
	public static boolean isPaused() { return pause; }
	public List<ObjPuyoBoard> getBoards() { return boards; }
	public boolean isOffsetEnabled() { return offset; }
	public int getPuyoColorAmount() { return puyoColors; }
	public Gamemode getGamemode() { return gamemode; }
	public int getWinsUntilVictory() { return winsUntilVictory; }
	public boolean getInstaDrop() { return instadrop; }
	
	public void setOffset(boolean value) { offset = value; }
	public void setNextDouble(boolean value) { doublenext = value; }
	public void setVerticalFlip(boolean value) { verticalFlip = value; }
	public void setSplitGarbage(boolean value) { split = value; }
	public void setPuyoColorAmount(int value) { puyoColors = value; }

	public int getPlayersRemaining() {
		int count = 0;
		for (ObjPuyoBoard board : boards) {
			if (!board.isGameOver()) {
				count++;
			}
		}
		return count;
	}
	public int getPlayers() { 
		return boards.size();
	}
	public boolean hasStarted() { return gameStarted; }

	public void verifyGarbage(ObjPuyoBoard send) {
		int amt = validationNeeded.getOrDefault(send, 0);
		for (ObjPuyoBoard board : boards) {
			if (send == board) { continue; }
			board.addDroppableGarbage(amt);
		}
		validationNeeded.put(send, 0);
	}
	
	///---static---///
	private abstract class PauseButton {
		GameSprite sprite;
		GameSprite selectedSprite;
		boolean singleplayerOnly;
		PauseButton(GameSprite sprite, GameSprite selectedSprite, boolean singleplayer) {
			this.sprite = sprite;
			this.selectedSprite = selectedSprite;
			this.singleplayerOnly = singleplayer;
		}
		abstract void onClick();
	}
}
