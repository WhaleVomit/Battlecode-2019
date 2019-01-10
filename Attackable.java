package bc19;

import static bc19.Consts.*;

public class Attackable extends Movable {
    public Attackable(MyRobot z) { super(z); }

    int attackPriority(Robot R) {
        if (R.unit == PREACHER) return 5;
        if (R.unit == PROPHET) return 4;
        if (R.unit == CRUSADER) return 3;
        if (R.unit == PILGRIM) return 2;
        return 1;
    }
    
    int canAttack(int dx, int dy) {
        if (ATTACK_F_COST[Z.me.unit] > Z.fuel) return -MOD;
        int dist = dx * dx + dy * dy;
        if (Z.me.unit == CRUSADER) {
            if (dist < 1 || dist > 16) return -MOD;
        } else if (Z.me.unit == PROPHET) {
            if (dist < 16 || dist > 64) return -MOD;
        } else if (Z.me.unit == PREACHER) {
            if (dist < 3 || dist > 16) return -MOD;
        } else  return -MOD;

        int x = Z.me.x + dx, y = Z.me.y + dy;
        if (!Z.containsRobot(x, y)) return -MOD;

        if (Z.me.unit == CRUSADER || Z.me.unit == PROPHET) {
	        if (Z.seenRobot[y][x].team == Z.me.team) return -MOD;
            return attackPriority(Z.seenRobot[y][x]);
        } else { // PREACHER
            if (dist < 3 || dist > 16) return -MOD;
            return 1;
        }
    }

    Action tryAttack() {
        int besPri = -MOD; Action bes = null;
        for (int dx = -8; dx <= 8; ++dx)
            for (int dy = -8; dy <= 8; ++dy) {
                int t = canAttack(dx, dy);
                if (t > besPri) {
                    besPri = t;
                    bes = Z.attack(dx,dy);
                }
            }
        return bes;
    }

    Action patrol() {
        int t = Z.closest(Z.myCastle); if (t == MOD) return null;
        if (Z.bfsDist(t) > 4) return moveHome();
        int y = t % 64; int x = Z.fdiv(t,64); 
        if ((Z.me.x+Z.me.y-x-y) % 2 == 0) return null;

        int bestDist = MOD, bestPos = MOD;
        for (int X = x-5; X <= x+5; ++X) 
            for (int Y = y-5; Y <= y+5; ++Y) 
                if (Z.passable(X,Y) && (X+Y-x-y) % 2 == 0 && (Z.sq(X-x)+Z.sq(Y-y) < bestDist) && Z.dist[Y][X] < 5) {
                    if (Z.sq(X-x)+Z.sq(Y-y) <= 2 && Z.numOpen(t) <= 2) continue;
                    bestDist = Z.sq(X-x)+Z.sq(Y-y);
                    bestPos = 64*X+Y;
                }

        return nextMove(bestPos);
    }
}