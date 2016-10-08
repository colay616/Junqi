import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread implements Connector{
    ServerSocket server;
    Socket client;
    Display d;
    
    public Server(Display d){
        this.d=d;
    }


    public void run(){
        try {
            server = new ServerSocket(1415);
            client = server.accept();
            d.addMessage("Connected to " + client.getInetAddress() + "\n");
            d.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Thread receivingThread = new Thread(){
            StringBuffer rec;
            Boolean stopping;

            public void run(){
                InputStream input;
                try {
                    input = client.getInputStream();
                    while(true){
                        synchronized(stopping) {
                            if(stopping)
                                break;
                        }

                        int ch = input.read();
                        if(ch >= 0)
                        {
                            synchronized(rec)
                            {
                                rec.append((char) ch);
                                rec.notify();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void finish(){
                synchronized(stopping){
                    stopping = true;
                }
            }


        };
        receivingThread.start();*/
    }

    public void getInitial(){
        new Thread(){
            public void run(){
                try {
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
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

    public void getEnemyMove(){
        new Thread(){
            public void run(){
                try {
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
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

    public void sendInitial(Piece[] pieces){
        try {
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            out.writeObject(pieces);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void sendMove(Point start, Point end){
        try {
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            out.writeObject(new Point[]{start,end});
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    public void terminate(){
        try {
            if(!client.isClosed()){
            client.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
