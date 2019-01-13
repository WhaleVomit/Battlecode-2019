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
                    val *= (R.team == Z.CUR.team) ? -2 : 1;
                    if (R.isStructure()) val *= 2;
                    t += val;
                }
        return t;
    }

    public int canAttack(int dx, int dy) {
        if (ATTACK_F_COST[Z.CUR.unit] > Z.fuel) return -MOD;
        int dist = dx * dx + dy * dy;
        if (Z.CUR.unit == CRUSADER) {
            if (dist < 1 || dist > 16) return -MOD;
        } else if (Z.CUR.unit == PROPHET) {
            if (dist < 16 || dist > 64) return -MOD;
        } else if (Z.CUR.unit == PREACHER) {
            if (dist < 1 || dist > 16) return -MOD;
        } else return -MOD;

        int x = Z.CUR.x + dx, y = Z.CUR.y + dy;
        if (!Z.containsRobot(x, y)) return -MOD;
        if (Z.CUR.unit == CRUSADER || Z.CUR.unit == PROPHET) {
            if (Z.robotMap[y][x].team == Z.CUR.team) return -MOD;
            return attackPriority(Z.robotMap[y][x]);
        } else {
            if (dist < 3 || dist > 16) return -MOD;
            return getValPreacher(x,y);
        }
    }

    public boolean adjacentEnemyPreacher(int x, int y) {
        for (int I = -1; I <= 1; ++I) for (int J = -1; J <= 1; ++J)
            if (Z.enemyRobot(x+I,y+J,PREACHER)) return true;
        return false;
    }
    public boolean attackedByPreacher(int x, int y) {
        // if (adjacentEnemyPreacher(x,y)) return false;
        for (int I = -1; I <= 1; ++I) for (int J = -1; J <= 1; ++J) {
            boolean ok = false;
            if (I == 0 && J == 0) ok = true;
            else if (x+I == Z.CUR.x && y+J == Z.CUR.y) ok = false;
            else if (Z.containsRobot(x+I,y+J)) ok = true;
            if (!ok || adjacentEnemyPreacher(x+I,y+J)) continue;
            for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j)
                if (i*i+j*j <= 16 && Z.enemyRobot(x+i,y+j,PREACHER))
                    return true;
        }
        return false;
    }

    public boolean notDumb(int x, int y) {
        for (int I = -1; I <= 1; ++I) for (int J = -1; J <= 1; ++J) {
            boolean ok = false;
            if (I == 0 && J == 0) ok = true;
            else if (x+I == Z.CUR.x && y+J == Z.CUR.y) ok = false;
            else if (Z.containsRobot(x+I,y+J)) ok = true;
            if (!ok || adjacentEnemyPreacher(x+I,y+J)) continue;
            ok = false;
            for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j)
                if (i*i+j*j <= 16 && Z.enemyRobot(x+i,y+j,PREACHER)) ok = true;
            if (!ok) continue;
            int caught = 0;
            for (int I2 = -1; I2 <= 1; ++I2) for (int J2 = -1; J2 <= 1; ++J2) {
                boolean yours = false;
                if (x+I+I2 == x && y+J+J2 == y) yours = true;
                else if (x+I+I2 == Z.CUR.x && y+J+J2 == Z.CUR.y) yours = false;
                else if (Z.yourAttacker(x+I+I2,y+J+J2)) yours = true;
                if (yours) caught ++;
            }
            if (caught > 1) return false;
        }
        return true;

    }
    public boolean attacked(int x, int y) {
        if (attackedByPreacher(x,y)) return true;
        for (int i = -8; i <= 8; ++i) for (int j = -8; j <= 8; ++j)
            if (Z.containsRobot(x+i,y+j)) {
                int d = i*i+j*j;
                Robot2 R = Z.robotMap[y+j][x+i];
                if (R.isAttacker(1-Z.CUR.team) && R.unit != PREACHER && d <= MAX_ATTACK_R[R.unit]) return true;
            }
        return false;
    }
    public Action2 dealWithPreacher() {
        if (!attackedByPreacher(Z.CUR.x,Z.CUR.y)) return null;
        Z.log("ATTACKED BY PREACHER "+Z.CUR.turn+" "+Z.CUR.x+" "+Z.CUR.y);

        int C = Z.closestStruct(true);
        int cx = Z.fdiv(C,64), cy = C%64;
        if (Z.CUR.unit == PROPHET) {
            for (int i = -2; i <= 2; ++i) for (int j = -2; j <= 2; ++j)
                if (canMove(Z.CUR,i,j) && !attacked(Z.CUR.x+i,Z.CUR.y+j)) return Z.moveAction(i,j);
        } else if (Z.CUR.unit == CRUSADER) {
            int bestDist = MOD;
            Action2 bestMove = null;
            for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) {
                int x = Z.CUR.x+i, y = Z.CUR.y+j;
                if (canMove(Z.CUR,i,j) && !attacked(x,y) && adjacentEnemyPreacher(x,y)) {
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
    boolean enemyCanAttack(Robot2 R, int x, int y) { // can R attack (x,y)? assuming there is a robot at (x,y)
		if(R.unit == PREACHER) {
			for(int dx = -1; dx <= 1; dx++) for(int dy = -1; dy <= 1; dy++) {
				if(dx == 0 && dy == 0 && Z.euclidDist(R,x,y) >= 1 && Z.euclidDist(R,x,y) <= 16) return true;
				else if(Z.containsRobot(x+dx,y+dy)) {
					Robot2 R1 = Z.robotMap[y+dy][x+dx];
					if(R1.team == Z.me.team && Z.euclidDist(R,R1) >= 1 && Z.euclidDist(R,R1) <= 16) return true;
				}
			}
			return false;
		} else {
			return Z.euclidDist(R,x,y) >= MIN_ATTACK_R[R.unit] && Z.euclidDist(R,x,y) <= MAX_ATTACK_R[R.unit];
		}
	}
    int getDamage(int x, int y) { // sum of damage of all enemy units that can attack this position
		int res = 0;
		for (int i = -8; i <= 8; ++i) for (int j = -8; j <= 8; ++j) {
            if (Z.containsRobot(x+i,y+j)) {
                int d = i*i+j*j;
                Robot2 R = Z.robotMap[y+j][x+i];
                if(R.team == Z.me.team || R.unit <= 1) continue;
                if(enemyCanAttack(R,x,y)) res += DAMAGE[R.unit];
            }
		}
		return res;
	}
    public Action2 position() {
		Robot2 R = Z.closestEnemy(Z.CUR); if (Z.euclidDist(R) > 196) R = null;
        if (R == null) return null;
        if (Z.euclidDist(R) < MIN_ATTACK_R[Z.CUR.unit]) return moveAway(R);
        if (Z.euclidDist(R) <= MAX_ATTACK_R[Z.CUR.unit]) return null;
        if (R.unit != PREACHER || Z.CUR.unit == PREACHER || Z.euclidDist(R) > 49) return moveToward(R.x,R.y);
        // CRUSADER vs PREACHER, both currently outside of attack ranges
        // return moveTowardEnemy(R);

        int oriDist = Z.sq(Z.CUR.x-R.x)+Z.sq(Z.CUR.y-R.y);
        int bestDam = MOD, bestDist = MOD; Action2 A = null;
        for (int i = -3; i <= 3; ++i) {
            for (int j = -3; j <= 3; ++j) {
				int dam = getDamage(Z.CUR.x+i,Z.CUR.y+j);
                if (canMove(Z.CUR,i,j) && dam < bestDam) {
					bestDist = Math.min(bestDist, i*i+j*j);
					bestDam = dam;
					A = Z.moveAction(i,j);
                }
			}
		}

        if (Math.sqrt(bestDist)+0.5 < Math.sqrt(oriDist)) {
            // Z.log ("AVOID "+Z.CUR.x+" "+Z.CUR.y+" "+R.x+" "+R.y+" "+bestDist);
            return A;
        }
        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) 
            if (canMove(Z.CUR,i,j) && notDumb(Z.CUR.x+i,Z.CUR.y+j)) {
                int dist = Z.sq(Z.CUR.x+i-R.x)+Z.sq(Z.CUR.y+j-R.y);
                if (dist < bestDist && dist < oriDist) {
                    bestDist = dist;
                    A = Z.moveAction(i,j);
                }
            }
        return A;
    }
    public Action2 react() {
        Action2 A = dealWithPreacher(); if (A != null) return A;
        A = tryAttack(); if (A != null) return A;
        return position();
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

    public int patrolVal(int X, int Y, int x, int y) {
        if (((X == Z.CUR.x && Y == Z.CUR.y) || Z.robotMapID[Y][X] <= 0) && (X+Y-x-y) % 2 == 0) {
            if (Z.sq(X-x)+Z.sq(Y-y) <= 2 && Z.numOpen(64*x+y) <= 2 && !(X == Z.CUR.x && Y == Z.CUR.y)) return MOD;
            int val = Math.abs(X-x)+Math.abs(Y-y)+2*Math.abs(Z.enemyDist[y][x][0]-Z.enemyDist[Y][X][0]);
            if (Z.karboniteMap[Y][X] || Z.fuelMap[Y][X]) val += 4;
            if (X == Z.CUR.x && Y == Z.CUR.y) val -= 2;
            return val;
        }
        return MOD;
    }
    public Action2 patrol() {
        int t = Z.closestStruct(true);
        if (Z.bfsDist(t) > 9) return moveHome();
        int x = Z.fdiv(t,64), y = t % 64;

        int bestVal = MOD, bestDist = MOD, pos = MOD;
        for (int X = x-10; X <= x+10; ++X) for (int Y = y-10; Y <= y+10; ++Y) if (Z.valid(X,Y)) {
            int val = patrolVal(X,Y,x,y);
            if (val < bestVal || (val == bestVal && Z.bfsDist[Y][X] < bestDist)) {
                bestVal = val; bestDist = Z.bfsDist[Y][X]; pos = 64*X+Y;
            }
        }
        // Z.log(Z.coordinates(pos)+" "+Z.coordinates(t)+" "+bestVal);

        return nextMove(pos);
    }

    public Action2 aggressive() {
        Action2 A = tryAttack(); if (A != null) return A;
        A = moveEnemy(); if (A != null) return A;
        // Z.log("NO MOVE?");
        return nextMove(Z.closestUnseen());
    }
}
