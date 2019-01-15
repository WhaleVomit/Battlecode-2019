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
    public int canAttack(int dx, int dy) {
        if (ATTACK_F_COST[Z.CUR.unit] > Z.fuel) return -MOD;
        int x = Z.CUR.x + dx, y = Z.CUR.y + dy;
        if (!inAttackRange(Z.CUR,x,y)) return -MOD;
        if (Z.CUR.unit == CRUSADER || Z.CUR.unit == PROPHET) {
			if (!Z.enemyRobot(x,y)) return -MOD;
            return attackPriority(Z.robotMap[y][x]);
        } else {
            if (!containsConfident(x,y)) return -MOD;
            return preacherVal(Z.CUR,x,y);
        }
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
    public Action2 dealWithPreacher() {
        if (totPreacherDamage(Z.CUR.x,Z.CUR.y) == 0) return null;
        int C = Z.closestStruct(true); int cx = Z.fdiv(C,64), cy = C%64;
        if (Z.CUR.unit == PROPHET) { // move away
            for (int i = -2; i <= 2; ++i) for (int j = -2; j <= 2; ++j)
                if (canMove(Z.CUR,i,j) && totDamageAfter(Z.CUR.x+i,Z.CUR.y+j) == 0) 
                    return Z.moveAction(i,j);
        } else if (Z.CUR.unit == CRUSADER) {
            int bestDist = MOD; Action2 bestMove = null;
            for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) {
                int x = Z.CUR.x+i, y = Z.CUR.y+j;
                if (canMove(Z.CUR,i,j) && Z.adjEnemyPreacher(x,y) && totDamageAfter(x,y) == 0) {
                    int dist = Z.sq(cx-x)+Z.sq(cy-y);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestMove = Z.moveAction(i,j);
                    }
                }
            }
            return bestMove;
        }
        return null;
    }

    public Action2 position() {
		Robot2 R = Z.closestEnemy(Z.CUR);
		if (Z.euclidDist(R) > 196 || (Z.euclidDist(R) > 100 && Z.bfsDistHome() > 9)) return null;   
        if (Z.euclidDist(R) < MIN_ATTACK_R[Z.CUR.unit]) {
            Action2 A = moveAway(R);
            // Z.log("OOPS "+Z.CUR.unit+" "+Z.CUR.x+" "+Z.CUR.y+A.dx+" "+A.dy);
            // Z.log("?? "+totPreacherDamageAfter(Z.CUR.x+A.dx,Z.CUR.y+A.dy)+" "+totPreacherDamageAfter(Z.CUR.x,Z.CUR.y));
            if (A != null && totPreacherDamageAfter(Z.CUR.x+A.dx,Z.CUR.y+A.dy) > 
                            totPreacherDamageAfter(Z.CUR.x,Z.CUR.y)) 
                A = new Action2(); 
            return A;
        }

        if (Z.euclidDist(R) <= MAX_ATTACK_R[Z.CUR.unit]) return null;
        if (R.unit != PREACHER || Z.euclidDist(R) > 49) return moveToward(R.x,R.y);
        if (Z.CUR.unit == PREACHER && Z.lastTurn[R.y][R.x] <= Z.CUR.turn-2) return moveToward(R.x,R.y);
        int oriDist = Z.sq(Z.CUR.x-R.x)+Z.sq(Z.CUR.y-R.y), oriDam = totDamage(Z.CUR.x,Z.CUR.y);
        int bestDam = MOD, bestDist = MOD; Action2 A = null;

        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) 
            if (canMove(Z.CUR,i,j)) {
                int dam = totDamageAfter(Z.CUR.x+i,Z.CUR.y+j), dist = Z.sq(Z.CUR.x+i-R.x)+Z.sq(Z.CUR.y+j-R.y);
                if (Math.sqrt(dist)+0.5 <= Math.sqrt(oriDist) && (dam < bestDam || (dam == bestDam && dist < bestDist))) {
                    bestDam = dam; bestDist = dist; A = Z.moveAction(i,j);
                }
			}

        // Z.log(Z.CUR.x+" "+Z.CUR.y+ " ORI DIST "+oriDist+" "+R.x+" "+R.y+" "+bestDam);
        if (bestDam <= 1.5*oriDam+10) return A;
        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) 
            if (canMove(Z.CUR,i,j)) {
                int dam = totDamageAfter(Z.CUR.x+i,Z.CUR.y+j), dist = Z.sq(Z.CUR.x+i-R.x)+Z.sq(Z.CUR.y+j-R.y);
                if (dam < bestDam || (dam == bestDam && dist < bestDist)) {
                    bestDam = dam; bestDist = dist; A = Z.moveAction(i,j);
                }
            }
        return A;
    }

    public Action2 tryAttack() {
        int besPri = 0, DX = MOD, DY = MOD;
        for (int dx = -8; dx <= 8; ++dx)
            for (int dy = -8; dy <= 8; ++dy) {
                int t = canAttack(dx, dy);
                if (t > besPri) {
                    besPri = t; DX = dx; DY = dy;
                }
            }
        if (besPri == 0) return null;
        return Z.attackAction(DX,DY);
    }
    public Action2 react() {
        Action2 A = dealWithPreacher();  if (A != null) return A;
        A = tryAttack(); if (A != null) return A;
        return position();
    }
    public int patrolVal(int X, int Y, int x, int y) {
        if (Z.numOpen(64*x+y) <= 2) Z.notClose = true;
        if (((X == Z.CUR.x && Y == Z.CUR.y) || Z.robotMapID[Y][X] <= 0) && (X+Y) % 2 == 0) {
            if (Z.sq(X-x)+Z.sq(Y-y) <= 2 && Z.notClose) return MOD;
            int val = Math.abs(X-x)+Math.abs(Y-y)+2*Math.abs(Z.enemyDist[y][x][0]-Z.enemyDist[Y][X][0]);
            if (Z.karboniteMap[Y][X] || Z.fuelMap[Y][X]) val += 10;
            if (X == Z.CUR.x && Y == Z.CUR.y) val -= 2;
            return val;
        }
        return MOD;
    }
    public Action2 patrol() {
        int t = Z.closestStruct(true);
        int x = Z.fdiv(t,64), y = t % 64;

        int bestVal = MOD, bestDist = MOD, pos = MOD;
        for (int X = x-10; X <= x+10; ++X) for (int Y = y-10; Y <= y+10; ++Y) if (Z.valid(X,Y) && Z.bfsDist(64*X + Y) <= 9) {
            int val = patrolVal(X,Y,x,y);
            if (val < bestVal || (val == bestVal && Z.bfsDist[Y][X] < bestDist)) {
                bestVal = val; bestDist = Z.bfsDist[Y][X]; pos = 64*X+Y;
            }
        }
        // Z.log(Z.coordinates(pos)+" "+Z.coordinates(t)+" "+bestVal);

        return nextMove(pos);
    }

    int notCrusaders() {
        int ret = 0;
        for (int i = -6; i <= 6; ++i) for (int j = -6; j <= 6; ++j) if (i*i+j*j <= 36) {
            int x = Z.CUR.x+i, y = Z.CUR.y+j;
            if (Z.yourAttacker(x,y) && Z.robotMap[y][x].unit != 3) ret ++;
        }
        return ret;
    }

    int shortestNotCrusaderDist(Robot2 R) {
        int ret = MOD;
        for (int i = -6; i <= 6; ++i) for (int j = -6; j <= 6; ++j) if (i*i+j*j <= 36) {
            int x = Z.CUR.x+i, y = Z.CUR.y+j;
            if (Z.yourAttacker(x,y) && Z.robotMap[y][x].unit != 3) {
                ret = Math.min(ret,Z.euclidDist(R,x,y));
            }
        }
        return ret;
    }

    public Action2 aggressive() {
        Robot2 R = Z.closestEnemy(Z.CUR);
        if (Z.CUR.unit == CRUSADER) {
            int a = notCrusaders(), b = shortestNotCrusaderDist(R);
            if (a >= 2 && Math.sqrt(Z.euclidDist(R))+2 <= Math.sqrt(b)) {
                Z.log("CRUSADER SHOULD WAIT FOR OTHERS");
                return null;
            }
        }
        Action2 A = moveEnemy(); if (A != null) return A;
        return nextMove(Z.closestUnseen());
    }

    public Action2 runDefault() {
        Z.sendToCastle(); 
        Action2 A = react(); if (A != null) return A;
        if (!Z.attackMode) {
            if (enoughResources()) return moveHome();
            return patrol();
        } 
        return aggressive();
    }
}
