/*
 * This file is part of the ga-tetris package.
 *
 * Copyright (C) 2012, Eric Fritz
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

package com.kauri.gatetris.command;

import com.kauri.gatetris.Game;

/**
 * @author Eric Fritz
 */
public class SoftDropCommand implements Command
{
	private Game game;

	public SoftDropCommand(Game game)
	{
		this.game = game;
	}

	@Override
	public void execute()
	{
		if (!game.isFalling()) {
			// TODO - repeated code - try to register hard drop command?
			game.tryMove(game.current, game.xPos, game.board.dropHeight(game.current, game.xPos, game.yPos), true);
		} else {
			if (game.tryMove(game.current, game.xPos, game.yPos - 1)) {
				game.pieceValue = Math.max(0, game.pieceValue - 1);
			}
		}
	}
}