package bc19;

import static bc19.Consts.*;

public class Prophet extends Attackable {
    public Prophet(MyRobot z) { super(z); }

    Action run() {
    	Z.sendToCastle();
        Action A = react(); if (A != null) return A;
        return patrol();
        /*A = moveEnemy(); if (A != null) return A;
        if (Z.otherCastle.size() == 0) { return patrol(); }
        return null;*/
        // return Z.nextMove(Z.closestUnseen());
    }
}
