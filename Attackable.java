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
    
    public int canAttack(int dx, int dy) {
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

    public int getEnemyLoc() {
        for (Robot2 R: Z.robots) if (R.isStructure() && R.team == Z.me.team && 1000 < R.signal && R.signal <= 1441) {
            int t = R.signal-1001;
            int y = (t%21)-10; y += R.y;
            int x = Z.fdiv(t,21)-10; x += R.x;
            Z.log("OH "+Z.me.x+" "+Z.me.y+" "+x+" "+y);
            return 64*x+y;
        }
        return MOD;
    }

    public Action react() {
        /*if (Z.unit == PROPHET && withinPreacherRange()) {
            int t = ;
            return nextMove(t);
        }*/
        Action A = tryAttack(); if (A != null) return A;
        Robot2 R = Z.closestEnemy(); 
        if (R != null) {
            if (Z.me.unit == PROPHET && Z.euclidDist(R) < 16) return moveAway(R);
            return moveToward(R);
        }
        return nextMove(getEnemyLoc());
    }

    int getValPreacher(int x, int y) {
        int t = 0;
        for (int i = x - 1; i <= x + 1; ++i) 
            for (int j = y - 1; j <= y + 1; ++j) 
                if (Z.valid(i, j) && Z.seenMap[j][i] > 0) {
                    Robot2 R = Z.seenRobot[j][i];
                    int val = attackPriority(R);
                    val *= (R.team == Z.me.team) ? -2 : 1;
                    if (R.isStructure()) val *= 2;
                    t += val;
                }
        return t;
    }
    
    public Action tryAttack() {
        if (Z.me.unit != PREACHER) {
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
        } else {
            int bes = 0, DX = MOD, DY = MOD;
            for (int dx = -4; dx <= 4; ++dx)
                for (int dy = -4; dy <= 4; ++dy)
                    if (canAttack(dx, dy) != -MOD) {
                        int t = getValPreacher(Z.me.x + dx, Z.me.y + dy);
                        if (t > bes) {
                            bes = t;
                            DX = dx;
                            DY = dy;
                        }
                    }
            if (bes == 0) return null;
            return Z.attack(DX, DY);
        }
    }

    public int getVal(int X, int Y, int x, int y) {
        if (((X == Z.me.x && Y == Z.me.y) || Z.passable(X,Y)) && (X+Y-x-y) % 2 == 0) {
            if (Z.sq(X-x)+Z.sq(Y-y) <= 2 && Z.numOpen(64*x+y) <= 2) return MOD;
            int val = Math.abs(X-x)+Math.abs(Y-y)+2*Math.abs(Z.enemyDist[y][x][0]-Z.enemyDist[Y][X][0]);
            if (Z.karboniteMap[Y][X] || Z.fuelMap[Y][X]) val += 4;
            return val;
        } else return MOD;
    }

    public Action patrol() {
        int t = Z.getClosestStruct(true);
        if (Z.bfsDist(t) > 4) return moveHome();
        int y = t % 64; int x = Z.fdiv(t,64); 

        int bestVal = MOD, bestDist = MOD, pos = MOD;
        for (int X = x-5; X <= x+5; ++X) 
            for (int Y = y-5; Y <= y+5; ++Y) {
                int val = getVal(X,Y,x,y);
                if (val < bestVal || (val == bestVal && Z.dist[Y][X] < bestDist)) {
                    bestVal = val; 
                    bestDist = Z.dist[Y][X];
                    pos = 64*X+Y;
                }
            }

        return nextMove(pos);
    }
}