package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteAnimation;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.manager.JoyconControls;
import me.ipodtouch0218.multiplayerpuyo.manager.LandingPuyoManager;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter.CharacterPose;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoInfo;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoRowInfo;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticlePuyoCleared;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ObjPuyoBoard extends GameObject {

	private static final GameSprite allClearSprite = new GameSprite("particles/all_clear.png");
	private static final SpriteSheet borderSheet = new SpriteSheet("ui/board/border.png", 106, 252);
	private static final SpriteSheet feverBoarderSheet = new SpriteSheet("ui/board/feverborder.png", 106, 252);
	private static final GameSprite[] borderSprites = new GameSprite[4];
	{
		for (int i = 0; i < 4; i++) {
			borderSprites[i] = borderSheet.getSprite(i, 0);
		}
	}
	private static final GameSprite[] feverBorderSprites = new GameSprite[4];
	{
		for (int i = 0; i < 4; i++) {
			feverBorderSprites[i] = feverBoarderSheet.getSprite(i,0);
		}
	}
	private static final GameSprite xSprite = new GameSprite("ui/board/x.png");
	private static final SpriteSheet stars = new SpriteSheet("ui/board/star.png", 11, 11);
	
	protected int[][] gameOverSpots = {{2,2}};
	
	protected PuyoBoardManager boardManager;
	protected int playerNumb;
	private PuyoCharacter character;

	public int puyoOrderPos;
	private PuyoInfo[][] board;
	private List<Integer[]> hide = new ArrayList<>();
	private int width, height;
	private Controls controls;
	private boolean readyForNewPuyo, gameOver;
	private double score;
	private int wins;
	private ObjGarbageIndicator garbage;
	private ObjNextDisplay display;
	private ObjDropper dropper;
	private LandingPuyoManager landThread;

	private boolean applyhide;
	private boolean allClear;
	private double allClearTimer = 1.25;
	
	private double xo, yo, rot, rotv;
	private boolean invisible;

	private int droppableGarbage = 0;

	private HashMap<Integer, PuyoRowInfo> rowInfo = new HashMap<>();
	protected List<Integer[]> flashing = new ArrayList<>();
	private double flashingTimer = 0;
	protected boolean midchain;
	
	private ObjCharacterPose characterpose;
	
	public ObjPuyoBoard(PuyoBoardManager bm, int playerNumb) {
		this(6, 12, bm, playerNumb);
	}

	public ObjPuyoBoard(Controls controls, PuyoBoardManager bm, int playerNumb) {
		this(6, 12, controls, bm, playerNumb);
	}

	public ObjPuyoBoard(int width, int height, PuyoBoardManager bm, int playerNumb) {
		this(width, height, new Controls(), bm, playerNumb);
	}

	public ObjPuyoBoard(int width, int height, Controls controls, PuyoBoardManager bm, int playerNumb) {
		this.width = width;
		this.height = height+2;
		board = createNewBoard(width, this.height);
		this.controls = controls;
		if (controls != null) {
			PuyoGameMain.getGameEngine().addGameObject(controls);
		}
		boardManager = bm;
		if (controls != null) {
			controls.setInGame(true);
		}
		for (int x = 0; x < width; x++) {
			PuyoRowInfo info = new PuyoRowInfo();
			rowInfo.put(x, info);
			info.rowSquish = 1;
			info.rowSquishTimer = 0;
			info.rowSquishIncrementAmount = 0;
		}
		this.playerNumb = playerNumb;
	}
	
	public void createObjects() {
		if (garbage == null) {
			garbage = PuyoGameMain.getGameEngine().addGameObject(new ObjGarbageIndicator(((double) playerNumb/(double) boardManager.getPlayers()) >= 0.5), this.x, this.y);
		}
		if (display == null && x != 0) {
			display = PuyoGameMain.getGameEngine().addGameObject(new ObjNextDisplay(boardManager.isNextDouble()), x + (width * 16) + 2, y + 16);
			display.addPuyoGroup(new PuyoType[]{boardManager.getNextPuyo(this), boardManager.getNextPuyo(this)});
			display.addPuyoGroup(new PuyoType[]{boardManager.getNextPuyo(this), boardManager.getNextPuyo(this)});
		}
	}
	
	
	@Override
	public void tick(double delta) {
		manageRowSquish(delta);
		
		if ((allClear && allClearTimer > 0) || (!allClear && allClearTimer != 1.25)) {
			allClearTimer -= 0.025;
		}
		if (allClearTimer <= -1.25) {
			allClearTimer = 1.25;
		}
		if (gameOver) {
			return;
		}
		if (readyForNewPuyo) {
			if (dropper != null) {
				PuyoGameMain.getGameEngine().removeGameObject(dropper);
				dropper = null;
			}
			for (int[] xspots : gameOverSpots) {
				if (getPuyoAt(xspots[0], xspots[1]).type != null) {
					gameOver();
					return;
				}
			}
			while (dropper == null || dropper.getBasePuyo() == null || dropper.getOtherPuyo() == null) {
				PuyoType[] puyos = display.getPuyoGroup();
				display.changeGroup();
				display.addPuyoGroup(new PuyoType[] {boardManager.getNextPuyo(this), boardManager.getNextPuyo(this)});

				PuyoGameMain.getGameEngine().removeGameObject(dropper);
				dropper = new ObjDropper(this, puyos[0], puyos[1]);
			}

			PuyoGameMain.getGameEngine().addGameObject(dropper);
			readyForNewPuyo = false;
			checkForFlashing(dropper.finalDropLocations());
			midchain = false;
		}

		if (flashingTimer > 30) {
			flashingTimer = -30;
		}
		flashingTimer+=delta*60d;
		
	}
	private void manageRowSquish(double delta) {
		for (int x = 0; x < width; x++) {
			PuyoRowInfo info = rowInfo.get(x);
			if (info.rowSquishTimer > 0) {
				info.rowSquishTimer--;
			}	
			if (info.rowSquish <= 1) {
				if (info.rowSquishTimer <= 0) {
					info.rowSquish += info.rowSquishIncrementAmount*60d*delta;
				} else {
					info.rowSquish -= info.rowSquishIncrementAmount*60d*delta;
				}
			}
		}
	}
	
	
	public void checkForFlashing(int[][] locs) {
		if (PuyoGameMain.quality != RenderQuality.HIGH) {
			return;
		}
		flashing.clear();
		if (dropper == null || dropper.isDisabled()) { return; }
		
		PuyoInfo[][] copiedboard = copyBoard();
		if (locs[0][1] >= 0) copiedboard[locs[0][0]][locs[0][1]].type = dropper.getBasePuyo();
		if (locs[1][1] >= 0) copiedboard[locs[1][0]][locs[1][1]].type = dropper.getOtherPuyo();
		
		ArrayList<Integer[]> matched = getPuyoCombo(locs[0][0], locs[0][1], dropper.getBasePuyo(), copiedboard);
		if (matchLength(matched,copiedboard) >= 4) {
			flashing.addAll(matched);
		}
		if (dropper.getBasePuyo() != dropper.getOtherPuyo()) {
			matched.clear();
			matched.addAll(getPuyoCombo(locs[1][0], locs[1][1], dropper.getOtherPuyo(), copiedboard));
			if (matchLength(matched,copiedboard) >= 4) {
				flashing.addAll(matched);
			}
		}
	}
	
	private int matchLength(ArrayList<Integer[]> matches, PuyoInfo[][] board) {
		int count = matches.size();
		for (Integer[] match : matches) {
			if (match[0] < 0 || match[1] < 0) { continue; }
			if (board[match[0]][match[1]].type == PuyoType.GARBAGE) { count--; }
		}
		return count;
	}
	
	private PuyoInfo[][] copyBoard() {
		PuyoInfo[][] newboard = new PuyoInfo[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				newboard[x][y] = board[x][y].clone();
			}
		}
		return newboard;
	}
	

	public void gameOver() {
		midchain = false;
		for (ObjPuyoBoard b : boardManager.getBoards()) {
			if (b == this) { continue; }
			if (!b.gameOver && b.midchain) {
				return;
			}
		}
		boardManager.getGamemode().onBoardGameOver(this);
		if (gameOver == true) {
			return;
		}
		readyForNewPuyo = false;
		if (dropper != null) {
			PuyoGameMain.getGameEngine().removeGameObject(dropper);
			dropper = null;
		}
		gameOver = true;
		new Thread() {
			int timer = 120;
			double xv = (Math.random() * 8d - 4d) / 8d;
			double yv = 0;
			double rot = xv / 1.3d;

			boolean side;
			public void run() {
				ObjPuyoBoard board = ObjPuyoBoard.this;
				if (rotv != 0) {
					side = true;
					xv = 12;
					if ((double) playerNumb/(double) boardManager.getPlayers() < 0.5);
					xv = -xv;
					rot = 25;
					clearBoard(true);
				}
				board.rot = 0;
				while (timer-- > 0) {
					if (timer == 60) {
						poseCharacter(CharacterPose.LOSE);
					}
					try {
						Thread.sleep((long) (1000 * (1d / 60d)));
						board.xo += xv;
						if (!side) {
							yv += (1.4d) / 8d;
							board.yo += yv;
						}
						board.rot += rot;
					} catch (InterruptedException e) {
					}
				}
				board.invisible = true;
			}
		}.start();
		if (controls instanceof JoyconControls) {
			((JoyconControls) controls).getJoycon().vibrate(false, (byte) 4, true, (byte) 15);
		}
		GameSounds.LOSE.play();
		new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {}
				CharacterSounds.LOSE.getSound(character).play();
			}
		}.start();
	}

	public void clearBoard(boolean particles) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (getPuyoAt(x, y).type != null && particles) {
					PuyoGameMain.getGameEngine().addGameObject(new ParticlePuyoCleared(x, y-2, this, getPuyoAt(x, y).type));
				}
				setPuyoAt(x, y, null);
				setIcy(x,y,false);
			}
		}
	}

	protected FeverBackground backgroundColor = FeverBackground.values()[(int) (Math.random() * FeverBackground.values().length)];
	protected boolean backgroundInChain = false;
	
	private VolatileImage renderImage = null;
	@Override
	public void render(Graphics2D g) {
		if (renderImage == null || renderImage.validate(GameRenderer.gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
			 renderImage = GameRenderer.createVolatile(width*16 + 8, (height-2)*16 + 8, false);
		}
		Graphics2D bg = renderImage.createGraphics();
		bg.setComposite(AlphaComposite.Clear);
		bg.fillRect(0,0,width*16+8,(height-2)*16+8);
		bg.setComposite(AlphaComposite.Src);
		g.setFont(PuyoGameMain.scorefont);
		
		
		if (!invisible) {
			if (allClear || allClearTimer != 1.25) {
				bg.drawImage(
						allClearSprite.getImage().getScaledInstance(allClearSprite.getImage().getWidth() * 2,
								allClearSprite.getImage().getHeight() * 2, Image.SCALE_FAST),
						((width / 2) * 16) + 4 - allClearSprite.getImage().getWidth(),
						(int) ((((height-2) / 4) * 16) + 4 + (Math.tan(allClearTimer) * 1 / 4) * 1000), null);
			}

			xloop:
			for (int xBoard = width - 1; xBoard >= 0; xBoard--) {
				if (PuyoBoardManager.isPaused()) { break xloop; }
				
				PuyoRowInfo xRowInfo = rowInfo.get(xBoard);
				if (xRowInfo.renderImage == null) {
					xRowInfo.renderImage = GameRenderer.createVolatile(16, (height-2)*16, true);
				}
				
				VolatileImage rowImage = xRowInfo.renderImage;
				Graphics2D rowG = rowImage.createGraphics();
				
				rowG.setComposite(AlphaComposite.Clear);
				rowG.fillRect(0, 0, 16, (height-2)*16);
				rowG.setComposite(AlphaComposite.Src);
				int xCoordinate = xBoard * 16;
				yLoop: for (int yBoard = height-1; yBoard >= 2; yBoard--) {
					int yCoordinate = yBoard * 16;
					PuyoInfo info = board[xBoard][yBoard];
					if (info.type == null) {
						break yLoop;
					}
					
					boolean popsprite = false;

					if (!info.ice) {
						if (hide != null) {
							for (Integer[] flashes : hide) {
								if (flashes[0] == xBoard && flashes[1] == yBoard) {
									if (applyhide) {
										continue yLoop;
									} else {
										popsprite = true;
									}
								}
							}
						}
					}

					int connected = 0;
					if (!info.ice) {
						if (isInBounds(xBoard + 1, yBoard) && !board[xBoard + 1][yBoard].ice && board[xBoard + 1][yBoard].type == info.type) connected += 1; // connected to the right
						if (isInBounds(xBoard - 1, yBoard) && !board[xBoard - 1][yBoard].ice && board[xBoard - 1][yBoard].type == info.type) connected += 2; // connected to the left
						if (isInBounds(xBoard, yBoard + 1) && !board[xBoard][yBoard + 1].ice && board[xBoard][yBoard + 1].type == info.type) connected += 4; // connected downwards
						if (yBoard-1 >= 2 && isInBounds(xBoard, yBoard - 1) && !board[xBoard][yBoard - 1].ice && board[xBoard][yBoard - 1].type == info.type) connected += 8; // connected upwards
					}

					PuyoSprites spr = PuyoType.PuyoSprites.BASE;
					if (popsprite) {
						spr = PuyoType.PuyoSprites.POPPING;
					} else {
						spr = PuyoType.PuyoSprites.values()[connected];
					}
					
					boolean flash = false;
					if (PuyoGameMain.quality == RenderQuality.HIGH) {
						if (!info.ice) {
							for (Integer[] flashers : flashing) {
								if (flashers[0] == xBoard && flashers[1] == yBoard) {
									flash = true;
									break;
								}
							}
						}
					}
					
					renderPuyo(rowG, info, spr, (yCoordinate-32), flash);
				}
				double squishAmount = rowInfo.get(xBoard).rowSquish;
				if (squishAmount > 1) {
					rowInfo.get(xBoard).rowSquish = 1;
					squishAmount = 1;
				}
				Image squished = rowImage;
				if (PuyoGameMain.quality == RenderQuality.HIGH) {
					squished = rowImage.getScaledInstance(16, (int) ((double) (((height-2)*16)-48) + (squishAmount*48)), Image.SCALE_FAST);
				} else {
					squishAmount = 1;
				}
				bg.drawImage(squished, xCoordinate + 4, (height-2)*16 - (int) ((double) (((height-2)*16)-48) + (squishAmount*48) - 4), null);
			}
			
			renderXSpots(bg);
			
			if (gameOver) {
				bg.setColor(Color.BLACK);
				bg.setComposite(AlphaComposite.SrcAtop);
				bg.fillRect(0, 0, width*16+8, (height-2)*16+8);
				bg.setComposite(AlphaComposite.Src);
			}
		}
		
		renderBackground(bg);
		
		double flipTimer = 0;
		if (this instanceof ObjPuyoFeverBoard) {
			
			flipTimer = ((ObjPuyoFeverBoard) this).getFlipTimer();
			
		}
		if (flipTimer != 0) {
			double flipFactor = Math.abs(0.5-(flipTimer))*2d;
			Image scaledImg = renderImage.getScaledInstance((int) ((width*16) * flipFactor) + 1,
					(height-2)*16+8, Image.SCALE_FAST);
			g.drawImage(scaledImg, (int) x + (width *16 / 2 - scaledImg.getWidth(null) / 2) - 4, (int) y,
					null);
		} else if (gameOver && rot != 0) {
			AffineTransform at = new AffineTransform();
			at.translate((x+xo), (y+yo));
			at.rotate(Math.toRadians(rot), width*16/2, (height-2)*16/2); 
			
			g.drawImage(renderImage, at, null);
		} else if (rotv != 0) {
			rot += rotv/7d;
			
			AffineTransform at = new AffineTransform();
			at.translate(x-8, y);
			at.rotate(Math.toRadians(rot), width*16/2, (height-2)*16); 
			
			if (rot >= 0.3) {
				rotv -= 1.2;
			} else if (rot < -0.3) {
				rotv += 1.2;
			} else if (rotv > -0.3 && rotv < 0.3) {
				rotv = 0.01;
				rot = 0;
			}
			rotv*=0.95;
			
			g.drawImage(renderImage, at, null);
		} else {
			g.drawImage(renderImage, (int) x - 8, (int) y, null);
		}
		
		renderBorder(g);
		renderScoreAndWins(g);
	}
	private void renderXSpots(Graphics2D g) {
		g.setComposite(AlphaComposite.DstOver);
		for (int[] spots : gameOverSpots) {
			g.drawImage(xSprite.getImage(), spots[0]*16+4, (spots[1]-2)*16+3, null);
		}
		g.setComposite(AlphaComposite.Src);
	}
	private static AffineTransform flipTransform = AffineTransform.getScaleInstance(-1,1);
	{
		if (flipTransform.getTranslateX() == 0) { 
			flipTransform.translate(-96-5, 3);
		}
	}
	private void renderBackground(Graphics2D g) {
		g.setComposite(AlphaComposite.DstOver);
		if (character == null) {
			g.setColor(new Color(78,78,78));
			g.fillRect(5, 3, width*16, (height-2)*16);
			return;
		}
		
		Image bgImage = character.getBackground().getImage();
		boolean feverbg = false;
		if (this instanceof ObjPuyoFeverBoard) {
			ObjPuyoFeverBoard t = (ObjPuyoFeverBoard) this;
			if (t.getFeverManager().isInFever()) {
				if (backgroundInChain) {
					bgImage = backgroundColor.getDuringChain().getImage();
				} else {
					bgImage = backgroundColor.getNoChain().getImage();
				}
				feverbg = true;
			}
		}
		
		if (flip == 1 && !feverbg) {
			g.drawImage(bgImage, flipTransform, null);
		} else {
			g.drawImage(bgImage, 5, 3, width*16, (height-2)*16, null);
		}
		g.setComposite(AlphaComposite.Src);
	}
	protected void renderPuyo(Graphics2D img, PuyoInfo info, PuyoSprites shape, int ycoordinate, boolean flash) {
		img.setComposite(AlphaComposite.Src);
		
		Image image = info.type.getSprite(shape).getImage();
		img.drawImage(image, 0, ycoordinate, null);
		
		if (flash && PuyoGameMain.quality == RenderQuality.HIGH) {
			img.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.min(0.49, Math.abs(flashingTimer/60d))));
			img.setColor(Color.WHITE);
			img.fillRect(0, ycoordinate, 16, 16);
		}
	}
	private AffineTransform tx;
	private int flip = -1;
	private void renderBorder(Graphics2D g) {
		if (flip == -1) {
			if (((double) playerNumb / (double) boardManager.getPlayers()) >= 0.5) {
				flip = 1;
			} else {
				flip = 0;
			}
		}
		GameSprite[] bset = ((this instanceof ObjPuyoFeverBoard) ? feverBorderSprites : borderSprites);
		GameSprite border = bset[Math.min(bset.length-1, playerNumb)];
		VolatileImage borderImg = border.getImage();
		
		if (flip == 1) {
			if (tx == null) {
				if (x != 0) {
					tx = AffineTransform.getScaleInstance(-1, 1);
					tx.translate(-x+8-borderSprites[0].getImage().getWidth(), y-33-8);
				} else {
					return;
				}
			}
			g.drawImage(borderImg, tx, null);
		} else {
			g.drawImage(borderImg, (int) (x-8), (int) (y-33-8), null);
		}
	}
	private void renderScoreAndWins(Graphics2D g) {
		String scoreDisplay = "";
		for (int i = 8- (((int) score) + "").length(); i > 0; i--) {
			scoreDisplay += "0";
		}
		scoreDisplay += (int) score;
		g.drawString(scoreDisplay, (int) x - (boardManager.getGamemode().isSingleplayer() ? 0 : 9), (int) (y+((height-2) * 16) + 16));
		if (!boardManager.getGamemode().isSingleplayer()) {
			for (int i = 0; i < boardManager.getWinsUntilVictory(); i++) {
				GameSprite drawSprite = null;
				if (i < wins) {
					drawSprite = stars.getSprite(1,0);
				} else {
					drawSprite = stars.getSprite(0,0);
				}
				g.drawImage(drawSprite.getImage(), (int) x + 76 + (i*12), (int) (y+((height-2) * 16) + 5), null);
			}
		}
	}

	public void onPuyoLand(boolean instaDrop) {
		flashing.clear();
		landThread = new LandingPuyoManager(this);
		if (dropper != null) {
			dropper.disable();
		}
		midchain = true;
		new Thread(landThread).start();
	}

	ArrayList<Integer[]> checkedSpots = new ArrayList<>();
	
	public boolean checkForMatches(LandingPuyoManager manager) {
		checkedSpots = new ArrayList<>();
		ArrayList<Integer[][]> toPop = new ArrayList<>();
		for (int xPos = 0; xPos < width; xPos++) {
			yLoop: for (int yPos = 2; yPos < height; yPos++) {
				for (Integer[] checks : checkedSpots) {
					if (checks[0] == xPos && checks[1] == yPos)
						continue yLoop;
				}

				PuyoInfo info = getPuyoAt(xPos, yPos);
				if (info.type == null || info.type == PuyoType.GARBAGE || info.ice) {
					continue yLoop;
				}

				ArrayList<Integer[]> combo = getPuyoCombo(xPos, yPos, info.type, board);
				combo.forEach(inte -> checkedSpots.add(inte));

				int comboLength = 0;
				for (Integer[] com : combo) {
					if (getPuyoAt(com[0], com[1]).type != PuyoType.GARBAGE && !getPuyoAt(com[0], com[1]).ice)
						comboLength++;
				}
				if (comboLength >= 4) {
					toPop.add(combo.toArray(new Integer[][] {}));
				}
			}
		}
		if (toPop.isEmpty()) {
			return false;
		}
		ArrayList<Integer[]> popAtOnce = new ArrayList<>();
		for (Integer[][] pops : toPop) {
			Arrays.asList(pops).forEach(inte -> popAtOnce.add(inte));
		}
		manager.popPuyos(popAtOnce, ((this instanceof ObjPuyoFeverBoard) && ((ObjPuyoFeverBoard) this).getFeverManager().isInFever()));
		if (landThread != null) {
			landThread.matches = toPop;
		}
		return true;
	}

	private int[][] offsets = {{1,0},{-1,0},{0,1},{0,-1}};

	public ArrayList<Integer[]> getPuyoCombo(int posX, int posY, PuyoType pType, PuyoInfo[][] board) {
		ArrayList<Integer[]> currentCombo = new ArrayList<>();
		getNearbyPuyos(posX, posY, pType, currentCombo, board);
		return currentCombo;
	}

	private void getNearbyPuyos(int posX, int posY, PuyoType type, ArrayList<Integer[]> currentCombo, PuyoInfo[][] board) {
		if (posY < 2) { return; }
		for (Integer[] locs : currentCombo) {
			if (locs[0] == posX && locs[1] == posY)
				return;
		}
		currentCombo.add(new Integer[] { posX, posY });

		for (int[] offset : offsets) {
			int checkX = posX + offset[0], checkY = posY + offset[1];
			PuyoInfo checkType = getPuyoAt(checkX, checkY, board);
			if (checkType == null || checkY < 2) {
				continue;
			}
			if (checkType.type == type && !checkType.ice) {
				getNearbyPuyos(checkX, checkY, type, currentCombo, board);
			}
			if (checkType.type == PuyoType.GARBAGE || checkType.ice) {
				currentCombo.add(new Integer[] { checkX, checkY });
			}
		}
	}

	public void reset() {
		clearBoard(false);
		
		if (display != null) GameEngine.getInstance().removeGameObject(display);
		display = null;
		if (dropper != null) GameEngine.getInstance().removeGameObject(dropper);
		dropper = null;
		if (characterpose != null) GameEngine.getInstance().removeGameObject(characterpose);
		characterpose = null;
		
//		score = 0;
		gameOver = false;
		if (garbage != null) garbage.setOverallGarbage(0);
		readyForNewPuyo = false;
		xo = 0;
		yo = 0;
		rot = 0;
		rotv = 0;
		invisible = false;
		droppableGarbage = 0;
		puyoOrderPos = 0;
//		boardManager.getGamemode().applyBoardEffects(this);
	}

	@Override
	public void onRemove() {
		readyForNewPuyo = false;
		PuyoGameMain.getGameEngine().removeGameObject(dropper);
		PuyoGameMain.getGameEngine().removeGameObject(garbage);
		PuyoGameMain.getGameEngine().removeGameObject(display);
		PuyoGameMain.getGameEngine().removeGameObject(characterpose);
		if (controls != null) {
			controls.setInGame(false);
		}
	}

	public void garbageShake() {
		boolean shake = false;
		for (int[] pos : gameOverSpots) {
			PuyoInfo info = getPuyoAt(pos[0], pos[1]);
			if (info.type != null) { shake = true; break; }
		}
		if (readyForNewPuyo && shake) {
			rot = -1;
			rotv = -12;
			GameSounds.GARBAGE_DROP_1.play();
		}
	}
	
	public void poseCharacter(CharacterPose chain) {
		if (character == null) { return; }
		if (characterpose != null) {
			if (characterpose.exists()) {
				GameEngine.getInstance().removeGameObject(characterpose);
			}
			characterpose = null;
		}
		
		characterpose = GameEngine.getInstance().addGameObject(new ObjCharacterPose(character, chain), x+4, 360);
	}
	
	// ---Setters---//
	public void setPuyoAt(int x, int y, PuyoType mainType) {
		if (!isInBounds(x, y))
			return;
		board[x][y].type = mainType;
	}
	public void setIcy(int x, int y, boolean value) {
		if (!isInBounds(x, y)) {
			return;
		}
		PuyoInfo inf = board[x][y];
		if (value && !inf.ice) {
			inf.ice = true;
			inf.icetimer = 2;
		}
		if (!value) {
			inf.ice = false;
			inf.icetimer = 0;
		}
	}

	public void setReadyForPuyo(boolean value) {
		readyForNewPuyo = value;
	}

	public void hide(List<Integer[]> value) {
		hide = value;
	}

	public void addScore(double toAdd) {
		score += toAdd;
	}

	public void setControls(Controls value) {
		this.controls = value;
	}
	
	public void setDropper(ObjDropper object) {
		dropper = object;
	}

	public void applyhide(boolean value) {
		applyhide = value;
	}

	public void setAllClear(boolean value) {
		allClear = value;
		if (value) {
			allClearTimer = 1.25;
		}
	}
	
	public void setDroppableGarbage(int garbage) {
		this.droppableGarbage = garbage;
	}

	public void setBoard(PuyoInfo[][] newBoard) {
		board = newBoard;
	}
	public void setWins(int amount) {
		this.wins = amount;
	}
	public void setCharacter(PuyoCharacter chara) {
		character = chara;
	}

	// ---Getters---//
	public PuyoInfo getTopPuyoAt(int x) {
		return getPuyoAt(x, getTopPuyoLocAt(x));
	}

	public int getTopPuyoLocAt(int x) {
		for (int i = 0; i < height; i++) {
			if (getPuyoAt(x, i).type != null) {
				return i;
			}
		}
		return -1;
	}
	public PuyoInfo getPuyoAt(int x, int y) {
		if (!isInBounds(x, y))
			return null;
		return board[x][y];
	}
	public PuyoInfo getPuyoAt(int x, int y, PuyoInfo[][] board2) {
		if (!isInBounds(x, y))
			return null;
		return board2[x][y]; 
	}
	public boolean isInBounds(int x, int y) { return !(x < 0 || width <= x || y < 0 || height <= y); }
	public Controls getControls() { return controls; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public PuyoInfo[][] getBoard() { return board; }
	public int getScore() { return (int) score; }
	public ObjGarbageIndicator getGarbageIndicator() { return garbage; }
	public PuyoBoardManager getBoardManager() { return boardManager; }
	public boolean isGameOver() { return gameOver; }
	public boolean isReadyForPuyo() { return readyForNewPuyo; }
	public ObjNextDisplay getNextDisplay() { return display; }
	public ObjDropper getDropper() { return dropper; }
	public boolean hasAllClear() { return allClear; }
	public void squishRow(int row, double amount, double d) {
		PuyoRowInfo info = rowInfo.get(row);
		info.rowSquishIncrementAmount = amount;
		info.rowSquishTimer = d;
	}
	public int getDroppableGarbage() { return droppableGarbage; }
	public void addDroppableGarbage(int amount) { droppableGarbage = Math.max(0, droppableGarbage+amount); }
	public PuyoCharacter getCharacter() { return character; }
	public int getWins() { return wins; }
	public boolean isMidChain() { return midchain; }
	public int getPlayer() { return playerNumb; }
	 
	
	public double getPanic() {
		if (board == null) { return 0; }
		int count = 0;
		int maxsize = width*height;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (board[x][y].type != null) { count++; }
			}
		}
		if (garbage != null) {
			count += Math.min(30, garbage.getOverallGarbage());
		}
		if (landThread != null) {
			count += landThread.getFallingPuyos().size();
		}
		
		return ((double) count/(double) maxsize);
	}

	// ------------------------//
	private static SpriteSheet sheet = new SpriteSheet("ui/board/background/fever/fever_backgrounds.png", 78, 132);
	private static int loadAmt = 0;
	public static enum FeverBackground {
		RED(1), YELLOW(2), GREEN(3), BLUE(0), PURPLE(4);
		
		private SpriteAnimation noChain;
		private SpriteAnimation duringChain;
		
		FeverBackground(int x) {
			noChain = new SpriteAnimation(new GameSprite[]{sheet.getSprite(x,0), sheet.getSprite(x, 1)}, 20);
			duringChain = new SpriteAnimation(new GameSprite[]{sheet.getSprite(x,2),sheet.getSprite(x,3)}, 20);
			
			noChain.start(true);
			duringChain.start(true);
			loadAmt++;
			if (loadAmt >= 5) {
				sheet.close();
			}
		}

		public GameSprite getNoChain() {
			return noChain.getCurrentFrame();
		}

		public GameSprite getDuringChain() {
			return duringChain.getCurrentFrame();
		}
	}
	public static PuyoInfo[][] createNewBoard(int i, int j) {
		PuyoInfo[][] newBoard = new PuyoInfo[i][j];
		for (int x = 0; x < i; x++) {
			for (int y = 0; y < j; y++) {
				newBoard[x][y] = new PuyoInfo();
			}
		}
		return newBoard;
	}
}
