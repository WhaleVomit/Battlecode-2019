package bc19;

import static bc19.Consts.*;

public class Crusader extends Attackable {
    public Crusader(MyRobot z) { super(z); }

    Action run() {
        Action A = tryAttack();
        if (A != null) return A;
        A = moveToward(Z.closestEnemy());
        if (A != null) return A;
        A = moveTowardCastle();
        if (A != null) return A;
        return nextMove(Z.closestUnseen());
    }
}
