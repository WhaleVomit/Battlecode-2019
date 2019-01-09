package bc19;

import static bc19.Consts.*;

public class Prophet {

    MyRobot Z;

    public Prophet(MyRobot z) {
        this.Z = z;
    }

    Action run() {
        Action A = Z.tryAttack();
        if (A != null) return A;
        Robot R = Z.closestEnemy();
        if (R != null) {
            if (Z.dist(R) > 64) A = Z.moveToward(R);
            if (Z.dist(R) < 16) A = Z.moveAway(R);
            return A;
        }
        A = Z.moveTowardCastle();
        if (A != null) return A;
        return Z.nextMove(Z.closestUnseen());
    }
}
