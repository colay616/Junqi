import java.awt.Point;

public interface Connector {
    public void getInitial();
    public void getEnemyMove();
    public void sendInitial(Piece[] pieces);
    public void sendMove(Point start, Point end);
    public void terminate();
}
