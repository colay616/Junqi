import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Display {
    private JFrame frame;
    private JPanel boardPanel, utilPanel, gPane;
    private JTextArea messageBox;
    private StringBuilder message;
    private JScrollPane messagePane;
    private JButton button1, button2;
    private JMenuBar menuBar;
    private JMenu connect;
    private JMenuItem host, find;
    private Piece[] pieces, enemyPieces;
    private Piece selectedPiece;
    private Board board;
    private boolean isRed, isGameOver;
    private Random r;
    private Connector connector;
    private Point lastMoveStart,lastMoveEnd;

    public boolean yourMove;

    public Display(){
        preinit();
    }

    private void preinit(){
        r = new Random();
        frame = new JFrame("陆战棋");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setSize(800,600);
        menuBar = new JMenuBar();
        connect = new JMenu("Connect");
        host = new JMenuItem("Host Game");
        find = new JMenuItem("Find Game");
        menuBar.add(connect);
        connect.add(host);
        connect.add(find);
        host.addActionListener(e->{
            addMessage("Waiting for Clients...\n");
            connect.setEnabled(false);
            isRed = true;
            connector = new Server(this);
            ((Server)connector).start();
        });
        find.addActionListener(e->{
            addMessage("Searching for Server...\n");
            connect.setEnabled(false);
            isRed = false;
            connector = new Client(this);
            ((Client)connector).start();
        });
        frame.setJMenuBar(menuBar);

        message = new StringBuilder("Use the Connect Menu to Begin\n");
        messageBox = new JTextArea(message.toString());
        messageBox.setEditable(false);
        messagePane = new JScrollPane(messageBox);
        messagePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        messagePane.setPreferredSize(new Dimension(400,250));
        utilPanel = new JPanel();
        utilPanel.setBackground(Color.GRAY);
        utilPanel.setPreferredSize(new Dimension(400,200));
        utilPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.anchor=GridBagConstraints.NORTH;
        c.weighty=1;
        c.gridy=0;
        c.gridwidth=2;
        utilPanel.add(messagePane, c);
        frame.add(utilPanel, BorderLayout.EAST);
        frame.setVisible(true);
    }

    public void init(){
        setUpGame();

        frame.remove(utilPanel);
        frame.setJMenuBar(null);
        addMessage("Please Set Up your Pieces\n");

        gPane=new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                if(enemyPieces != null){
                    for(int i=0; i<enemyPieces.length; i++){
                        if(enemyPieces[i].onBoard()){
                            if(isGameOver || (enemyPieces[i].getRank() == Rank.JUNQI && board.revealEnemyFlag())){
                                g2.drawImage(enemyPieces[i].getImage(), enemyPieces[i].location().x, enemyPieces[i].location().y, 55, 35, null);
                            } else {
                                g2.drawImage(Piece.unknownImage, enemyPieces[i].location().x, enemyPieces[i].location().y, 55, 35, null);
                            }
                        }
                    }
                }

                for(int i=0; i<pieces.length; i++){
                    g2.drawImage(pieces[i].getImage(), pieces[i].location().x, pieces[i].location().y, 55, 35, null);
                }

                if(lastMoveStart != null && lastMoveEnd != null){
                    g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
                    g2.drawLine(lastMoveStart.x + 27, lastMoveStart.y + 17, lastMoveEnd.x + 27, lastMoveEnd.y + 17);
                    AffineTransform tx = new AffineTransform();
                    Polygon arrowHead = new Polygon();  
                    arrowHead.addPoint( 0,5);
                    arrowHead.addPoint( -5, -5);
                    arrowHead.addPoint( 5,-5);
                    tx.setToIdentity();
                    double angle = Math.atan2(lastMoveEnd.y- lastMoveStart.y, lastMoveEnd.x - lastMoveStart.x);
                    tx.translate(lastMoveEnd.x + 27, lastMoveEnd.y + 17);
                    tx.rotate((angle-Math.PI/2d));  
                    g2.setTransform(tx);   
                    g2.fill(arrowHead);
                }
                g2.dispose();
            }
        };
        gPane.setLayout(new BorderLayout());
        frame.setGlassPane(gPane);

        boardPanel = new JPanel();
        boardPanel.setPreferredSize(new Dimension(500,600));
        boardPanel.setBackground(new Color(210 + ((isRed)?20:0),210,210 + ((isRed)?0:20)));
        BufferedImage boardImage;
        try {
            boardImage = ImageIO.read(new File("board.png"));
            boardPanel.add(new JLabel(new ImageIcon(boardImage)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        button1 = new JButton("Randomize");
        button1.setPreferredSize(new Dimension(150,50));
        button1.setMaximumSize(new Dimension(150,50));
        button1.setSize(new Dimension(150,50));
        button2 = new JButton("Ready");
        button2.setPreferredSize(new Dimension(150,50));
        button2.setMaximumSize(new Dimension(150,50));
        button2.setSize(new Dimension(150,50));
        utilPanel = new JPanel();
        utilPanel.setBackground(Color.GRAY);
        utilPanel.setPreferredSize(new Dimension(400,200));
        utilPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.anchor=GridBagConstraints.NORTH;
        c.weighty=1;
        c.gridy=0;
        c.gridwidth=2;
        utilPanel.add(messagePane, c);
        c.anchor=GridBagConstraints.BELOW_BASELINE;
        c.gridwidth=1;
        c.weighty=0;
        c.gridy=1;
        utilPanel.add(button1, c);
        c.gridx=1;
        utilPanel.add(button2, c);
        frame.add(boardPanel, BorderLayout.WEST);
        frame.add(utilPanel, BorderLayout.EAST);

        button1.addActionListener(e -> {
            addMessage("Position Randomized\n");
            randomizePosition();
        });
        button2.addActionListener(e-> {
            for(Piece p : pieces){
                if(p.getCoords() == null){
                    addMessage("Please Finish Setting Up\n");
                    return;
                }
            }
            addMessage("Waiting for your Opponent to Finish...\n");
            board.endSetpUp();
            button1.setVisible(false);
            button2.setVisible(false);
            for(Piece p : pieces){
                p.setOnBoard(true);
            }
            connector.sendInitial(pieces);
            connector.getInitial();
        });

        gPane.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                Point glassPanePoint = e.getPoint();
                Point containerPoint = SwingUtilities.convertPoint(gPane,  glassPanePoint, utilPanel);
                if(messagePane.contains(containerPoint.x, containerPoint.y)){//TODO
                    Point componentPoint = SwingUtilities.convertPoint(
                            gPane,
                            glassPanePoint,
                            messagePane);
                    messagePane.dispatchEvent(new MouseEvent(messagePane,
                            e.getID(),
                            e.getWhen(),
                            e.getModifiers(),
                            componentPoint.x,
                            componentPoint.y,
                            e.getClickCount(),
                            e.isPopupTrigger()));
                }

                if(selectedPiece!=null){
                    selectedPiece.setLocation(Math.max(0, Math.min(865, e.getX()-27)), Math.max(0, Math.min(575, e.getY()-17)));
                    //selectedPiece.setCoords(Math.max(-selectedPiece.location().x/5,Math.min(e.getX()-selectedPiece.location().x/2, frame.getWidth()-4*selectedPiece.location().x/5)),
                    //        Math.max(-selectedPiece.location().y/5, Math.min(frame.getHeight()-selectedPiece.location().y,e.getY()-selectedPiece.location().y/2)));
                    gPane.repaint();
                }
            }
            public void mouseMoved(MouseEvent e) {}
        });

        gPane.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)){
                    Point glassPanePoint = e.getPoint();
                    Container container = frame.getContentPane();
                    Point containerPoint = SwingUtilities.convertPoint(gPane,  glassPanePoint, container);
                    Component component = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);

                    if (component != null && (component.equals(button1) || component.equals(button2))) {
                        ((JButton) component).doClick();
                    } 
                }
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)){
                    for(int i=pieces.length-1; i>=0; i--)
                        if(pieces[i].contains(e.getPoint())){
                            selectedPiece=pieces[i];
                            selectedPiece.recordLoc();
                            movePieceToFront(i);
                            break;
                        }
                }
            }
            public void mouseReleased(MouseEvent e) {
                if(selectedPiece!=null){
                    selectedPiece.snap();
                    if(!isGameOver && !board.move(selectedPiece.getCoords(), locToCoords(selectedPiece.location()), selectedPiece)){
                        selectedPiece.revertLoc();
                    }
                }
                selectedPiece=null;
                frame.repaint();
            }
        });

        frame.pack();
        gPane.setVisible(true);
        gPane.setOpaque(false);
        frame.setVisible(true);
    }

    public void makeEnemyMove(Point start, Point end){
        board.get(start.x, start.y).setLocation(coordsToLoc(end));
        board.move(start, end, board.get(start.x, start.y));
        gPane.repaint();
        yourMove = true;
    }

    public void legalMoveMade(Point start, Point end){
        connector.sendMove(start, end);
            yourMove = false;
            connector.getEnemyMove();
    }

    public void setUpGame(){
        board = new Board(isRed, this);
        pieces = new Piece[25];
        pieces[0] = new Piece(Rank.JUNQI, isRed, new Point(500, 350));
        pieces[1] = new Piece(Rank.BOMB, isRed, new Point(720, 350));
        pieces[2] = new Piece(Rank.BOMB, isRed, new Point(775, 350));
        pieces[3] = new Piece(Rank.MINE, isRed, new Point(555, 350));
        pieces[4] = new Piece(Rank.MINE, isRed, new Point(610, 350));
        pieces[5] = new Piece(Rank.MINE, isRed, new Point(665, 350));
        pieces[6] = new Piece(Rank.SILING, isRed, new Point(500, 385));
        pieces[7] = new Piece(Rank.JUNZHANG, isRed, new Point(555, 385));
        pieces[8] = new Piece(Rank.SHIZHANG, isRed, new Point(610, 385));
        pieces[9] = new Piece(Rank.SHIZHANG, isRed, new Point(665, 385));
        pieces[10] = new Piece(Rank.LUZHANG, isRed, new Point(500, 420));
        pieces[11] = new Piece(Rank.LUZHANG, isRed, new Point(555, 420));
        pieces[12] = new Piece(Rank.TUANZHANG, isRed, new Point(610, 420));
        pieces[13] = new Piece(Rank.TUANZHANG, isRed, new Point(665, 420));
        pieces[14] = new Piece(Rank.YINGZHANG, isRed, new Point(500, 455));
        pieces[15] = new Piece(Rank.YINGZHANG, isRed, new Point(555, 455));
        pieces[16] = new Piece(Rank.LIANZHANG, isRed, new Point(610, 455));
        pieces[17] = new Piece(Rank.LIANZHANG, isRed, new Point(665, 455));
        pieces[18] = new Piece(Rank.LIANZHANG, isRed, new Point(720, 455));
        pieces[19] = new Piece(Rank.PAIZHANG, isRed, new Point(500, 490));
        pieces[20] = new Piece(Rank.PAIZHANG, isRed, new Point(555, 490));
        pieces[21] = new Piece(Rank.PAIZHANG, isRed, new Point(610, 490));
        pieces[22] = new Piece(Rank.GONGBING, isRed, new Point(665, 490));
        pieces[23] = new Piece(Rank.GONGBING, isRed, new Point(720, 490));
        pieces[24] = new Piece(Rank.GONGBING, isRed, new Point(775, 490));
    }
    public void addMessage(String s){
        message.append(s);
        messageBox.setText(message.toString());
    }

    public void movePieceToFront(int index){
        for(int i=index; i<pieces.length-1; i++){
            Piece temp=pieces[i];
            pieces[i]=pieces[i+1];
            pieces[i+1]=temp;
        }
    }

    public Point locToCoords(Point loc){
        int i = 0, j = 0;
        for(int x = 38; x < 450; x += 92, i++){
            j=0;
            for(int y = 18; y<610; y+=40, j++){
                if(Math.abs(loc.x - x) < 15 && Math.abs(loc.y - y) < 15){
                    return new Point(i,j);
                }
                if(y == 218 || y == 285){
                    y+=27;
                }

                if(y > 300){
                    y+=1;
                }
            }
        }
        return null;
    }

    public Point coordsToLoc(Point coords){
        int i = 0, j = 0;
        for(int x = 38; x < 450; x += 92, i++){
            j=0;
            for(int y = 18; y<610; y+=40, j++){
                if(coords.x == i && coords.y == j){
                    return new Point(x,y);
                }
                if(y == 218 || y == 285){
                    y+=27;
                }

                if(y > 300){
                    y+=1;
                }
            }
        }
        return null;
    }

    public void randomizePosition(){
        boolean hasRandomized = false;
        for(int i = 0; i < pieces.length; i++){
            if(pieces[i].getCoords() == null){
                randomizePosition(pieces[i]);
                hasRandomized = true;
            }
        }
        if(!hasRandomized){
            for(int i = 0; i < pieces.length; i++){
                board.set(pieces[i].getCoords().x, pieces[i].getCoords().y, null);
                pieces[i].removeFromBoard();
            }
            for(int i = 0; i < pieces.length; i++){
                randomizePosition(pieces[i]);
            }
        }
    }

    public void randomizePosition(Piece p){
        int x;
        int y;
        if(p.getRank() == Rank.JUNQI){
            if(board.get(1, 12) != null && board.get(3,  12) != null){
                x = r.nextInt(2);
                p.setCoords(new Point(1+2*x, 12));
                p.setLocation(coordsToLoc(p.getCoords()));
                randomizePosition(board.set(1 + 2*x, 12, p));
            } else{
                do{
                    x = r.nextInt(2);
                } while(board.get(1+2*x, 12) != null);
                board.set(1+2*x, 12, p);
                p.setCoords(new Point(1+2*x, 12));
                p.setLocation(coordsToLoc(p.getCoords()));
            }
        } else if (p.getRank() == Rank.BOMB){
            if(board.isFull(8) && board.isFull(9) && board.isFull(10) && board.isFull(11) && board.isFull(12)){
                do{
                    x = r.nextInt(5);
                    y = 12 - r.nextInt(5);
                } while(Board.terrain[y][x] == Position.BUNKER);
                p.setCoords(new Point(x,y));
                p.setLocation(coordsToLoc(p.getCoords()));
                randomizePosition(board.set(x,y,p));
            } else{
                do{
                    x = r.nextInt(5);
                    y = 12 - r.nextInt(5);
                } while(Board.terrain[y][x] == Position.BUNKER || board.get(x, y) != null);
                p.setCoords(new Point(x,y));
                p.setLocation(coordsToLoc(p.getCoords()));
                board.set(x, y, p);
            }
        } else if (p.getRank() == Rank.MINE){
            if(board.isFull(11) && board.isFull(12)){
                x = r.nextInt(5);
                y = 12 - r.nextInt(2);
                p.setCoords(new Point(x,y));
                p.setLocation(coordsToLoc(p.getCoords()));
                randomizePosition(board.set(x,y,p));
            } else{
                do{
                    x = r.nextInt(5);
                    y = 12 - r.nextInt(2);
                } while(board.get(x, y) != null);
                p.setCoords(new Point(x,y));
                p.setLocation(coordsToLoc(p.getCoords()));
                board.set(x, y, p);
            }
        } else {
            do{
                x = r.nextInt(5);
                y = 12 - r.nextInt(6);
            } while(Board.terrain[y][x] == Position.BUNKER || board.get(x, y) != null);
            p.setCoords(new Point(x,y));
            p.setLocation(coordsToLoc(p.getCoords()));
            board.set(x, y, p);
        }
    }

    public void loadEnemyPieces(Piece[] p){
        enemyPieces = p;
        for(Piece pc: enemyPieces){
            //pc.setRed(!isRed);
            board.set(pc.getCoords().x, pc.getCoords().y, pc);
        }
        gPane.repaint();
        addMessage("Game Commencing\n");
        addMessage((isRed)?"Your Turn to Play\n":"Your Opponent is Thinking...\n");
        yourMove = isRed;
        if(!yourMove){
            connector.getEnemyMove();
        }
    }

    public void revealEnemyFlag(){
        for(Piece p: enemyPieces){
            if(p.getRank() == Rank.JUNQI){
                try {
                    p.setImage(ImageIO.read(new File("pieces.png")).getSubimage(Rank.JUNQI.getImageLocation().x,
                            Rank.JUNQI.getImageLocation().y + ((!isRed)?0:210), 56, 40));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setLastMove(Point start, Point end){
        lastMoveStart = start;
        lastMoveEnd = end;
    }

    public void alertGameOver(){
        connector.terminate();
        isGameOver = true;
        for(Piece p : enemyPieces){
            try {
                p.setImage(ImageIO.read(new File("pieces.png")).getSubimage(p.getRank().getImageLocation().x,
                        p.getRank().getImageLocation().y + ((!isRed)?0:210), 56, 40));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        new Display();
    }
}
