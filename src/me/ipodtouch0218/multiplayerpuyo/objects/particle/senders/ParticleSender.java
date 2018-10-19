package me.ipodtouch0218.multiplayerpuyo.objects.particle.senders;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;

import me.ipodtouch0218.java2dengine.display.sprite.GameSprite;
import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.java2dengine.util.Vector2D;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain.RenderQuality;
import me.ipodtouch0218.multiplayerpuyo.manager.PuyoBoardManager;

public abstract class ParticleSender extends GameObject {

	private int tailLength;
	private Color tail;
	private double velocity;
	private double speed;
	protected int targetX, targetY;
	private Vector2D vel = new Vector2D();
	
	private boolean done;
	protected int tailX, tailY;
	
	public ParticleSender(int targetX, int targetY, GameSprite image, Color tail, double speed, int tailLength) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.speed = speed;
		
		this.sprite = image;
		this.tail = tail;
		this.tailLength = tailLength;
	}
	
	@Override
	public void tick(double delta) {
		if (PuyoBoardManager.isPaused()) { return; }
		velocity += (speed*60d)*delta;
		vel.setX(targetX-x).setY(targetY-y);
		
		if (vel.getMagnitude() < velocity) {
			done = true;
			PuyoGameMain.getGameEngine().removeGameObject(this);
			arrived();
			return;
		}
		
		vel.normalize().multiply(velocity*tailLength);
		tailX = (int) (x-vel.getX());
		tailY = (int) (y-vel.getY());
		

		vel.normalize().multiply(velocity);
		x += vel.getX();
		y += vel.getY();
	}
	
	public boolean done() { return done; }
	public abstract void arrived();
	
	
	@Override
	public void render(Graphics2D g) {
		if (PuyoGameMain.quality == RenderQuality.HIGH && tailX != 0 && tailY != 0) {
			GradientPaint gPaint = new GradientPaint(new Point((int) x,(int) y), tail, new Point(tailX,tailY), new Color(255,255,255,100));
			g.setPaint(gPaint);
			g.fillPolygon(new int[]{tailX,(int) (x+sprite.getImage().getWidth()),(int) x}, new int[]{tailY,(int) (y+(sprite.getImage().getHeight()/2)),(int) (y+(sprite.getImage().getHeight()/2))}, 3);
		}
		
		g.drawImage(sprite.getImage(), (int) x, (int) y, null);
	}
}
