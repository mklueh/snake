package snake;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Snake {

	private List<Piece> snakePieces = new ArrayList<Piece>();
	private List<SnakeObserver> observers = new ArrayList<SnakeObserver>();
	private List<GameObject> gameObjects = new ArrayList<GameObject>();;
	private SnakeController controller;
	private int snakeID;
	private HitListener mListener;
	private Direction currentDirection;
	private Rectangle gameField;
	private boolean appendNewPiece;

	public Snake(int id, int startX, int startY, int length,
			StartDirection direction, Rectangle field, HitListener listener,
			List<GameObject> gameObjects) {
		snakeID = id;
		mListener = listener;
		gameField = field;
		createSnake(startX, startY, length, direction);
	}

	public void registerListener(HitListener listener) {
		this.mListener = listener;
	}

	public void registerObserver(SnakeObserver observer) {
		observers.add(observer);
	}


	public void registerController(SnakeController controller) {
		this.controller = controller;
	}


	public void registerGameObject(GameObject object) {
		gameObjects.add(object);
	}

	public void unregisterGameObject(GameObject object) {
		gameObjects.remove(object);
	}

	private void createSnake(int startX, int startY, int length,
			StartDirection direction) {

		for (int i = 0; i < length; i++) {
			switch (direction) {
			case toRight:
				currentDirection = Direction.left;
				snakePieces.add(new Piece(i, startX + i, startY));
				break;

			case toLeft:
				currentDirection = Direction.right;
				snakePieces.add(new Piece(i, startX - i, startY));
				break;

			case toUp:
				currentDirection = Direction.up;
				snakePieces.add(new Piece(i, startX, startY - i));
				break;

			case toDown:
				currentDirection = Direction.down;
				snakePieces.add(new Piece(i, startX, startY + i));
				break;

			}
		}
	}

	// /////////////////////////////////////////////////////////////////////

	// Movement

	public void move(Direction dir) {
		int xPrev = 0, yPrev = 0, xOld, yOld, id = 0;
		Piece head = snakePieces.get(0);

		switch (dir) {

		case left:
			xPrev = head.xV - 1;
			yPrev = head.yV;
			break;
		case right:
			xPrev = head.xV + 1;
			yPrev = head.yV;
			break;
		case up:
			xPrev = head.xV;
			yPrev = head.yV - 1;
			break;
		case down:
			xPrev = head.xV;
			yPrev = head.yV + 1;
			break;
		}

		for (ListIterator<Piece> it = snakePieces.listIterator(); it.hasNext();) {
			Piece currentPiece = it.next();
			xOld = currentPiece.xV;
			yOld = currentPiece.yV;
			currentPiece.xV = xPrev;
			currentPiece.yV = yPrev;
			xPrev = xOld;
			yPrev = yOld;
			id = currentPiece.idP;

		}
		if (appendNewPiece) {
			snakePieces.add(new Piece(id + 1, xPrev, yPrev));
			appendNewPiece = false;
		}
	}

	private Rotation getRotationFromController() {
		if (controller != null)
			return controller.rotate();
		return Rotation.none;
	}

	public void doLogic() {

		Direction nextDirection = nextDirection();
		currentDirection = nextDirection;

		if (validMove(nextDirection))
			move(nextDirection);
		notifyObservers();
	}

	private void notifyObservers() {

		for (ListIterator<SnakeObserver> it = observers.listIterator(); it
				.hasNext();) {
			SnakeObserver obs = it.next();
			obs.info(snakeID, snakePieces.size());
		}
	}

	private boolean validMove(Direction direction) {
		int objID = objectCollides();
		if (objID != -1) {
			return fireCollision(snakeID, objID);
		}
		return !selfCollides() && isInBounds();
	}

	private boolean fireCollision(int snakeID, int objectID) {

		for (ListIterator<SnakeObserver> it = observers.listIterator(); it
				.hasNext();) {
			it.next().colliededWith(snakeID, objectID);
		}
		return mListener.hit(snakeID, objectID);
	}

	public void addPiece() {
		appendNewPiece = true;
	}

	private boolean isInBounds() {
		Piece head = snakePieces.get(0);
		if (head.xV < gameField.x)
			return false;
		if (head.xV > gameField.width)
			return false;
		if (head.yV < gameField.y)
			return false;
		if (head.yV > gameField.height)
			return false;
		return true;
	}

	private boolean selfCollides() {
		Piece head = snakePieces.get(0);
		for (ListIterator<Piece> it = snakePieces.listIterator(); it.hasNext();) {
			Piece piece = it.next();
			if (piece.idP != 0 && head.intersects(piece)) {
				return true;
			}
		}
		return false;
	}

	private int objectCollides() {
		if (gameObjects != null) {
			Piece head = snakePieces.get(0);
			for (ListIterator<GameObject> objIt = gameObjects.listIterator(); objIt
					.hasNext();) {
				GameObject object = objIt.next();
				for (ListIterator<Piece> it = object.pieces.listIterator(); it
						.hasNext();) {
					if (head.intersects(it.next())) {
						return object.gameObjectID;
					}
				}
			}
		}
		return -1;

	}

	private Direction nextDirection() {
		Rotation rotation = getRotationFromController();
		if (rotation != null) {
			switch (currentDirection) {

			case up:
				if (rotation.equals(Rotation.right)) {
					return Direction.right;
				} else if (rotation.equals(Rotation.left)) {
					return Direction.left;
				}
				return Direction.up;

			case down:
				if (rotation.equals(Rotation.right)) {
					return Direction.left;
				} else if (rotation.equals(Rotation.left)) {
					return Direction.right;
				}
				return Direction.down;
			case left:
				if (rotation.equals(Rotation.right)) {
					return Direction.up;
				} else if (rotation.equals(Rotation.left)) {
					return Direction.down;
				}
				return Direction.left;

			case right:
				if (rotation.equals(Rotation.right)) {
					return Direction.down;
				} else if (rotation.equals(Rotation.left)) {
					return Direction.up;
				}

				return Direction.right;
			}
		}
		return null;
	}

	// Definitions

	enum StartDirection {
		toRight, toLeft, toUp, toDown;
	}

	enum Direction {
		left, right, up, down;
	}

	enum Rotation {

		right, left, none;
	}

	// Interfaces

	interface SnakeController {
		public Rotation rotate();
	}

	interface HitListener {

		public boolean hit(int snakeID, int ObjectID);

		public boolean addPiece();
	}

	interface SnakeObserver {
		public void colliededWith(int snakeID, int objectID);

		public void info(int snakeID, int snakeSize);

		public void snakePieces(List<Piece> snakePieces);

	}

	static abstract class GameObject {
		private int gameObjectID;
		private List<Piece> pieces = new ArrayList<Piece>();

		public List<Piece> getPieces() {
			return pieces;
		}

		public int getID() {
			return gameObjectID;
		}

		public void add(Piece piece) {
			pieces.add(piece);
		}

		public void remove(Piece piece) {
			pieces.remove(piece);
		}

		public boolean intersectsWith(Piece piece) {

			for (ListIterator<Piece> it = pieces.listIterator(); it.hasNext();) {
				if (piece.intersects(it.next()))
					return true;
			}
			return false;
		}
	}

	static class Piece {
		int xV, yV, idP;

		public Piece(int id, int x, int y) {
			xV = x;
			yV = y;
			idP = id;
		}

		public boolean intersects(Piece piece) {
			if (piece.xV == xV && piece.yV == yV)
				return true;
			return false;
		}

	}

	public List<Piece> getSnakePieces() {
		return snakePieces;
	}

	public Rectangle getGameField() {
		return gameField;
	}

	public boolean intersectsWithSnake(Piece piece) {

		for (ListIterator<Piece> it = snakePieces.listIterator(); it.hasNext();) {
			if (piece.intersects(it.next()))
				return true;
		}
		return false;
	}

}
