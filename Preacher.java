package bc19;

import static bc19.Consts.*;

public class Preacher extends Attackable {
    public Preacher(MyRobot z) { super(z); }

    public Action run() {
        Action A = react(); if (A != null) return A;
        int pos = Z.getClosestCastle(false);
        if(Z.euclidDist((pos-(pos%64))/64,pos%64) > 16) {
			A = moveTowardCastle();
			if (A != null) return A;
			return nextMove(Z.closestUnseen());
		}
    }
}
