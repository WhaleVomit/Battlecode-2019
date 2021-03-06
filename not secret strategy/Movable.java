package bc19;

import static bc19.Consts.*;

public class Movable {
    public MyRobot Z;
    public Movable (MyRobot z) { Z = z; }

    public int attackPriority(Robot2 R) {
        if (R.unit == PREACHER) return 10;
        if (R.unit == PROPHET) return 7;
        if (R.unit == CRUSADER) return 6;
        if (R.unit == PILGRIM) return 5;
        return 4;
    }
    public boolean inAttackRange(Robot2 R, int x, int y) { // can R attack (x,y)? assuming there is a robot at (x,y)
        int d = Z.euclidDist(R,x,y);
        int mn = MIN_ATTACK_R[R.unit]; if (R.unit == PREACHER) mn = 3;
        return d >= mn && d <= MAX_ATTACK_R[R.unit];
    }

    public int preacherVal(Robot2 P, int x, int y) {
        if (!Z.valid(x,y)) return 0;
        int t = 0;
        for (int i = x-1; i <= x+1; ++i) for (int j = y-1; j <= y+1; ++j)
            if (Z.valid(i,j) && Z.robotMapID[j][i] > 0) {
                Robot2 R = Z.robotMap[j][i];
                int val = attackPriority(R);
                val *= (R.team == P.team) ? -4 : 1;
                if (R.isStructure()) val *= 2;
                if (R.unit == CASTLE && R.team != Z.CUR.team && R.id == MOD) continue;
                t += val;
            }
        return t;
    }
    public boolean containsConfident(int x, int y) {
        for (int i = x-1; i <= x+1; ++i) for (int j = y-1; j <= y+1; ++j)
            if (Z.enemyRobot(i,j) && Z.lastTurn[j][i] >= Z.CUR.turn-1) return true;
        return false;
    }

    // PREACHER EVASION
    public int maxPreacherVal(Robot2 P, int x, int y) { // P is the preacher
        int tot = 0;
        for (int X = x-1; X <= x+1; ++X) for (int Y = y-1; Y <= y+1; ++Y)
            if (inAttackRange(P,X,Y)) tot = Math.max(tot,preacherVal(P,X,Y));
        return tot;
    }
    public int totPreacherDamage(int x, int y) {
        int res = 0;
        for (int i = x-5; i <= x+5; ++i) for (int j = y-5; j <= y+5; ++j)
            if (Z.enemyRobot(i,j,PREACHER))
                res += maxPreacherVal(Z.robotMap[j][i],x,y);
        return 2*res;
    }
    public int totDamage(int x, int y) { // sum of damage of all enemy units that can attack this position
        int res = totPreacherDamage(x,y);
        for (int i = -8; i <= 8; ++i) for (int j = -8; j <= 8; ++j)
            if (Z.enemyAttacker(x+i,y+j)) {
                Robot2 R = Z.robotMap[y+j][x+i];
                if (R.unit == 5) continue;
                if (inAttackRange(R,x,y)) res += DAMAGE[R.unit];
            }
        return res;
    }
    public int totPreacherDamageAfter(int x, int y) {
        Z.robotMapID[Z.CUR.y][Z.CUR.x] = 0; Z.robotMap[Z.CUR.y][Z.CUR.x] = null;
        Z.CUR.x = x; Z.CUR.y = y;
        Z.robotMapID[Z.CUR.y][Z.CUR.x] = Z.CUR.id; Z.robotMap[Z.CUR.y][Z.CUR.x] = Z.CUR;

        int t = totPreacherDamage(x,y);

        Z.robotMapID[Z.CUR.y][Z.CUR.x] = 0; Z.robotMap[Z.CUR.y][Z.CUR.x] = null;
        Z.CUR.x = Z.ORI.x; Z.CUR.y = Z.ORI.y;
        Z.robotMapID[Z.CUR.y][Z.CUR.x] = Z.CUR.id; Z.robotMap[Z.CUR.y][Z.CUR.x] = Z.CUR;

        return t;
    }
    public int totDamageAfter(int x, int y) {
        Z.robotMapID[Z.CUR.y][Z.CUR.x] = 0; Z.robotMap[Z.CUR.y][Z.CUR.x] = null;
        Z.CUR.x = x; Z.CUR.y = y;
        Z.robotMapID[Z.CUR.y][Z.CUR.x] = Z.CUR.id; Z.robotMap[Z.CUR.y][Z.CUR.x] = Z.CUR;

        int t = totDamage(x,y);

        Z.robotMapID[Z.CUR.y][Z.CUR.x] = 0; Z.robotMap[Z.CUR.y][Z.CUR.x] = null;
        Z.CUR.x = Z.ORI.x; Z.CUR.y = Z.ORI.y;
        Z.robotMapID[Z.CUR.y][Z.CUR.x] = Z.CUR.id; Z.robotMap[Z.CUR.y][Z.CUR.x] = Z.CUR;

        return t;
    }

    Action2 tryGive() {
        for (Robot2 R: Z.robots)
            if (R.isStructure() && R.team == Z.CUR.team && Z.adjacent(Z.CUR,R) && enoughResources()) {
                Z.resource = -1;
                return Z.giveAction(R.x-Z.CUR.x,R.y-Z.CUR.y,Z.CUR.karbonite,Z.CUR.fuel);
            }
        return null;
    }
    boolean enoughResources() {
      if (Z.CUR.unit != PILGRIM || Z.danger[Z.CUR.y][Z.CUR.x] == 0)
        return Z.CUR.fuel > 25 || (Z.CUR.fuel > 0 && !Z.fuelMap[Z.CUR.y][Z.CUR.x])
        	|| Z.CUR.karbonite > 5 || (Z.CUR.karbonite > 0 && !Z.karboniteMap[Z.CUR.y][Z.CUR.x]);
      return Z.CUR.fuel > 25 || Z.CUR.karbonite > 5;
    }

    Action2 moveClose(int x, int y) {
        int bestDist = Z.euclidDist(Z.CUR,x,y); Action2 bestMove = null;
        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j)
            if (Z.canMove(Z.CUR,i,j)) {
                int dist = Z.sq(Z.CUR.x+i-x)+Z.sq(Z.CUR.y+j-y);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestMove = Z.moveAction(i,j);
                }
            }
        return bestMove;
    }
    Action2 moveAway(int x, int y) {
        int farthest = Z.euclidDist(Z.CUR,x,y); Action2 best = null;
        for (int i = -3; i <= 3; i++) for (int j = -3; j <= 3; j++)
            if (Z.canMove(Z.CUR,i,j)) {
                int dis = Z.sq(x - Z.CUR.x - i) + Z.sq(y - Z.CUR.y - j);
                if (dis > farthest) {
                  farthest = dis;
                  best = Z.moveAction(i, j);
                }
            }
        return best;
    }
    Action2 moveAway(Robot2 R) { return R == null ?  null : moveAway(R.x, R.y); }

    Action2 goHome() {
        Action2 A = tryGive(); if (A != null) return A;
        if (Z.CUR.unit == PILGRIM) return Z.safe.moveYourStruct();
        return Z.bfs.moveYourStruct();
    }
}
