package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Graphics2D;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.display.sprite.SpriteSheet;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.manager.Controls;
import me.ipodtouch0218.multiplayerpuyo.misc.PuyoCharacter;
import me.ipodtouch0218.multiplayerpuyo.objects.boards.ObjPuyoBoard;
import me.ipodtouch0218.multiplayerpuyo.sound.CharacterSounds;

public class ObjCharacterSelect extends GameObject {

	private static final SpriteSheet playercursors = new SpriteSheet("ui/character/cursors.png", 29, 19);
	private static final GameSprite selected = new GameSprite("ui/character/selected.png", false); //-5,-5
	private static final GameSprite blank = new GameSprite("ui/character/blank.png", false);
	
	private HashMap<ObjPuyoBoard, CursorInfo> pos = new HashMap<>();
	private boolean allready;
	
	public ObjCharacterSelect(ArrayList<ObjPuyoBoard> boards) {
		for (ObjPuyoBoard b : boards) {
			pos.put(b, new CursorInfo());
			b.setCharacter(PuyoCharacter.values()[0], false);
		}
	}
	
	
	@Override
	public void tick(double delta) {
		if (allready) {
			return;
		}
		for (Entry<ObjPuyoBoard,CursorInfo> entry : pos.entrySet()) {
			ObjPuyoBoard board = entry.getKey();
			CursorInfo info = entry.getValue();
			if (info.confirmed) {
				continue;
			}
			
			checkForMovement(board, delta);
			checkForConfirmation(board);
			if (info.held) {
				info.heldtimer += delta;
			}
			
			if (!info.confirmed) { allready = false; }
		}
	}
	private boolean checkForMovement(ObjPuyoBoard b, double delta) {
		Controls c = b.getControls();
		CursorInfo inf = pos.get(b);
		if (c == null || inf.pressed) { return false; }
		
		if (inf.timer > 0) {
			inf.timer-=delta;
			return false;
		}
		boolean updateTimer = false;
		if (c.menuLeft) {
			inf.x -= 1;
			if (inf.x < 0) {
				inf.x = 9;
			} else if (inf.x > 9) {
				inf.x = 0;
			}
			updateTimer = true;
		} else if (c.menuRight) {
			inf.x += 1;
			if (inf.x < 0) {
				inf.x = 9;
			} else if (inf.x > 9) {
				inf.x = 0;
			}			
			updateTimer = true;
		}
		if (c.menuUp) {
			inf.y -= 1;
			if (inf.y < 0) {
				inf.y = 3;
			} else if (inf.y > 3) {
				inf.y = 0;
			}
			updateTimer = true;
		} else if (c.menuDown) {
			inf.y += 1;
			if (inf.y < 0) {
				inf.y = 2;
			} else if (inf.y > 2) {
				inf.y = 0;
			}
			updateTimer = true;
		}
		
		if (updateTimer) {
			inf.timer = 0.25;
			int chara = inf.x+inf.y*10;
			if (chara < PuyoCharacter.values().length) {
				b.setCharacter(PuyoCharacter.values()[chara], inf.alternate);
			}
		}
		return updateTimer;
	}
	
	private void checkForConfirmation(ObjPuyoBoard b) {
		Controls c = b.getControls();
		CursorInfo inf = pos.get(b);
		
		if (inf.pressed) {
			inf.held = c.menuEnter;
		}
		
		if (c == null || (inf.heldtimer > 1) || (inf.pressed && !inf.held)) {

			inf.alternate = (inf.heldtimer > 1);
			inf.confirmed = true;
			CharacterSounds.SELECT.getSound(b.getCharacter(), inf.alternate).play();
			b.setCharacter(b.getCharacter(), inf.alternate);
		}
		if (c.menuEnter) {
			inf.pressed = true;
		}
	}

	@Override
	public void render(Graphics2D g) {
		//drawChars(Graphics g);
		
		int counter = 0;
		for (int y1 = 0; y1 < 3; y1++) {
			int ypos = (360/4)+(y1*56);
			for (int x1 = 0; x1 < 10; x1++) {
				int xpos = (640/2)+((x1-4)*61)-60;
				
				VolatileImage img = null;
				if (counter >= PuyoCharacter.values().length) {
					img = blank.getImage();
				} else {
					img = PuyoCharacter.values()[counter].getSelectImage().getImage();
				}
				g.drawImage(img, xpos, ypos, null);
				counter++;
			}
		}
		
		ArrayList<Integer[]> sel = new ArrayList<>();
		for (Entry<ObjPuyoBoard,CursorInfo> entry : pos.entrySet()) {
			CursorInfo inf = entry.getValue();
			ObjPuyoBoard b = entry.getKey();
			int xpos = (640/2)+((inf.x-4)*61)-60-5;
			int ypos = (360/4)+(inf.y*56)-5;
			
			boolean drawSelected = true;
			s: for (Integer[] s : sel) {
				if (s[0] == inf.x && s[1] == inf.y) {
					drawSelected = false;
					break s;
				}
			}
			if (drawSelected) {
				g.drawImage(selected.getImage(), xpos, ypos, null);
				sel.add(new Integer[]{inf.x,inf.y});
			}
			int player = b.getPlayer()+1;
			boolean lower = player > 2;
			g.drawImage(playercursors.getSprite(player-1, 0).getImage(), xpos+(player%2==0 ? 29 : 0), ypos-1+(lower ? 40 : 0), null);
			
			if (inf.alternate) {
				g.drawImage(playercursors.getSprite(4, 0).getImage(), xpos+(player%2==0 ? 29 : 0), ypos-1+(lower ? 40 : 0), null);
			}
		}
	}
	
	
	//---getters---//
	public boolean allConfirmed() {
		for (CursorInfo info : pos.values()) {
			if (!info.confirmed) return false;
		}
		return true;
	}
	
	//---//
	private class CursorInfo {
		
		double timer, heldtimer;
		int x, y;
		boolean confirmed, alternate, held, pressed;
		
	}
}
