package Engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * The JFrame that holds the GamePanel
 * Just does some setup and exposes the gamePanel's screenManager to allow an external class to setup their own content and attach it to this engine.
 */
public class GameWindow {
	private JFrame gameWindow;
	private GamePanel gamePanel;
	private Rectangle windowedBounds;
	private JMenuBar windowedMenuBar;
	private JCheckBoxMenuItem fullscreenItem;
	private KeyEventDispatcher fullscreenKeyDispatcher;
	private boolean fullscreen;
	private boolean toggleKeyDown;

	public GameWindow() {
		gameWindow = new JFrame("Game");
		gamePanel = new GamePanel();
		gamePanel.setFocusable(true);
		gameWindow.setContentPane(gamePanel);
		setupWindowMenu();
		gameWindow.setSize(Config.GAME_WINDOW_WIDTH, Config.GAME_WINDOW_HEIGHT);
		gameWindow.setLocationRelativeTo(null);
		gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // it'd be nice if this actually worked more than 1/3rd of the time
		gameWindow.setVisible(true);
		gamePanel.setupGame();
		setupFullscreenKeyDispatcher();
		gamePanel.requestFocusInWindow();
	}

	private void setupWindowMenu() {
		windowedMenuBar = new JMenuBar();
		JMenu viewMenu = new JMenu("View");
		fullscreenItem = new JCheckBoxMenuItem("Fullscreen (F11)");
		fullscreenItem.addActionListener(event -> toggleFullscreen());
		viewMenu.add(fullscreenItem);
		windowedMenuBar.add(viewMenu);
		gameWindow.setJMenuBar(windowedMenuBar);
		gameWindow.setResizable(true);
	}

	private void setupFullscreenKeyDispatcher() {
		fullscreenKeyDispatcher = event -> {
			int keyCode = event.getKeyCode();
			if (event.getID() == KeyEvent.KEY_RELEASED
					&& (keyCode == KeyEvent.VK_F11 || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE)) {
				toggleKeyDown = false;
				return false;
			}

			boolean toggleKey = keyCode == KeyEvent.VK_F11
					|| (keyCode == KeyEvent.VK_ENTER && event.isAltDown());
			boolean exitKey = fullscreen && keyCode == KeyEvent.VK_ESCAPE;
			if (!toggleKey && !exitKey) {
				return false;
			}

			if (gameWindow.isActive() && event.getID() == KeyEvent.KEY_PRESSED && !toggleKeyDown) {
				toggleKeyDown = true;
				if (toggleKey || fullscreen) {
					SwingUtilities.invokeLater(this::toggleFullscreen);
				}
				event.consume();
			}
			return event.isConsumed();
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(fullscreenKeyDispatcher);
		gameWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent event) {
				SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
			}

			@Override
			public void windowClosed(WindowEvent event) {
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
						.removeKeyEventDispatcher(fullscreenKeyDispatcher);
			}
		});
	}

	private void toggleFullscreen() {
		if (fullscreen) {
			gameWindow.dispose();
			gameWindow.setUndecorated(false);
			gameWindow.setBounds(windowedBounds);
			gameWindow.setJMenuBar(windowedMenuBar);
			gameWindow.setVisible(true);
			fullscreen = false;
		} else {
			windowedBounds = gameWindow.getBounds();
			Rectangle screenBounds = gameWindow.getGraphicsConfiguration().getBounds();
			gameWindow.dispose();
			gameWindow.setUndecorated(true);
			gameWindow.setJMenuBar(null);
			gameWindow.setBounds(screenBounds);
			gameWindow.setVisible(true);
			fullscreen = true;
		}
		fullscreenItem.setSelected(fullscreen);
		gameWindow.getRootPane().revalidate();
		gameWindow.toFront();
		gameWindow.requestFocus();
		SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
	}

	// triggers the game loop to start as defined in the GamePanel class
	public void startGame() {
		gamePanel.startGame();
	}

	public ScreenManager getScreenManager() {
		return gamePanel.getScreenManager();
	}
}
