package bc19;

import java.util.*;
import static bc19.Consts.*;

public class Attackable extends Movable {
    public Attackable(MyRobot z) { super(z); }

    public double canAttack(int dx, int dy) {
        if (ATTACK_F_COST[Z.CUR.unit] > Z.fuel) return -MOD;
        int x = Z.CUR.x + dx, y = Z.CUR.y + dy;
        if (!inAttackRange(Z.CUR,x,y)) return -MOD;
        if (Z.CUR.unit == CRUSADER || Z.CUR.unit == PROPHET || Z.CUR.unit == CASTLE) {
            if (!Z.enemyRobot(x,y)) return -MOD;
            return attackPriority(Z.robotMap[y][x]);
        } else {
            if (!containsConfident(x,y)) return -MOD;
            return preacherVal(Z.CUR,x,y);
        }
    }

    public Action2 dealWithPreacher() {
        if (totPreacherDamage(Z.CUR.x,Z.CUR.y) == 0) return null;
        int C = Z.bfs.closestStruct(true); int cx = Z.fdiv(C,64), cy = C%64;
        if (Z.CUR.unit == PROPHET) { // move away
            for (int i = -2; i <= 2; ++i) for (int j = -2; j <= 2; ++j)
                if (Z.canMove(Z.CUR,i,j) && totDamageAfter(Z.CUR.x+i,Z.CUR.y+j) == 0)
                    return Z.moveAction(i,j);
        } else if (Z.CUR.unit == CRUSADER) {
            int bestDist = MOD; Action2 bestMove = null;
            for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) {
                int x = Z.CUR.x+i, y = Z.CUR.y+j;
                if (Z.canMove(Z.CUR,i,j) && Z.adjEnemyPreacher(x,y) && totDamageAfter(x,y) == 0) {
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

    public Action2 minimizeDamage(Robot2 R) {
        int oriDist = Z.sq(Z.CUR.x-R.x)+Z.sq(Z.CUR.y-R.y), oriDam = totDamage(Z.CUR.x,Z.CUR.y);
        int bestDam = MOD, bestDist = MOD; Action2 A = null;

        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j)
            if (Z.canMove(Z.CUR,i,j)) {
                int dam = totDamageAfter(Z.CUR.x+i,Z.CUR.y+j), dist = Z.sq(Z.CUR.x+i-R.x)+Z.sq(Z.CUR.y+j-R.y);
                if (Math.sqrt(dist)+0.5 <= Math.sqrt(oriDist) && (dam < bestDam || (dam == bestDam && dist < bestDist))) {
                    bestDam = dam; bestDist = dist; A = Z.moveAction(i,j);
                }
            }
        if (bestDam <= 1.5*oriDam+10) return A;
        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j)
            if (Z.canMove(Z.CUR,i,j)) {
                int dam = totDamageAfter(Z.CUR.x+i,Z.CUR.y+j), dist = Z.sq(Z.CUR.x+i-R.x)+Z.sq(Z.CUR.y+j-R.y);
                if (dam < bestDam || (dam == bestDam && dist < bestDist)) {
                    bestDam = dam; bestDist = dist; A = Z.moveAction(i,j);
                }
            }
        return A;
    }

    public boolean attacking(Robot2 R) { // PROPHET
        for (int i = -8; i <= 8; ++i) for (int j = -8; j <= 8; ++j) {
            int x = R.x+i, y = R.y+j;
            if (Z.yourRobot(x,y) && inAttackRange(R,x,y)) return true;
        }
        return false;
    }

    public Action2 waitOutOfRange(Robot2 R) {
        int bestDist = Z.euclidDist(R);
        Action2 bestMove = null;
        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j) {
            if (Z.canMove(Z.CUR,i,j) && totDamageAfter(Z.CUR.x+i,Z.CUR.y+j) == 0) {
                int dist = Z.sq(Z.CUR.x+i-R.x)+Z.sq(Z.CUR.y+j-R.y);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestMove = Z.moveAction(i,j);
                }
            }
        }
        return bestMove;
    }

    public Action2 moveTowardEnemy(Robot2 R) {
        if (Z.euclidDist(R) > 100) return Z.bfs.move(R.x,R.y);
        return moveClose(R.x,R.y);
    }

    public Action2 preacherPosition(Robot2 R) {
        if (Z.euclidDist(R) > 49 || R.unit == PROPHET || Z.lastTurn[R.y][R.x] <= Z.CUR.turn-2)
          return moveTowardEnemy(R);
        return minimizeDamage(R);
    }

    public Action2 crusaderPosition(Robot2 R) {
        if (Z.euclidDist(R) > 100) return moveTowardEnemy(R);
        Action2 A = waitOutOfRange(R);
        if (A != null) {
            double d0 = Math.sqrt(Z.euclidDist(R));
            double d1 = Math.sqrt(Z.euclidDist(R,Z.CUR.x+A.dx,Z.CUR.y+A.dy));
            if (d1+1 <= d0) return A;
        }
        if (Z.euclidDist(R) > 49 || R.unit == PROPHET) return moveTowardEnemy(R);
        return minimizeDamage(R);
    }

    public Action2 prophetPosition(Robot2 R) {
        if (Z.euclidDist(R) > 100) return moveTowardEnemy(R);
        if (R.unit == PROPHET && !attacking(R)) {
            Z.log("WAIT FOR IT");
            return waitOutOfRange(R);
        }
        return minimizeDamage(R);
    }

    public Action2 position() {
		  Robot2 R = Z.closestAttacker(Z.CUR,1-Z.CUR.team);
		  if (Z.euclidDist(R) > 196 || (Z.euclidDist(R) > 100 && Z.bfs.distHome() > 9)) return null;
      if (Z.U.closeUnits[PROPHET] > 15 && !Z.attackMode) return null;
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
      if (Z.CUR.unit == PREACHER) return preacherPosition(R);
      if (Z.CUR.unit == CRUSADER) return crusaderPosition(R);
      if (Z.CUR.unit == PROPHET) return prophetPosition(R);
      return null;
    }

    public Action2 tryAttack() {
        double besPri = 0;
        int DX = MOD, DY = MOD;
        for (int dx = -8; dx <= 8; ++dx)
            for (int dy = -8; dy <= 8; ++dy) {
                double t = canAttack(dx, dy);
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
        if(Z.attackMode) return position();
    }
    public int patrolVal(int X, int Y, int x, int y) {
  		int big = 123456, val = 0;
  		if (Z.euclidDist(X,Y,x,y) < 4) return val += big; // avoid congestion
  		if (Z.enemyDist[Y][X][0] <= Math.min(14,Z.enemyDist[y][x][0])) return val += 2*big;
      if (((X == Z.CUR.x && Y == Z.CUR.y) || Z.robotMapID[Y][X] <= 0) && (X+Y) % 2 == 0) {
          val += Math.abs(X-x)+Math.abs(Y-y)+2*Math.abs(Z.enemyDist[y][x][0]-Z.enemyDist[Y][X][0]);
          if (Z.karboniteMap[Y][X] || Z.fuelMap[Y][X]) val += big;
          return val;
      }
      return MOD;
    }

    public Action2 patrol() {
  	  int t = Z.bfs.closestStruct(true); if (t == MOD) return null;
  	  int x = Z.fdiv(t,64), y = t%64;

      // Z.log("TRY PATROL");
      if (Z.lastPatrol != Z.CUR.turn-1) Z.endPos = MOD;
      Z.lastPatrol = Z.CUR.turn;
      if (Z.bfs.dist(Z.endPos) == MOD) Z.endPos = MOD;
      if (Z.endPos != MOD) {
    		int ex = Z.fdiv(Z.endPos,64); int ey = Z.endPos%64;
        // Z.log("PREV POS "+ex+" "+ey+" "+Z.w+" "+Z.h);
    		if (Z.enemyDist[ey][ex][0] <= 14) Z.endPos = MOD;
    		//if(patrolVal(ex,ey,x,y) == MOD && !(Z.CUR.x == ex && Z.CUR.y == ey)) Z.endPos = MOD;
      }

      // Z.log("DETERMINE POS");
      if (Z.endPos == MOD) {
        int bestVal = MOD;
        for (int X = 0; X < Z.w; ++X) for (int Y = 0; Y < Z.h; ++Y) {
            int val = patrolVal(X,Y,x,y);
            if (Z.bfs.dist(X,Y) != MOD) bestVal = Math.min(bestVal,val);
        }
        int bestDist = MOD;
        for (int X = 0; X < Z.w; ++X) for (int Y = 0; Y < Z.h; ++Y) {
            int val = patrolVal(X,Y,x,y);
            if (val <= bestVal+5) {
              int dist = Z.bfs.dist(X,Y);
              if (dist < bestDist) {
                bestDist = dist;
                Z.endPos = 64*X+Y;
              }
            }
        }
        if (Z.CUR.unit == PREACHER) Z.log("PATROL "+Z.CUR.x+" "+Z.CUR.y+" "+Z.coordinates(Z.endPos)+" "+bestDist+" "+bestVal);
      }
      // Z.log("FINISH");

      return Z.bfs.move(Z.endPos);
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

    boolean shouldWait() {
      if (Z.CUR.health != Z.lastHealth) return false;

      ArrayList<Integer> dists = new ArrayList<>();
      for (int i = -6; i <= 6; ++i) for (int j = -6; j <= 6; ++j) if (i*i+j*j <= Math.min(VISION_R[Z.CUR.unit],36)) {
          int x = Z.CUR.x+i, y = Z.CUR.y+j;
          if (Z.yourAttacker(x,y)) dists.add(Z.enemyDist[y][x][0]);
      }

      Collections.sort(dists); Collections.reverse(dists);
      if (dists.size() > 1 && Z.enemyDist[Z.CUR.y][Z.CUR.x][0]+3 < dists.get(1) && !Z.waited) {
        Z.waited = true;
        Z.log("WAITED");
        return true;
      }
      Z.waited = false;
      return false;
    }

    Action2 aggressive() {
        if (shouldWait()) return null;
        Action2 A = Z.bfs.moveEnemyStruct(); if (A != null) return A;
        return Z.bfs.moveUnseen();
    }

    Action2 runDefault() {
        Action2 A = react(); if (A != null) return A;
        if (!Z.attackMode) {
            if (enoughResources()) return goHome();
            return patrol();
        }
        return aggressive();
    }
}
