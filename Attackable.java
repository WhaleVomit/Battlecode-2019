package bc19;

import static bc19.Consts.*;

public class Attackable extends Movable {
    public Attackable(MyRobot z) { super(z); }

    public int attackPriority(Robot2 R) {
        if (R.unit == PREACHER) return 10;
        if (R.unit == PROPHET) return 7;
        if (R.unit == CRUSADER) return 6;
        if (R.unit == PILGRIM) return 5;
        return 4;
    }
    public int getValPreacher(int x, int y) {
        int t = 0;
        for (int i = x - 1; i <= x + 1; ++i) 
            for (int j = y - 1; j <= y + 1; ++j) 
                if (Z.valid(i, j) && Z.robotMapID[j][i] > 0) {
                    Robot2 R = Z.robotMap[j][i];
                    int val = attackPriority(R);
                    val *= (R.team == Z.ME.team) ? -2 : 1;
                    if (R.isStructure()) val *= 2;
                    t += val;
                }
        return t;
    }
    public int canAttack(int dx, int dy) {
        if (ATTACK_F_COST[Z.ME.unit] > Z.fuel) return -MOD;
        int dist = dx * dx + dy * dy;
        if (Z.ME.unit == CRUSADER) {
            if (dist < 1 || dist > 16) return -MOD;
        } else if (Z.ME.unit == PROPHET) {
            if (dist < 16 || dist > 64) return -MOD;
        } else if (Z.ME.unit == PREACHER) {
            if (dist < 3 || dist > 16) return -MOD;
        } else return -MOD;

        int x = Z.ME.x + dx, y = Z.ME.y + dy;
        if (!Z.containsRobot(x, y)) return -MOD;
        if (Z.ME.unit == CRUSADER || Z.ME.unit == PROPHET) {
            if (Z.robotMap[y][x].team == Z.ME.team) return -MOD;
            return attackPriority(Z.robotMap[y][x]);
        } else { 
            if (dist < 3 || dist > 16) return -MOD;
            return getValPreacher(x,y);
        }
    }

    public boolean attackedByPreacher(int x, int y) {
        for (int I = -1; I <= 1; ++I) for (int J = -1; J <= 1; ++J) {
            boolean ok = false;
            if (I == 0 && J == 0) ok = true;
            else if (x+I == Z.ME.x && y+J == Z.ME.y) ok = false;
            else if (Z.containsRobot(x+I,y+J)) ok = true;
            if (!ok) continue;
            for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) 
                if (i*i+j*j <= 16 && Z.enemyRobot(x+i,y+j,PREACHER)) 
                    return true;
        }
        return false;
    }
    public boolean attacked(int x, int y) {
        if (attackedByPreacher(x,y)) return true;
        for (int i = -8; i <= 8; ++i) for (int j = -8; j <= 8; ++j) 
            if (Z.containsRobot(x+i,y+j)) {
                int d = i*i+j*j;
                Robot2 R = Z.robotMap[y+j][x+i];
                if (R.isAttacker(1-Z.ME.team) && d <= MAX_ATTACK_R[R.unit]) return true;
            }
        return false;
    }

    public Action avoidPreacher() {
        if (Z.ME.unit != PROPHET || !attackedByPreacher(Z.ME.x,Z.ME.y)) return null;
        Z.log("ATTACKED BY PREACHER "+Z.ME.turn+" "+Z.ME.x+" "+Z.ME.y);
        for (int i = -2; i <= 2; ++i) for (int j = -Math.abs(2-Math.abs(i)); j <= Math.abs(2-Math.abs(i)); ++j)
            if (Z.passable(Z.ME.x+i,Z.ME.y+j) && !attacked(Z.ME.x+i,Z.ME.y+j)) {
                Z.log("BACK UP");
                return Z.move(i,j);
            }
        Z.log("OOPS");
        return null;
    }
    public Action react() {
        Action A = avoidPreacher(); if (A != null) return A;
        A = tryAttack(); if (A != null) return A;
        Robot2 R = Z.closestEnemy(); 
        if (R == null) return null;
        if (Z.ME.unit == PROPHET && Z.euclidDist(R) < 16) return moveAway(R);
        return moveToward(R);
    }
    public Action tryAttack() {
        int besPri = 0; Action bes = null;
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

    public int patrolVal(int X, int Y, int x, int y) {
        if (((X == Z.ME.x && Y == Z.ME.y) || Z.passable(X,Y)) && (X+Y-x-y) % 2 == 0) {
            if (Z.sq(X-x)+Z.sq(Y-y) <= 2 && Z.numOpen(64*x+y) <= 2 && !(X == Z.ME.x && Y == Z.ME.y)) return MOD;
            int val = Math.abs(X-x)+Math.abs(Y-y)+2*Math.abs(Z.enemyDist[y][x][0]-Z.enemyDist[Y][X][0]);
            if (Z.karboniteMap[Y][X] || Z.fuelMap[Y][X]) val += 4;
            if (X == Z.ME.x && Y == Z.ME.y) val -= 2;
            return val;
        } 
        return MOD;
    }

    public Action patrol() {
        int t = Z.closestStruct(true);
        if (Z.bfsDist(t) > 4) return moveHome();
        int y = t % 64; int x = Z.fdiv(t,64); 

        int bestVal = MOD, bestDist = MOD, pos = MOD;
        for (int X = x-5; X <= x+5; ++X) 
            for (int Y = y-5; Y <= y+5; ++Y) {
                int val = patrolVal(X,Y,x,y);
                if (val < bestVal || (val == bestVal && Z.bfsDist[Y][X] < bestDist)) {
                    bestVal = val; bestDist = Z.bfsDist[Y][X]; pos = 64*X+Y;
                }
            }

        return nextMove(pos);
    }

    public Action aggressive() {
        Action A = moveEnemy(); if (A != null) return A;
        return nextMove(Z.closestUnseen());
    }
}