package me.ipodtouch0218.multiplayerpuyo;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;

import me.ipodtouch0218.java2dengine.GameEngine;
import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.GameWindow;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.PlayerManager;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;
import me.ipodtouch0218.multiplayerpuyo.menu.MenuManager;
import me.ipodtouch0218.multiplayerpuyo.menu.MenuPanel;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.LoadingBar;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.MenuArrows;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.MenuButton;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.MenuCheckbox;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.MenuElement;
import me.ipodtouch0218.multiplayerpuyo.menu.elements.MenuItemArrows;
import me.ipodtouch0218.multiplayerpuyo.misc.Gamemodes;
import me.ipodtouch0218.multiplayerpuyo.misc.Gamemodes.Gamemode;
import me.ipodtouch0218.multiplayerpuyo.misc.LoadingListener;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjCharacterSelect;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjCountdown;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjGarbageIndicator.GarbageSprites;
import me.ipodtouch0218.multiplayerpuyo.objects.ObjTextDisplay;
import me.ipodtouch0218.multiplayerpuyo.objects.particle.ParticleFeverIcon;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;
import me.ipodtouch0218.multiplayerpuyo.sound.Soundpack;

public class PuyoGameMain {
	
	private static GameEngine engine;
	public static Font puyofont;
	public static Font scorefont;
	private static MenuManager menus;
	private static PlayerManager playerManager;
	
	public static RenderQuality quality  = RenderQuality.HIGH;
	
