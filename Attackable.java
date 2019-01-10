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

        if (Z.me.unit == CRUSADER || Z.me.unit == PROPHET) {
	        if (!Z.containsRobot(x, y) || Z.seenRobot[y][x].team == Z.me.team) return -MOD;
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
}