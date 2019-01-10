package bc19;

import static bc19.Consts.*;

public class Preacher extends Attackable {
    public Preacher(MyRobot z) { super(z); }

    int getVal(int x, int y) {
        int t = 0;
        for (int i = x - 1; i <= x + 1; ++i) 
            for (int j = y - 1; j <= y + 1; ++j) 
                if (Z.valid(i, j) && Z.seenMap[j][i] > 0) {
                    Robot R = Z.seenRobot[j][i];
                    int val = attackPriority(R);
                    val *= (R.team == Z.me.team) ? -2 : 1;
                    if (Z.isStructure(R)) val *= 2;
                    t += val;
                }
        return t;
    }

    Action tryAttack() {
        int bes = 0, DX = MOD, DY = MOD;
        for (int dx = -4; dx <= 4; ++dx)
            for (int dy = -4; dy <= 4; ++dy)
                if (canAttack(dx, dy) != -MOD) {
                    int t = getVal(Z.me.x + dx, Z.me.y + dy);
                    if (t > bes) {
                        bes = t;
                        DX = dx;
                        DY = dy;
                    }
                }
        if (bes == 0) return null;
        return Z.attack(DX, DY);
    }

    public Action run() {
        Action A = tryAttack();
        if (A != null) {
			return A;
		}
        int pos = Z.getClosestCastle(false);
        if(Z.euclidDist((pos-(pos%64))/64,pos%64) > 16) {
			A = moveTowardCastle();
			if (A != null) return A;
			return nextMove(Z.closestUnseen());
		}
    }
}
