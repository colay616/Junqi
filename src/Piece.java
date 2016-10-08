import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import javax.imageio.ImageIO;

public class Piece implements Serializable{
    private transient BufferedImage image;
    private boolean isRed;
    private boolean onBoard;
    private Rank rank;
    private Point loc; //for graphics
    private Point oldLoc;
    private Point coords; //for logic
    public static final BufferedImage unknownImage = createUnknownImage();

    public Piece(Rank r, boolean isRed){
        this(r, isRed, new Point());
    }

    public Piece(Rank r, boolean isRed, Point loc){
        rank = r;
        this.isRed = isRed;
        this.loc = loc;
        createImage();
    }

    private void createImage(){
        try {
            image = ImageIO.read(new File("pieces.png")).getSubimage(rank.getImageLocation().x, rank.getImageLocation().y + ((isRed)?0:210), 56, 40);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean contains(Point p){
        return(p.x - loc.x < 55 && p.x >= loc.x && p.y - loc.y <35 && p.y >= loc.y);
    }

    public void snap(){
        for(int x = 38; x < 450; x += 92){
            for(int y = 18; y<610; y+=40){
                if(Math.abs(loc.x - x) < 15 && Math.abs(loc.y - y) < 15){
                    loc = new Point(x,y);
                }
                if(y == 218 || y == 285){
                    y+=27;
                }
                
                if(y > 300){
                    y+=1;
                }
            }
        }
    }
    private static BufferedImage createUnknownImage(){
        try {
            return ImageIO.read(new File("pieces.png")).getSubimage(231, 166, 56, 40);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void recordLoc(){
        oldLoc = loc;
    }
    public void revertLoc(){
        loc = oldLoc;
    }
    
    public boolean removeFromBoard(){
        Random r = new Random();
        loc = new Point(500 + r.nextInt(350), 300 + r.nextInt(300));
        coords = null;
        onBoard = false;
        return rank == Rank.SILING;
    }

    public Point getCoords() {
        return coords;
    }
    
    public Point getOldLoc(){
        return oldLoc;
    }

    public void setCoords(Point coords) {
        this.coords = coords;
    }

    public Point location(){
        return loc;
    }
    public void setLocation(int x, int y){
        loc = new Point(x, y);
    }
    
    public void setLocation(Point p){
        loc = p;
    }

    public Rank getRank(){
        return rank;
    }
    public boolean isRed(){
        return isRed;
    }
    public void setRed(boolean isRed){
        this.isRed = isRed;
    }
    public boolean onBoard(){
        return onBoard;
    } 
    public void setOnBoard(boolean b){
        onBoard = b;
    }
    public void setImage(BufferedImage b){
        this.image = b;
    }
    public BufferedImage getImage(){
        return image;
    }
}
