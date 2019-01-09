package bc19;

import static bc19.Consts.*;

public class Crusader {
    MyRobot Z;

    public Crusader(MyRobot z) {
        this.Z = z;
    }

    Action run() {
        Action A = Z.tryAttack();
        if (A != null) return A;
        A = Z.moveToward(Z.closestEnemy());
        if (A != null) return A;
        A = Z.moveTowardCastle();
        if (A != null) return A;
        return Z.nextMove(Z.closestUnseen());
    }
}
