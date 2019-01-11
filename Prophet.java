package bc19;

import static bc19.Consts.*;

public class Prophet extends Attackable {
    public Prophet(MyRobot z) { super(z); }

    Action react() {
        Action A = tryAttack(); if (A != null) return A;
        Robot R = Z.closestEnemy(); if (R != null && Z.euclidDist(R) < 16) return moveAway(R);
        return null;
    }

    Action run() {
        Z.castleTalk(0);
        Action A = react(); if (A != null) return A;
        if (Z.turn <= 200) return patrol();
        A = moveTowardCastle(); if (A != null) return A;
        if (Z.otherCastle.size() == 0) { Z.castleTalk(100); return patrol(); }
        return null;
        // return Z.nextMove(Z.closestUnseen());
    }
}
