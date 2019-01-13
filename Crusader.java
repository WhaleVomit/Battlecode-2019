package bc19;

import static bc19.Consts.*;

public class Crusader extends Attackable {
    public Crusader(MyRobot z) { super(z); }
    Action2 run() {
        Z.sendToCastle();
        Action2 A = react(); if (A != null) return A;
        return patrol();
        // return aggressive();
    }
}
