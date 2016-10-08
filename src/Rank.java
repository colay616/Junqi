import java.awt.Point;

public enum Rank{
    BOMB, MINE, SILING, JUNZHANG, SHIZHANG, LUZHANG, TUANZHANG, YINGZHANG, LIANZHANG, PAIZHANG, GONGBING, JUNQI;
    
    public Point getImageLocation(){//location of red pieces, add 210 to y for black pieces
        switch(ordinal()){
        case 0://BOMB
            return new Point(176,0);
        case 1://MINE
            return new Point(116,42);
        case 2://SILING
            return new Point(0,0);
        case 3://JUNZHANG
            return new Point(59,0);
        case 4://SHIZHANG
            return new Point(0,42);
        case 5://LUZHANG
            return new Point(0,85);
        case 6://TUANZHANG
            return new Point(0,127);
        case 7://YINGZHANG
            return new Point(0,168);
        case 8://LIANGZHANG
            return new Point(116,84);
        case 9://PAIZHANG
            return new Point(116,127);
        case 10://GONGBING
            return new Point(116,169);
        case 11://JUNGQI
            return new Point(117,0);
        default:
            return new Point();
        }
    }
}
