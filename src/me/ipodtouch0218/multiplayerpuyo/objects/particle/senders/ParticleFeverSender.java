package me.ipodtouch0218.multiplayerpuyo.objects.particle.senders;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;

import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.PuyoType;
import me.ipodtouch0218.multiplayerpuyo.PuyoType.PuyoSprites;
import me.ipodtouch0218.multiplayerpuyo.manager.FeverManager;
import me.ipodtouch0218.multiplayerpuyo.sound.GameSounds;

public class ParticleFeverSender extends ParticleSender {

	private FeverManager manager;
	
	public ParticleFeverSender(FeverManager manager, int targetX, int targetY) {
		super(targetX, targetY, PuyoType.GARBAGE.getSprite(PuyoSprites.POPPING), null, 0.156, 4);
		this.manager = manager;
	}
	
	@Override
	public void arrived() {
		manager.addCharge();
		GameSounds.FEVER_METER_FILL.play();
	}
	
	
	private Color col = new Color(38, 251, 255);
	@Override
	public void render(Graphics2D g) {
		if (PuyoGameMain.quality == RenderQuality.HIGH && tailX != 0 && tailY != 0) {
			GradientPaint gPaint = new GradientPaint(new Point((int) x,(int) y), col, new Point(tailX,tailY), Color.white);
			g.setPaint(gPaint);
			g.fillPolygon(new int[]{tailX,(int) (x+8),(int) x}, new int[]{tailY,(int) (y+4),(int) (y+4)}, 3);
		}
		
		g.setColor(Color.WHITE);
		g.fillOval((int) x, (int) y, 8, 8);
	}
}
