package bc19;

import static bc19.Consts.*;

public class Prophet extends Attackable {
    public Prophet(MyRobot z) { super(z); }

    Action run() {
        Action A = react(); if (A != null) return A;
        if (Z.turn <= 200) return patrol();
        A = moveTowardCastle(); if (A != null) return A;
        if (Z.otherCastle.size() == 0) { return patrol(); }
        return null;
        // return Z.nextMove(Z.closestUnseen());
    }
}
