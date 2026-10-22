package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.image.BufferedImage;


public class PumpkinGate extends EnhancedMapTile {
    private static final int LINTEL = 7;        
    private static final int SLAM_FRAMES = 8; 
    private static final int LIFT_FRAMES = 30; 

    private boolean closed = false;
    private float amountDown = 0;              

    public PumpkinGate(Point location) {
        super(location.x, location.y,
                new FrameBuilder(ImageLoader.load("PumpkinGate.png")).withScale(3).build(),
                TileType.PASSABLE);
        setIsUpdateOffScreen(true);  
    }

    public void close() {
        closed = true;
        tileType = TileType.NOT_PASSABLE;  
    }

    public void open() {
        closed = false;                   
    }

    public boolean isOpen() {
        return !closed && amountDown == 0;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (closed && amountDown < 1) {
            amountDown = Math.min(1, amountDown + 1f / SLAM_FRAMES);
            if (amountDown == 1) {

                float middle = getX() + getWidth() / 2f, ground = getY() + getHeight();
                GourdProjectile.puff(map, middle - 22, ground, -1.2f);
                GourdProjectile.puff(map, middle + 22, ground, 1.2f);
            }
        } else if (!closed && amountDown > 0) {
            amountDown = Math.max(0, amountDown - 1f / LIFT_FRAMES);
            if (amountDown == 0) {
                tileType = TileType.PASSABLE;
            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (amountDown == 0) {
            return; 
        }
        BufferedImage image = currentFrame.getImage();
        int scale = 3;
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        int w = image.getWidth();


        graphicsHandler.drawImage(image.getSubimage(0, 0, w, LINTEL), x, y, w * scale, LINTEL * scale);


        int barsShown = Math.round((image.getHeight() - LINTEL) * amountDown);
        if (barsShown > 0) {
            graphicsHandler.drawImage(image.getSubimage(0, image.getHeight() - barsShown, w, barsShown),
                    x, y + LINTEL * scale, w * scale, barsShown * scale);
        }
    }
}
