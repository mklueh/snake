package snake;

import snake.Snake.GameObject;
import snake.Snake.Piece;

public class Food extends GameObject {
	public Food(int id, int x, int y) {
		add(new Piece(id, x, y));
	}

}
