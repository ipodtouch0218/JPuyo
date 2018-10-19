package me.ipodtouch0218.multiplayerpuyo.objects;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import me.ipodtouch0218.java2dengine.object.GameObject;
import me.ipodtouch0218.multiplayerpuyo.PuyoGameMain;


public class ObjTextDisplay extends GameObject {

	private double lifetime;
	private double xv,yv;
	private String display;
	private float size = 14;
	private Color color = Color.BLACK;
	
	public ObjTextDisplay(String display, double xv, double yv, int lifetime) {
		this(display,xv,yv,lifetime,14);
	}
	
	public ObjTextDisplay(String display, double xv, double yv, int lifetime, float px) {
		this(display,xv,yv,lifetime,px,Color.BLACK);
	}
	
	public ObjTextDisplay(String display, double xv, double yv, int lifetime, float px, Color color) {
		this.xv = xv;
		this.yv = yv;
		this.display = display;
		this.lifetime = lifetime/60d;
		this.size = px;
		this.color = color;
	}
	
	private double prevDelta;
	@Override
	public void tick(double delta) {
		x+=(xv*60d)*delta;
		y+=(yv*60d)*delta;
		if (lifetime <= 0 && lifetime+prevDelta+0.01 > 0) {
			PuyoGameMain.getGameEngine().removeGameObject(this);
		}
		lifetime-=delta;
		prevDelta = delta;
	}
	
	@Override
	public void render(Graphics2D g) {
		
		if (display == null) { return; }
		if (PuyoGameMain.puyofont == null) { return; }
		g.setColor(color);
		g.setFont(PuyoGameMain.puyofont.deriveFont(size));
		
		FontMetrics metrics = g.getFontMetrics();
		if (metrics == null) { return; }
		int nx = 0, ny = 0;
		try {
		    nx = (int) (x - (metrics.stringWidth(display)) / 2);
		    ny = (int) (y + (metrics.getHeight() / 2) + metrics.getAscent());
		} catch (NullPointerException e) {}
	    
		if (display == null) { return; }
		g.drawString(display, nx, ny);
		
	}
	
	public void setColor(Color color) { this.color = color; }
	public void setDisplay(String display) { this.display = display; }
}
