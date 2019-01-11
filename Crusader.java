package bc19;

import static bc19.Consts.*;

public class Crusader extends Attackable {
    public Crusader(MyRobot z) { super(z); }

    Action run() {
    	Action A = react(); if (A != null) return A;
    	// if (Z.turn <= 100 || Z.me.team == 1) return patrol();
        A = moveTowardCastle(); if (A != null) return A;
        return nextMove(Z.closestUnseen());
    }
}
