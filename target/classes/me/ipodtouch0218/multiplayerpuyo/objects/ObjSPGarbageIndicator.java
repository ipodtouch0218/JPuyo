package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.VolatileImage;

import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;

public class ObjSPGarbageIndicator extends ObjGarbageIndicator {

	private static final GameSprite bgSprite = new GameSprite("ui/board/garbage/bg.png");
	
	public ObjSPGarbageIndicator() {
		super(false);
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(bgSprite.getImage(), (int) x-6, (int) y-15, null);
		if (getOverallGarbage() <= 0) { return; } //dont render if theres no garabge dummy
		int spacer = 0;
		if (renderer == null || renderer.validate(GameRenderer.gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
			renderer = GameRenderer.createVolatile(20*6, 16, false);
		}
		
		Graphics2D rendererG = renderer.createGraphics();
		rendererG.setComposite(AlphaComposite.Clear);
		rendererG.fillRect(0, 0, renderer.getWidth(), renderer.getHeight());
		rendererG.setComposite(AlphaComposite.SrcOver);
		
		int count = 0;
		int garbageCounter = garbage;
		
		for (GarbageSprites sprites : GarbageSprites.values()) {
			while (garbageCounter >= sprites.getValue() && count < 6) {
				garbageCounter -= sprites.getValue();
				count++;
				Image sprite = sprites.getSprite().getImage();
				if (grayscale) {
					sprite = sprites.getGrayscaleSprite();
				}
				rendererG.drawImage(sprite, (int) (spacer*moveDelay), 8-(sprite.getHeight(null)/2), null);
				spacer += 16;
			}
		}
		
		g.drawImage(renderer, (int) x, (int) y, 20*6, 16, null);
	}
	
	public void clear() {
		garbage = 0;
	}
}
