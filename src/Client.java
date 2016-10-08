import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread implements Connector{
    Socket server;
    String ip;
    Display d;
    
    public Client(Display d){
        this.d = d;
        
        ip = "127.0.0.1";
        //ip = "143.215.127.103";
        //ip = "128.84.127.117"; EL
    }
    public Client(Display d, String ip){
        this.d = d;
        this.ip = ip;
    }
    
    public void run(){
        try {
            server = new Socket(ip, 1415);
            d.addMessage("Connected to " + server.getInetAddress() + "\n");
            d.init();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getInitial() {
        new Thread(){
            public void run(){
                try {
                    ObjectInputStream in = new ObjectInputStream(server.getInputStream());
                    Piece[] temp = (Piece[]) in.readObject();
                    for(Piece p: temp){
                        p.setCoords(Board.flipCoords(p.getCoords()));
                        p.setLocation(d.coordsToLoc(p.getCoords()));
                    }
                    d.loadEnemyPieces(temp);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        
    }

    @Override
    public void getEnemyMove() {
        new Thread(){
            public void run(){
                try {
                    ObjectInputStream in = new ObjectInputStream(server.getInputStream());
                    Point[] points = (Point[]) in.readObject();
                    Point start = Board.flipCoords(points[0]);
                    Point end = Board.flipCoords(points[1]);
                    d.makeEnemyMove(start, end);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void sendInitial(Piece[] pieces) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            out.writeObject(pieces);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMove(Point start, Point end) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            out.writeObject(new Point[]{start,end});
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void terminate(){
        try {
            if(!server.isClosed()){
            server.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
