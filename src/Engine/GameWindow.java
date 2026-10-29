package Engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

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
	private boolean fullscreen;

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
		setupFullscreenToggle();
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

	private void setupFullscreenToggle() {
		String actionName = "toggleFullscreen";
		gameWindow.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), actionName);
		gameWindow.getRootPane().getActionMap().put(actionName, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				toggleFullscreen();
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
		gamePanel.requestFocusInWindow();
	}

	// triggers the game loop to start as defined in the GamePanel class
	public void startGame() {
		gamePanel.startGame();
	}

	public ScreenManager getScreenManager() {
		return gamePanel.getScreenManager();
	}
}
