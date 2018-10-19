package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;

import javax.swing.GrayFilter;

import me.ipodtouch0218.java2dengine.display.GameRenderer;
import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.misc.LoadingListener;

public class ObjGarbageIndicator extends GameObject {
	
	protected int garbage; 
	protected double moveDelay = 1;
	protected boolean grayscale = false;
	private boolean opposite;
	
	public ObjGarbageIndicator(boolean opposite) {
		this.opposite = opposite;
	}
	
	@Override
	public void tick(double delta) {
//		System.out.println(x + "," + y);
		if (moveDelay < 1) {
			moveDelay += delta*4;
		} else {
			moveDelay = 1;
		}
	}
	
	protected VolatileImage renderer;
	@Override
	public void render(Graphics2D g) {
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
		
		g.drawImage(renderer, (int) x+((opposite && grayscale) ? 48 : 0)-3, (int) (y-(grayscale ? 16 : 16)), (int) (grayscale ? 20*3 : 20*6), (grayscale ? 8 : 16), null);
	}
	
	public void setGrayscale(boolean enabled) { grayscale = enabled; }
	public int getOverallGarbage() { return garbage; }
	public void setOverallGarbage(int value) {
		if (getOverallGarbage() == value) {
			return;
		}
		moveDelay = 0;
		garbage = value; 
	}
	public void addGarbage(int value) {
		setOverallGarbage(getOverallGarbage() + value);
	}
	
	//---Others---//
	public static enum GarbageSprites {
		
		COMET("comet.png",1440),
		CROWN("crown.png",720),
		MOON("moon.png",360),
		STAR("star.png",180),
		RED("red.png",30),
		LARGE("large.png",6),
		SMALL("small.png",1);
		
		private GameSprite sprite;
		private Image graySprite;
		private String filename;
		private int value;
		GarbageSprites(String filename, int value) {
			this.filename = filename;
			this.value = value;
		}
		
		public int getValue() { return value; }
		public String getFileName() { return filename; }
		public GameSprite getSprite() {
			if (sprite == null) { load(); }
			return sprite;
		}
		public Image getGrayscaleSprite() {
			if (graySprite == null) { load(); }
			return graySprite;
		}
		
		public void load() {
			sprite = new GameSprite("ui/board/garbage/" + filename);
			ImageFilter filter = new GrayFilter(true, 50);  
			ImageProducer producer = new FilteredImageSource(sprite.getImage().getSource(), filter);  
			graySprite = Toolkit.getDefaultToolkit().createImage(producer);  
		}
		
		///
		public static void load(LoadingListener listener) {
			for (GarbageSprites spr : GarbageSprites.values()) {
				spr.load();
				listener.loaded(spr.getFileName());
			}
		}
	}
}
