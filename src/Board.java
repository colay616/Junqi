import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Board {
    private Piece[][] grid;
    private Display d;
    private boolean settingUp;
    private boolean isRed;
    private boolean flagRevealed;

    public static final Position[][] terrain = { //coords (y,x)
            {Position.STATION, Position.CAMP, Position.STATION, Position.CAMP, Position.STATION},
            {Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD},
            {Position.RAILROAD, Position.BUNKER, Position.STATION, Position.BUNKER, Position.RAILROAD},
            {Position.RAILROAD, Position.STATION, Position.BUNKER, Position.STATION, Position.RAILROAD},
            {Position.RAILROAD, Position.BUNKER, Position.STATION, Position.BUNKER, Position.RAILROAD},
            {Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD},
            {Position.RAILROAD, Position.MOUNTAIN, Position.RAILROAD, Position.MOUNTAIN, Position.RAILROAD},
            {Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD},
            {Position.RAILROAD, Position.BUNKER, Position.STATION, Position.BUNKER, Position.RAILROAD},
            {Position.RAILROAD, Position.STATION, Position.BUNKER, Position.STATION, Position.RAILROAD},
            {Position.RAILROAD, Position.BUNKER, Position.STATION, Position.BUNKER, Position.RAILROAD},
            {Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD, Position.RAILROAD},
            {Position.STATION, Position.CAMP, Position.STATION, Position.CAMP, Position.STATION}
    };

    public Board(boolean isRed, Display d){
        this.d = d;
        this.isRed = isRed;
        settingUp = true;
        grid = new Piece[5][13];
    }

    public boolean move(Point start, Point end, Piece p){
        if(end == null){
            if(start != null && !p.onBoard()){
                grid[start.x][start.y] = null;
                p.setCoords(null);
            }
            return !p.onBoard();
        }
        if(!settingUp && p.isRed() == isRed && !d.yourMove){
            d.addMessage("Not your Move!\n");
            return false;
        }
        if(!moveIsValid(start, end, p)){
            return false;
        }

        Piece old = grid[end.x][end.y];
        grid[end.x][end.y] = p;
        if(start != null){
            grid[start.x][start.y] = null;
        }
        p.setCoords(end);

        if(old != null){
            if(old.isRed() == p.isRed()){//setup only
                old.setCoords(start);
                old.setLocation(p.getOldLoc());
                if(start != null){
                    grid[start.x][start.y] = old;
                }
            } else{//combat
                if(old.getRank() == Rank.JUNQI){//game over
                    old.removeFromBoard();
                    if(old.isRed() == isRed){
                        d.addMessage("You Have Been Defeated!\n");
                    } else {
                        d.addMessage("You Win!\n");
                    }
                    d.alertGameOver();
                } else if(old.getRank() == Rank.BOMB || p.getRank() == Rank.BOMB){
                    grid[end.x][end.y] = null;
                    d.addMessage("Both Pieces were Slain\n");
                    if(old.removeFromBoard()){
                        if(old.isRed() != isRed){
                            flagRevealed = true;
                            d.revealEnemyFlag();
                            d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                        } else {
                            d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                        }
                    }
                    if(p.removeFromBoard()){
                        if(p.isRed() != isRed){
                            flagRevealed = true;
                            d.revealEnemyFlag();
                            d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                        } else {
                            d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                        }
                    }
                } else if(old.getRank() == Rank.MINE){
                    if(p.getRank() == Rank.GONGBING){
                        old.removeFromBoard();
                        d.addMessage((old.isRed() == isRed)?"An Allied " + old.getRank() + " was Slain\n": "An Enemy has been Slain\n");
                    } else{
                        grid[end.x][end.y] = old;
                        d.addMessage((p.isRed() == isRed)?"An Allied " + p.getRank() + " was Slain\n": "An Enemy has been Slain\n");
                        if(p.removeFromBoard()){
                            if(p.isRed() != isRed){
                                flagRevealed = true;
                                d.revealEnemyFlag();
                                d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                            } else {
                                d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                            }
                        }
                    }
                } else {
                    if(old.getRank().ordinal() < p.getRank().ordinal()){//old wins
                        grid[end.x][end.y] = old;
                        d.addMessage((p.isRed() == isRed)?"An Allied " + p.getRank() + " was Slain\n": "An Enemy has been Slain\n");
                        if(p.removeFromBoard()){
                            if(p.isRed() != isRed){
                                flagRevealed = true;
                                d.revealEnemyFlag();
                                d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                            } else {
                                d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                            }
                        }
                    } else if(old.getRank().ordinal() > p.getRank().ordinal()){//p wins
                        d.addMessage((old.isRed() == isRed)?"An Allied " + old.getRank() + " was Slain\n": "An Enemy has been Slain\n");
                        if(old.removeFromBoard()){
                            if(old.isRed() != isRed){
                                flagRevealed = true;
                                d.revealEnemyFlag();
                                d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                            } else {
                                d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                            }
                        }
                    } else{//draw
                        grid[end.x][end.y] = null;
                        d.addMessage("Both Pieces were Slain\n");
                        if(old.removeFromBoard()){
                            if(old.isRed() != isRed){
                                flagRevealed = true;
                                d.revealEnemyFlag();
                                d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                            } else {
                                d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                            }
                        }
                        if(p.removeFromBoard()){
                            if(p.isRed() != isRed){
                                flagRevealed = true;
                                d.revealEnemyFlag();
                                d.addMessage("The Enemy Si Ling is dead. Their Flag has been Revealed!\n");
                            } else {
                                d.addMessage("Your Si Ling is dead. Your Flag has been Revealed!\n");
                            }
                        }
                    }
                }
            }
        }
        /*grid[end.x][end.y] = p;
        if(start != null && grid[start.x][start.y] == p){
            grid[start.x][start.y] = null;
        }
        p.setCoords(end);*/
        return true;
    }

    public boolean moveIsValid(Point start, Point end, Piece p){
        if(settingUp){
            if(end.y < 7){
                d.addMessage("Pieces must begin on your side\n");
                return false;
            }
            if(terrain[end.y][end.x] == Position.BUNKER){
                d.addMessage("Pieces cannot begin in Bunkers\n");
                return false;
            }
            if(p.getRank() == Rank.BOMB && end.y == 7){
                d.addMessage("Bombs cannot begin the front row\n");
                return false;
            }
            if(p.getRank() == Rank.MINE && end.y < 11){
                d.addMessage("Mines may only be placed in the last two rows\n");
                return false;
            }
            if(p.getRank() == Rank.JUNQI && terrain[end.y][end.x] != Position.CAMP){
                d.addMessage("Flags may only be placed in the main camps\n");
                return false;
            }
            Piece old = grid[end.x][end.y];
            if(old != null && start != null){
                if(old.getRank() == Rank.BOMB && start.y == 7){
                    d.addMessage("Invalid Swap\n");
                    return false;
                }
                if(old.getRank() == Rank.MINE && start.y < 11){
                    d.addMessage("Invalid Swap\n");
                    return false;
                }
                if(old.getRank() == Rank.JUNQI && terrain[start.y][start.x] != Position.CAMP){
                    d.addMessage("Invalid Swap\n");
                    return false;
                }
            }
        }else{
            if(start == null){
                d.addMessage("This Piece is already dead\n");
                return false;
            }
            if(start.x == end.x && start.y == end.y){
                return false;
            }
            if(p.getRank() == Rank.MINE){
                d.addMessage("Mines cannot move\n");
                return false;
            }
            if(terrain[end.y][end.x] == Position.MOUNTAIN){
                d.addMessage("Cannot move into mountains\n");
                return false;
            }

            if(grid[end.x][end.y] != null && (grid[end.x][end.y].isRed() == p.isRed() || terrain[end.y][end.x] == Position.BUNKER)){
                d.addMessage("Target Space Blocked\n");
                return false;
            }
            if(terrain[start.y][start.x] == Position.CAMP){
                d.addMessage("Cannot move out of main camps\n");
                return false;
            }
            if((terrain[start.y][start.x] == Position.BUNKER || terrain[end.y][end.x] == Position.BUNKER) &&
                    (Math.abs(start.x - end.x) > 1 || Math.abs(start.y - end.y) > 1)){
                d.addMessage("Invalid Move\n");
                return false;
            }
            if((terrain[start.y][start.x] == Position.STATION || terrain[end.y][end.x] == Position.STATION ||
                    terrain[end.y][end.x] == Position.CAMP) && Math.abs(start.x - end.x) + Math.abs(start.y - end.y) != 1){
                d.addMessage("Invalid Move\n");
                return false;
            }
            if(terrain[start.y][start.x] == Position.RAILROAD && terrain[end.y][end.x] == Position.RAILROAD){
                if(p.getRank() != Rank.GONGBING){
                    if(start.x != end.x && start.y != end.y){
                        d.addMessage("Invalid Move\n");
                        return false;
                    }
                    int tempx = start.x;
                    int tempy = start.y;
                    while(tempx != end.x || tempy != end.y){
                        if(tempx != end.x){
                            tempx+= (tempx > end.x)?-1:1;
                        } else{
                            tempy+= (tempy > end.y)?-1:1;
                        }
                        if((tempx != end.x || tempy != end.y) && grid[tempx][tempy] != null || terrain[tempy][tempx] != Position.RAILROAD){
                            d.addMessage("Path Blocked\n");
                            return false;
                        }
                    }
                } else if(isBlocked(start, end)){
                    d.addMessage("Path Blocked\n");
                    return false;
                }
            }
            if(p.isRed() == isRed){
                d.legalMoveMade(start, end);
            }
            d.setLastMove(d.coordsToLoc(start), d.coordsToLoc(end));
        }
        return true;
    }

    public static Point flipCoords(Point coords){
        return new Point(4-coords.x, 12-coords.y);
    }

    public boolean isBlocked(Point s, Point e){
        Queue<Point> queue = new LinkedList<>();
        HashSet<Point> traversed = new HashSet<>();
        traversed.add(s);
        do{
            for(int i = -1; i<=1; i+=2){
                if(isValid(s.x+i, s.y)){
                    Point temp = new Point(s.x+i, s.y);
                    if(temp.equals(e)){
                        return false;
                    }
                    if(!traversed.contains(temp) && terrain[temp.y][temp.x] == Position.RAILROAD && grid[temp.x][temp.y] == null){
                        traversed.add(temp);
                        queue.add(temp);
                    }
                }
                if(isValid(s.x, s.y+i)){
                    Point temp = new Point(s.x, s.y+i);
                    if(temp.equals(e)){
                        return false;
                    }
                    if(!traversed.contains(temp) && terrain[temp.y][temp.x] == Position.RAILROAD && grid[temp.x][temp.y] == null){
                        traversed.add(temp);
                        queue.add(temp);
                    }
                }
            }
            s = queue.poll();
        } while(s != null);
        return true;
    }

    public boolean isValid(int x, int y){
        return x<grid.length && x >= 0 && y >=0 && y<grid[x].length;
    }

    public boolean isFull(int row){ //checks if a row is full, for setup purposes
        for(int i=0; i<grid.length; i++){
            if(grid[i][row] == null && terrain[row][i] != Position.BUNKER){
                return false;
            }
        }
        return true;
    }
    public void endSetpUp(){
        settingUp = false;
    }

    public Piece get(int x, int y){
        if(!isValid(x,y)){
            return null;
        }
        return grid[x][y];
    }
    
    public Piece set(int x, int y, Piece p){
        Piece temp = grid[x][y];
        grid[x][y] = p;
        return temp;
    }
    
    public boolean revealEnemyFlag(){
        return flagRevealed;
    }

    public String toString(){
        StringBuffer s = new StringBuffer();
        for(int i=0; i<grid.length; i++){
            for(int j=0; j<grid[i].length; j++){
                s.append((grid[i][j]==null)?"null,":grid[i][j].getRank() + ",");
            }
            s.append("\n");
        }
        return s.toString();
    }
}
