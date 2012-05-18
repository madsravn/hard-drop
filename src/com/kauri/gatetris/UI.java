/*
 * This file is part of the ga-tetris package.
 *
 * Copyright (C) 2012, efritz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */

package com.kauri.gatetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import com.kauri.gatetris.Game.State;
import com.kauri.gatetris.Tetromino.Shape;
import com.kauri.gatetris.ai.Strategy.Move;

/**
 * @author efritz
 */
public class UI
{
	private static Map<Shape, Color> colors = new HashMap<Shape, Color>();

	static {
		colors.put(Shape.I, Color.red);
		colors.put(Shape.J, Color.blue);
		colors.put(Shape.L, Color.orange);
		colors.put(Shape.O, Color.yellow);
		colors.put(Shape.S, Color.magenta);
		colors.put(Shape.T, Color.cyan);
		colors.put(Shape.Z, Color.green);
		colors.put(Shape.Junk, Color.darkGray);
		colors.put(Shape.NoShape, new Color(240, 240, 240));
	}
	private boolean fontIsDirty = true;
	private int width;
	private int height;
	private Game game;

	public UI(Game game)
	{
		this.game = game;
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;

		fontIsDirty = true;

		// TODO - also change font size
	}

	private int getWidth()
	{
		return width;
	}

	private int getHeight()
	{
		return height;
	}

	private int getAdjustedBoardWidth()
	{
		return (int) Math.min(getWidth(), (double) getHeight() * game.board.getWidth() / game.board.getHeight());
	}

	private int getAdjustedBoardHeight()
	{
		return (int) Math.min(getHeight(), (double) getWidth() * game.board.getHeight() / game.board.getWidth());
	}

	private int getSquareWidth()
	{
		return getAdjustedBoardWidth() / (game.board.getWidth() + (game.showNextPiece ? 2 : 0));
	}

	private int getSquareHeight()
	{
		return getAdjustedBoardHeight() / (game.board.getHeight() + (game.showNextPiece ? 4 : 0));
	}

	private int getLeftMargin()
	{
		return (getWidth() - game.board.getWidth() * getSquareWidth()) / 2;
	}

	private int getTopMargin()
	{
		return (getHeight() - game.board.getHeight() * getSquareWidth()) / 2;
	}

	public void render(Graphics g)
	{
		g.setColor(colors.get(Shape.NoShape));
		g.fillRect(0, 0, getWidth(), getHeight());

		for (int row = 0; row < game.board.getHeight(); row++) {
			for (int col = 0; col < game.board.getWidth(); col++) {
				drawSquare(g, translateBoardRow(row), translateBoardCol(col), colors.get(game.board.getShapeAt(row, col)));
			}
		}

		if (game.showShadowPiece) {
			int ghostPosition = game.board.dropHeight(game.current, game.xPos, game.yPos);

			if (ghostPosition < game.yPos) {
				drawTetromino(g, game.current, translateBoardRow(ghostPosition), translateBoardCol(game.xPos), changeAlpha(colors.get(game.current.getShape()), .3), getTopMargin());
			}
		} else if (game.showAiPiece) {
			Move move = game.ai.getBestMove(game.board, game.current, game.preview, game.xPos, game.yPos);

			Tetromino current2 = game.current;

			for (int i = 0; i < move.rotationDelta; i++) {
				current2 = Tetromino.rotateLeft(current2);
			}

			int ghostPosition = game.board.dropHeight(current2, game.xPos + move.translationDelta, game.yPos);

			if (ghostPosition < game.yPos) {
				drawTetromino(g, current2, translateBoardRow(ghostPosition), translateBoardCol(game.xPos + move.translationDelta), changeAlpha(colors.get(current2.getShape()), .3), getTopMargin());
			}
		}

		if (game.board.canMove(game.current, game.xPos, game.yPos)) {
			drawTetromino(g, game.current, translateBoardRow(game.yPos), translateBoardCol(game.xPos), colors.get(game.current.getShape()), getTopMargin());
		}

		if (game.showNextPiece) {
			int xPos = (game.board.getWidth() - game.preview.getWidth()) / 2 + Math.abs(game.preview.getMinX());

			int rowOffset = (getTopMargin() - (game.preview.getHeight() * getSquareHeight())) / 2;

			drawTetromino(g, game.preview, rowOffset, translateBoardCol(xPos), colors.get(game.preview.getShape()), 0);
		}

		if (game.state == State.PAUSED) {
			drawString(g, "paused");
		}

		if (game.state == State.GAMEOVER) {
			drawString(g, "game over");
		}

	}

	private Color changeAlpha(Color color, double percent)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(1, (int) (color.getAlpha() * percent))));
	}

	Font font;

	private void drawString(Graphics g, String string)
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		//
		// NASTY NASTY NASTY
		//

		if (fontIsDirty) {
			int points = 20;
			int targetThreshold = (int) (getWidth() * .75);
			FontMetrics fm;

			do {
				font = new Font("Arial", Font.PLAIN, points++);
				g.setFont(font);

				fm = g.getFontMetrics();
			} while (fm.stringWidth(string) < targetThreshold);

			fontIsDirty = false;
		}

		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();

		g.setColor(new Color(0, 0, 0, (int) (255 * .5)));
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(new Color(255, 255, 255));
		g.drawString(string, (getWidth() / 2) - (fm.stringWidth(string) / 2), (getHeight() / 2) + fm.getDescent());
	}

	private int translateBoardRow(int row)
	{
		return getTopMargin() + (game.board.getHeight() - 1 - row) * getSquareHeight();
	}

	private int translateBoardCol(int col)
	{
		return getLeftMargin() + col * getSquareWidth();
	}

	private void drawTetromino(Graphics g, Tetromino piece, int row, int col, Color color, int top)
	{
		if (piece.getShape() != Shape.NoShape) {
			for (int i = 0; i < piece.getSize(); i++) {
				int xPos = col + piece.getX(i) * getSquareWidth();
				int yPos = row + piece.getY(i) * getSquareHeight();

				if (yPos >= top) {
					drawSquare(g, yPos, xPos, color);
				}
			}
		}
	}

	private void drawSquare(Graphics g, int row, int col, Color color)
	{
		g.setColor(color.darker());
		g.fillRect(col, row, getSquareWidth(), getSquareHeight());

		g.setColor(color);
		g.fillRect(col + 1, row + 1, getSquareWidth() - 2, getSquareHeight() - 2);
	}
}
