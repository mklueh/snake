package snake;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import snake.Snake.HitListener;
import snake.Snake.Piece;
import snake.Snake.Rotation;
import snake.Snake.SnakeController;
import snake.Snake.SnakeObserver;
import snake.Snake.StartDirection;

public class Main extends JPanel implements KeyListener, HitListener,
		SnakeController, SnakeObserver {
	private Rotation rotation;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(400, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Main main = new Main();
		frame.add(main);
		frame.addKeyListener(main);
		frame.pack();
		frame.setVisible(true);

	}

	private Snake snake1;
	private Food food;
	private int snakeSize;

	public Main() {

		snake1 = new Snake(100, 10, 10, 4, StartDirection.toRight,
				new Rectangle(new Dimension(30, 30)), this, null);
		snake1.registerController(this);
		snake1.registerObserver(this);
		placeFood();

		start();
	}

	private void placeFood() {

		int x = (int) (Math.random() * snake1.getGameField().width);
		int y = (int) (Math.random() * snake1.getGameField().height);
		food = new Food(0, x, y);
		System.out.println(x + " " + y);
		snake1.registerGameObject(food);
	}

	private void start() {

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					snake1.doLogic();
					rotation = Rotation.none;
					repaint();

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		}).start();
	}

	@Override
	public boolean addPiece() {

		return false;
	}

	@Override
	public boolean hit(int snakeID, int objectID) {
		System.out.println("Hit of " + snakeID + " with " + objectID);

		if (objectID == food.getID()) {
			snake1.unregisterGameObject(food);
			food = null;
			snake1.addPiece();
			placeFood();
			return true;
		}
		return false;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_LEFT) {
			rotation = Rotation.left;
		} else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
			rotation = rotation.right;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	
		g.drawString(snakeSize + "", 10, 10);
		if (food != null) {

			for (ListIterator<Piece> it = food.getPieces().listIterator(); it
					.hasNext();) {
				Piece p = it.next();
				g.drawRect(p.xV * 10, p.yV * 10, 10, 10);
			}
		}
		for (ListIterator<Piece> it = snake1.getSnakePieces().listIterator(); it
				.hasNext();) {
			Piece p = it.next();
			g.drawRect(p.xV * 10, p.yV * 10, 10, 10);
		}
		Rectangle rect = snake1.getGameField();
		g.drawRect( rect.x * 10+10,  rect.y * 10+10, rect.width * 10+10,
				rect.height * 10+10);
	}

	@Override
	public Rotation rotate() {
		if (rotation != null)
			return rotation;
		return Rotation.none;
	}

	@Override
	public void info(int snakeID, int snakeSize) {
		this.snakeSize = snakeSize;
	}

	@Override
	public void snakePieces(List<Piece> snakePieces) {
		// TODO Auto-generated method stub

	}

	@Override
	public void colliededWith(int snakeID, int objectID) {
		// TODO Auto-generated method stub

	}

}
