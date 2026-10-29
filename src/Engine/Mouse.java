package Engine;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Keeps track of mouse
public class Mouse {
    private static volatile int x, y;
	private static volatile boolean leftDown, rightDown;
	private static volatile double coordinateScale = 1;
	private static volatile int coordinateOffsetX, coordinateOffsetY;

	private static final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			setButton(e, true);
			moveTo(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setButton(e, false);
			moveTo(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			moveTo(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			moveTo(e);
		}
	};

	private Mouse() { }

	public static MouseAdapter getMouseListener() {
		return mouseListener;
	}

	public static int getX() {
		return x;
	}

	public static int getY() {
		return y;
	}

	public static boolean isLeftDown() {
		return leftDown;
	}

	public static boolean isRightDown() {
		return rightDown;
	}

	private static void moveTo(MouseEvent e) {
		x = (int) ((e.getX() - coordinateOffsetX) / coordinateScale);
		y = (int) ((e.getY() - coordinateOffsetY) / coordinateScale);
	}

	static void setCoordinateTransform(double scale, int offsetX, int offsetY) {
		coordinateScale = scale;
		coordinateOffsetX = offsetX;
		coordinateOffsetY = offsetY;
	}

	private static void setButton(MouseEvent e, boolean down) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			leftDown = down;
		}
		if (SwingUtilities.isRightMouseButton(e)) {
			rightDown = down;
		}
	}
}
