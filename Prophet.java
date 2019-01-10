package bc19;

import static bc19.Consts.*;

public class Prophet extends Attackable {
    public Prophet(MyRobot z) { super(z); }

    Action react() {
        Action A = tryAttack(); if (A != null) return A;
        Robot R = Z.closestEnemy();
        if (R != null && Z.euclidDist(R) < 16) return moveAway(R);
        return null;
    }

    Action patrol() {
        int x = Z.closest(Z.myCastle); if (x == MOD) return null;
        if (Z.bfsDist(x) > 4) return moveHome();
        int y = x % 64; x = Z.fdiv(x,64); 
        if ((Z.me.x+Z.me.y) % 2 == 0) return null;

        int bestDist = MOD, bestPos = MOD;
        for (int X = x-5; X <= x+5; ++X) 
            for (int Y = y-5; Y <= y+5; ++Y) 
                if (Z.passable(X,Y) && (X+Y) % 2 == 0 && (Z.sq(X-x)+Z.sq(Y-y) < bestDist) && Z.dist[Y][X] < 5) {
                    if (Z.sq(X-x)+Z.sq(Y-y) <= 2 && Z.numOpen(x) <= 2) continue;
                    bestDist = Z.sq(X-x)+Z.sq(Y-y);
                    bestPos = 64*X+Y;
                }

        return nextMove(bestPos);
    }

    Action run() {
        Z.castleTalk(0);
        Action A = react(); if (A != null) return A;
        if (Z.turn <= 100) return patrol();
        A = moveTowardCastle(); if (A != null) return A;
        if (Z.otherCastle.size() == 0) {
            Z.castleTalk(100);
            return patrol();
        }
        return null;
        // return Z.nextMove(Z.closestUnseen());
    }
}
