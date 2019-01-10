package bc19;

import static bc19.Consts.*;

public class Preacher {
    MyRobot Z;

    public Preacher(MyRobot z) {
        this.Z = z;
    }

    int getVal(int x, int y) {
        int t = 0;
        for (int i = x - 1; i <= x + 1; ++i)
            for (int j = y - 1; j <= y + 1; ++j)
                if (Z.valid(i, j) && Z.robotMap[i][j] > 0) {
                    Robot R = Z.getRobot(Z.robotMap[i][j]);
                    int sgn = (R.team == Z.me.team) ? -2 : 1;
                    if (Z.isAttacker(R)) sgn *= 2;
                    else if (Z.isStructure(R)) sgn *= 3;
                    t += sgn;
                }

        return t;
    }

    Action tryAttack() {
        int bes = 0, DX = MOD, DY = MOD;
        for (int dx = -4; dx <= 4; ++dx)
            for (int dy = -4; dy <= 4; ++dy)
                if (Z.canAttack(dx, dy) != -MOD) {
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
        if (A != null) return A;
        A = Z.moveToward(Z.closestEnemy());
        if (A != null) return A;
        if (Z.distHome() > 25) return Z.moveHome();
        return Z.nextMove(Z.closestUnseen());
    }
}