	public static void main(String[] args) {
		try {
			puyofont = Font.createFont(Font.TRUETYPE_FONT, PuyoGameMain.class.getResourceAsStream("/res/sprites/ui/puyo.ttf")).deriveFont(12f);
			scorefont = Font.createFont(Font.TRUETYPE_FONT, PuyoGameMain.class.getResourceAsStream("/res/sprites/ui/score.ttf")).deriveFont(12f);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		new PuyoGameMain();
	}
	//-----//
	
	public PuyoGameMain() {
		System.setProperty("sun.java2d.opengl", "true");
		engine = new GameEngine(60);	
		
		GameWindow.setWindowName("Puyo Puyo: Java");
		GameWindow.setWindowSize(640, 360);
		GameWindow.setScaleSize(2, 2);
		engine.start();
		
		loadSoundsAndSprites();
		GameWindow.center();
		
		playerManager = new PlayerManager();
		engine.addGameObject(playerManager);
		playerManager.configure(1);
		
		menus = new MenuManager();
		addMenuElements();
		menus.openNewPanel(menus.getPanelFromString("main_menu"));
		engine.addGameObject(menus);
		
		GameRenderer.setRenderPriority(ObjCharacterSelect.class, ObjTextDisplay.class, ParticleFeverIcon.class, PlayerManager.class, ObjCountdown.class, PuyoBoardManager.class, ObjGarbageIndicator.class);
		GameSounds.setSoundpack(Soundpack.CLASSIC);
		
		
		engine.addGameObject(new ObjTextDisplay("", 0, 0, -1) {
			public void tick(double delta) {
				double fps = Math.round(engine.getFPS()*10d)/10d;
				setDisplay(fps + " fps");
				double sc = (engine.getMaxFPS() == 0 ? 60 : engine.getMaxFPS());
				fps = Math.min(fps,sc);
				setColor(new Color((int) (127+(128d*(fps/sc))), (int) (255*(fps/sc)), (int) (255*(fps/sc))));
			}
		}, 45, -5);
	}
	
	private void loadSoundsAndSprites() {
		int values = 4;
		
		LoadingBar masterBar = new LoadingBar(values, 0);
		LoadingBar middleBar = new LoadingBar(0, 0);
		masterBar.setLength((GameWindow.getSetWidth()/2));
		middleBar.setLength((GameWindow.getSetWidth()/2));
		masterBar.setHeight(20);
		middleBar.setHeight(20);
		ObjTextDisplay textDisplay = new ObjTextDisplay("", 0, 0, -1, 12f, Color.WHITE);
		engine.addGameObject(masterBar, (GameWindow.getSetWidth()-masterBar.getLength())/2, (GameWindow.getSetHeight())/2);
		engine.addGameObject(middleBar, (GameWindow.getSetWidth()-middleBar.getLength())/2, (GameWindow.getSetHeight())/2+50);
		engine.addGameObject(textDisplay, GameWindow.getSetWidth()/2, GameWindow.getSetHeight()/2+100);
		
		for (PuyoType types : PuyoType.values()) {
			middleBar.setMaxValue(PuyoSprites.values().length);
			middleBar.setValue(0);
			types.loadSprites(new LoadingListener() {
				public void loaded(String whatLoaded) {
					middleBar.setValue(middleBar.getValue()+1);
					textDisplay.setDisplay(whatLoaded);
				}
			});
		}
		masterBar.setValue(masterBar.getValue()+1);
		for (Soundpack packs : Soundpack.values()) {
			middleBar.setMaxValue(GameSounds.values().length);
			middleBar.setValue(0);
			packs.loadPack(new LoadingListener() {
				public void loaded(String whatLoaded) {
					middleBar.setValue(middleBar.getValue()+1);
					textDisplay.setDisplay(whatLoaded);
				}
			});
		}
		masterBar.setValue(masterBar.getValue()+1);
		middleBar.setMaxValue(GarbageSprites.values().length);
		middleBar.setValue(0);
		ObjGarbageIndicator.GarbageSprites.load(new LoadingListener() {
			public void loaded(String whatLoaded) {
				middleBar.setValue(middleBar.getValue()+1);
				textDisplay.setDisplay(whatLoaded);
			}
		});
		
		engine.removeGameObject(masterBar);
		engine.removeGameObject(middleBar);
		engine.removeGameObject(textDisplay);
	}
	
	private void applySettings(PuyoBoardManager bm) {
		bm.setVerticalFlip(verticalFlip.getValue());
		bm.setNextDouble(doubleNextDisplay.getValue());
	}
	
	private MenuArrows boardWidth = new MenuArrows((250/2)-96, 40, "Board Width:", 3, 20, 6, 135);
	private MenuArrows boardHeight = new MenuArrows((250/2)-96, 90, "Board Height:", 2, 30, 12, 135);
	private MenuCheckbox verticalFlip = new MenuCheckbox(450, 40, "Vertical Turning", 140, true);
	private MenuCheckbox invertControls = new MenuCheckbox(450, 160-70, "Invert Controls", 140, false);
	private MenuCheckbox doubleNextDisplay = new MenuCheckbox(450, 230-90, "Double Next Display", 140, true);
	private void addMenuElements() {	
		
		menus.addPanel(new MenuPanel("options_panel", false) {
			public void createElements() {
				elements = new MenuElement[2][5];
				
				elements[0][0] = boardWidth;
				elements[0][1] = boardHeight;
				elements[0][2] = new MenuItemArrows<RenderQuality>((250/2)-96, 140, 1, 135, RenderQuality.values()) {
					public void onValueChange() {
						PuyoGameMain.quality = RenderQuality.values()[value];
					}
				};
				elements[0][3] = new MenuArrows((250/2)-96, 190, "Max FPS:", 0, 300, 60, 135, 5) {
					public void onValueChange() {
						engine.setMaxFps(value);
						if (value == 0) {
							display = "Max FPS: Unlimited";
						} else {
							display = "Max FPS:";
						}
					}
				};
				
				elements[1][0] = verticalFlip;
				elements[1][1] = invertControls;
				elements[1][2] = doubleNextDisplay;
				
				elements[0][4] = new MenuButton(GameWindow.getSetWidth()/2-50, 260, new GameSprite("ui/menu/back-wide-selected.png"), new GameSprite("ui/menu/back-wide-deselected.png")) {
					public void onClick() {
						menus.openPreviousPanel();
					}
				};
				elements[1][3] = elements[0][4];
				elements[1][4] = elements[0][4];
			}
		});
		
		menus.addPanel(new MenuPanel("singleplayer_panel", false) {
			public void createElements() {
				elements = new MenuElement[2][2];
				
				MenuItemArrows<Gamemode> gamemode = new MenuItemArrows<Gamemode>((250/2)-97, 100, 0, 150, new Gamemode[]{Gamemodes.ENDLESS_PUYO,Gamemodes.ENDLESS_FEVER});
				elements[0][0] = gamemode;
				elements[1][0] = gamemode;
				
				elements[0][1] = new MenuButton((250/2)-64, 150, new GameSprite("ui/menu/main/button-start-selected.png"), new GameSprite("ui/menu/main/button-start.png")) {
					public void onClick() {
						Gamemode gm = gamemode.getValue();
						PuyoBoardManager bm = new PuyoBoardManager(playerManager, gm);
						PuyoGameMain.getGameEngine().addGameObject(bm);
						gm.createBoards(bm, 1, boardWidth.getValue(), boardHeight.getValue());
						bm.countDown();
						menus.hide();
					}
				};
				
				elements[1][1] = new MenuButton((250/2), 150, new GameSprite("ui/menu/back-wide-selected.png"), new GameSprite("ui/menu/back-wide-deselected.png")) {
					public void onClick() {
						menus.openPreviousPanel();
					}
				};
			}
		});
		
		menus.addPanel(new MenuPanel("multiplayer_panel", false) {
			public void createElements() { 
				elements = new MenuElement[2][4];
				
				MenuItemArrows<Gamemode> gamemode = new MenuItemArrows<Gamemode>((250/2)-97, 100, 1, 150, new Gamemode[]{Gamemodes.TSU, Gamemodes.FEVER, Gamemodes.NONSTOP_FEVER, Gamemodes.ICE_PUYO});
				elements[0][0] = gamemode;
				elements[1][0] = gamemode;
				
				MenuArrows players = new MenuArrows((250/2)-(97), 150, "Players:", 2, 5, 2, 110);
				elements[0][1] = players;
				elements[1][1] = players;
				
				MenuCheckbox splitting = new MenuCheckbox((250/2), 200, "Garbage Splitting", 120, true);
				elements[0][2] = splitting;
				elements[1][2] = splitting;
			
				
				
				elements[0][3] = new MenuButton((250/2)-52, 250, new GameSprite("ui/menu/main/button-start-selected.png"), new GameSprite("ui/menu/main/button-start.png")) {
					public void onClick() {
						
						if (PlayerManager.getInstance().getPlayers() < players.getValue()) {
							PlayerManager.getInstance().configure(players.getValue());
						}
						
						new Thread() {
							public void run() {
								while (PlayerManager.getInstance().isConfiguring()) {
									try {
										Thread.sleep(1);
									} catch (InterruptedException e) {}
								}
								
								if (PlayerManager.getInstance().getPlayers() < players.getValue()) {
									return;
								}
								
								Gamemode gm = gamemode.getValue();
								PuyoBoardManager bm = new PuyoBoardManager(playerManager, gm);
								gm.createBoards(bm, players.getValue(), boardWidth.getValue(), boardHeight.getValue());
								PuyoGameMain.getGameEngine().addGameObject(bm);
								applySettings(bm);
								bm.setSplitGarbage(splitting.getValue());
								bm.countDown();
								menus.hide();
							}
						}.start();
					}
				};
				
				elements[1][3] = new MenuButton((250/2), 250, new GameSprite("ui/menu/back-wide-selected.png"), new GameSprite("ui/menu/back-wide-deselected.png")) {
					public void onClick() {
						menus.openPreviousPanel();
					}
				};
			}
		});
		
		menus.addPanel(new MenuPanel("start_panel", true) {
			public void createElements() {
				elements = new MenuElement[2][2];
				
				elements[0][0] = new MenuButton((250/2)-52, 100, new GameSprite("ui/menu/start/1p-selected.png"), new GameSprite("ui/menu/start/1p-deselected.png")) {
					public void onClick() {
						menus.openNewPanel(menus.getPanelFromString("singleplayer_panel"));
					}
				};
				
				elements[1][0] = new MenuButton((250/2), 100, new GameSprite("ui/menu/start/mp-selected.png"), new GameSprite("ui/menu/start/mp-deselected.png")) {
					public void onClick() {
						menus.openNewPanel(menus.getPanelFromString("multiplayer_panel"));
					}
				};
				
				MenuButton back = new MenuButton((250/2)-65, 170, new GameSprite("ui/menu/back-wide-selected.png"), new GameSprite("ui/menu/back-wide-deselected.png")) {
					public void onClick() {
						menus.openPreviousPanel();
					}
				};
				
				elements[0][1] = back;
				elements[1][1] = back;
			}
		});
		
		menus.addPanel(new MenuPanel("main_menu", false) {
			@Override
			public void createElements() {
				elements = new MenuElement[1][4];
				
				elements[0][0] = new MenuButton(GameWindow.getSetWidth()/2-32, 100, new GameSprite("ui/menu/main/button-start-selected.png"), new GameSprite("ui/menu/main/button-start.png")) {
					public void onClick() {	
						menus.openNewPanel(menus.getPanelFromString("start_panel"));
					}
				};
				elements[0][1] = new MenuButton(GameWindow.getSetWidth()/2-32, 150, new GameSprite("ui/menu/main/options-selected.png"), new GameSprite("ui/menu/main/options-deselected.png")) {
					public void onClick() {
						menus.openNewPanel(menus.getPanelFromString("options_panel"));
					}
				};
				elements[0][2] = new MenuButton(GameWindow.getSetWidth()/2-32, 200, new GameSprite("ui/menu/main/controls-selected.png"), new GameSprite("ui/menu/main/controls-deselected.png")) {
					public void onClick() {
						PlayerManager.getInstance().configure(1);
					}
				};
				elements[0][3] = new MenuButton(GameWindow.getSetWidth()/2-32, 250, new GameSprite("ui/menu/main/exit-selected.png"), new GameSprite("ui/menu/main/exit-deselected.png")) {
					public void onClick() {
						System.exit(0);
					}
				};
			}
		});
	}
	
	public static void screenshot() {
		File outputfile = new File(System.getProperty("user.home") + "/AppData/Roaming/PuyoJava/Screenshots/" + Calendar.getInstance().getTimeInMillis() + ".png");
		File folder = new File(System.getProperty("user.home") + "/AppData/Roaming/PuyoJava/Screenshots/");
		try {
			if (!folder.exists()) { folder.mkdirs(); }
			if (!outputfile.exists()) { outputfile.createNewFile(); }
			ImageIO.write(GameRenderer.getLastFrame().getSnapshot(), "png", outputfile);
			System.out.println(outputfile.getPath());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(outputfile.getPath());
		}
	}
	
	//---Getters---//
	public static GameEngine getGameEngine() { return engine; }
	public static MenuManager getMenus() { return menus; }
	
	public static enum RenderQuality {
		LOW,HIGH;
		@Override
		public String toString() {
			return ("Quality: " + name());
		}
	}
}
