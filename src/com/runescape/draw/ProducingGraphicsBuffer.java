package com.runescape.draw;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public final class ProducingGraphicsBuffer {

	public final int[] canvasRaster;
	public final int canvasWidth;
	public final int canvasHeight;
	private final BufferedImage bufferedImage;
	private float[] depthbuffer;

	public ProducingGraphicsBuffer(int canvasWidth, int canvasHeight) {
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		depthbuffer = new float [canvasWidth * canvasHeight];
		bufferedImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
		canvasRaster = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
		initDrawingArea();
	}

	public void drawGraphics(int x, int y, Graphics graphics) {
		graphics.drawImage(bufferedImage, x, y, null);
	}

	public void initDrawingArea() {
		Rasterizer2D.initializeDrawingArea(canvasHeight, canvasWidth, canvasRaster, depthbuffer);   
	}
}
