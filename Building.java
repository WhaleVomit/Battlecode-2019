package bc19;

import static bc19.Consts.*;
import java.util.*;

public class Building extends Attackable {
  boolean shouldBuild = true;
  boolean isOccupiedPatrol[];

  public Building(MyRobot z) { super(z); }

  int closeResources() {
    int ret = 0;
    for (int i = -5; i <= 5; ++i) for (int j = -5; j <= 5; ++j)
      if (Z.containsResource(Z.CUR.x+i,Z.CUR.y+j)) ret ++;
    return ret;
  }
  int seenResources() {
    int ret = 0;
    for (int i = -10; i <= 10; ++i) for (int j = -10; j <= 10; ++j) if (i*i+j*j <= 100)
      if (Z.containsResource(Z.CUR.x+i,Z.CUR.y+j)) ret ++;
    return ret;
  }
  int seenPilgrim() {
    int ret = 0;
    for (int i = -10; i <= 10; ++i) for (int j = -10; j <= 10; ++j) if (i*i+j*j <= 100)
      if (Z.teamRobot(Z.CUR.x+i,Z.CUR.y+j,Z.CUR.team) &&
        Z.robotMap[Z.CUR.y+j][Z.CUR.x+i].unit == PILGRIM) ret ++;
    return ret;
  }
  int closeKarb() {
    int ret = 0;
    for (int i = -5; i <= 5; ++i) for (int j = -5; j <= 5; ++j) {
      int x = Z.CUR.x+i, y = Z.CUR.y+j;
      if (Z.valid(x,y) && Z.karboniteMap[y][x]) ret ++;
    }
    return ret;
  }
  int closePilgrim() {
    int ret = 0;
    for (int i = -5; i <= 5; ++i) for (int j = -5; j <= 5; ++j)
      if (Z.teamRobot(Z.CUR.x+i,Z.CUR.y+j,Z.CUR.team) &&
        Z.robotMap[Z.CUR.y+j][Z.CUR.x+i].unit == PILGRIM) ret ++;
    return ret;
  }
  int needAttackFuel() {
    return 10*Z.U.closeUnits[CRUSADER]+25*Z.U.closeUnits[PROPHET]+15*Z.U.closeUnits[PREACHER];
  }
  boolean enoughFuel() {
    if (Z.fuel < 2*needAttackFuel()+75) return false;
    if (Z.fuel < 200 && Z.CUR.unit == CASTLE) return false;
    return true;
  }
  int reallyCloseAttackers() {
    int ret = 0;
    for (int i = -5; i <= 5; ++i) for (int j = -5; j <= 5; ++j)
      if (i*i+j*j <= 25 && Z.teamAttacker(Z.CUR.x+i,Z.CUR.y+j,Z.CUR.team))
        ret ++;
    return ret;
  }
  boolean reallyClose() {
    for (int i = -7; i <= 7; ++i) for (int j = -7; j <= 7; ++j) if (i*i+j*j <= 49) {
      int x = Z.CUR.x+i, y = Z.CUR.y+j;
      if (Z.enemyRobot(x,y)) {
        if (Z.robotMap[y][x].unit == PREACHER) return true;
        if (Z.robotMap[y][x].isStructure()) return true;
      }
    }
    return false;
  }

  int decideUnit() {
    if (Z.U.closeEnemyUnits() > 0) Z.seenEnemy = true;
  	if (!Z.seenEnemy && (Z.fuel < 250 || Z.karbonite < 80)) return MOD;

  	int res = MOD;
  	if (Z.U.closeEnemy[CRUSADER]+Z.U.closeEnemy[PREACHER]-Z.U.closeUnits[PREACHER] > 0) res = PREACHER;
    else if (Z.U.closeEnemyAttackers() == 0) {
      if (Z.U.closeUnits[PROPHET] < 2) res = PROPHET;
      else if (Z.U.closeUnits[PREACHER] == 0) res = PREACHER;
    } else {
	    if ((Z.karbonite < 25 || Z.fuel < 100) && Z.U.closeEnemy[PREACHER]+Z.U.closeEnemy[CASTLE]+Z.U.closeEnemy[CHURCH] == 0) res = CRUSADER;
      else if (Z.U.closeEnemy[CHURCH] > 0 && Z.U.closeEnemy[PROPHET] == 0) res = PREACHER;
      else res = PROPHET;
  	}
    if (reallyClose()) res = PREACHER;
    return res;

  	/*int mx = -1;
  	if (Z.canBuild(CRUSADER)) mx = CRUSADER;
  	if (Z.canBuild(PROPHET)) mx = PROPHET;
  	if (Z.canBuild(PREACHER)) mx = PREACHER;
  	if (mx == -1) return MOD;*/

    /*int crus = 2*Z.U.closeEnemy[CRUSADER] - Z.U.closeUnits[CRUSADER]; // if(!Z.canBuild(CRUSADER) || cnt[PREACHER] >= 3) crus = 0; cnt[PREACHER] + cnt[CASTLE]
    int proph = 2*Z.U.closeEnemy[PREACHER] + 2*Z.U.closeEnemy[CRUSADER] + 2*Z.U.closeEnemy[PROPHET]; if(!Z.canBuild(PROPHET)) proph = 0;
    int preach = Z.U.closeEnemy[CASTLE]+Z.U.closeEnemy[CHURCH] + Z.U.closeEnemy[PILGRIM] + 2*Z.U.closeEnemy[CRUSADER] - Z.U.closeUnits[PREACHER]; if(!Z.canBuild(PREACHER)) preach = 0;

    if (crus >= proph && crus >= preach && crus > 0) return CRUSADER;
    if (proph >= crus && proph >= preach && proph > 0) return PROPHET;
    if (preach > 0) return PREACHER;
    return MOD;*/
  }
  Action2 safeBuild() {
    // if (Z.CUR.team == 1) return tryBuildAttacker(PREACHER);
	  int numDefenders = Z.U.closeUnits[CRUSADER]+Z.U.closeUnits[PREACHER]+Z.U.closeUnits[PROPHET];
	  if (Z.karbonite < 80 || Z.fuel < 250) return null; // always reserve room for new church
    if (Z.CUR.unit == CASTLE && Z.U.tooMany()) return null;

    int mn = MOD;
    if (Z.CUR.turn > 4 && Z.CUR.unit == CASTLE) {
      for (Robot2 C: Z.robots) if (Z.myCastleID.contains(C.id))
      mn = Math.min(mn,C.castle_talk);
      if (mn < 30 && Z.U.closeAttackers() > mn+2) return null;
    }

    return tryBuildAttacker(Z.U.decideUnit());
  }
  Action2 spamBuild() {
	  return tryBuildAttacker(CRUSADER);
  }
  Action2 panicBuild() {
    if (Z.CUR.unit == CASTLE) {
      if (Z.U.closeEnemyAttackers() == 0) return null;
      if (Z.U.totUnits[PILGRIM] == 0) return null;
    }
    if (!enoughFuel()) return new Action2();
    int w = decideUnit(); if (w == MOD) return null;
    Action2 A = tryBuildAttacker(w); if (A == null) A = new Action2();
    return A;
  }

	double getScore(int pos) { // small score is better
		int x = Z.fdiv(pos,64); int y = pos%64;
		double curx = Z.CUR.x; double cury = Z.CUR.y;
		if(Z.enex == -1) return Z.realdis(x,y,curx,cury);
		double besx = curx; double besy = cury;
        double d = Z.realdis(Z.enex,Z.eney,curx,cury);
        double dx = (Z.enex-curx)/d; double dy = (Z.eney-cury)/d;
        besx += 2*dx; besy += 1.5*dy;

        return Z.realdis(x,y,besx,besy) -0.3*Z.realdis(x,y,curx,cury) +0.07*Z.realdis(x,y,Z.enex,Z.eney);
	}

	boolean near(int pos) {
		return Z.euclidDist(pos) <= 9;
	}

	int comparePatrol(int pos1, int pos2) {
		//if(near(pos1) && !near(pos2)) return -1;
		//else if(!near(pos1) && near(pos2)) return 1;
		if(Z.badPatrol != null) {
			boolean b1 = Z.badPatrol[pos1%64][Z.fdiv(pos1,64)] > 0;
			boolean b2 = Z.badPatrol[pos2%64][Z.fdiv(pos2,64)] > 0;
			if(b1 && !b2) return -1;
			else if(!b1 && b2) return 1;
		}
		double x = getScore(pos1) - getScore(pos2);
		if(x > 0) return 1;
		else if(x == 0) return 0;
		return -1;
	}
  void sortPatrol() {
	  ArrayList<Integer> temp = new ArrayList<>();
  	for(int i = 0; i < Z.patrolcount; i++) temp.add(Z.sortedPatrol[i]);
      Collections.sort(temp, new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
  		  int p1 = Z.patrolPos[a]; int p2 = Z.patrolPos[b];
          return comparePatrol(p1,p2);
        }
      });
    for(int i = 0; i < Z.patrolcount; i++) Z.sortedPatrol[i] = temp.get(i);
  }
  void initPatrol() {
	  Z.patrolcount = 0;
	  for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++) {
      if (Z.patrolVal(x,y,Z.CUR.x,Z.CUR.y) >= 123456) continue;
		  Z.patrolcount++;
	  }

	  Z.sortedPatrol = new int[Z.patrolcount];
	  Z.atkToPatrol = new int[4097]; for (int i = 0; i < 4097; i++) Z.atkToPatrol[i] = -1;
	  Z.patrolPos = new int[Z.patrolcount];

	  Z.patrolcount = 0;
	  for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++) {
      if (Z.patrolVal(x,y,Z.CUR.x,Z.CUR.y) >= 123456) continue;
		  Z.sortedPatrol[Z.patrolcount] = Z.patrolcount;
		  Z.patrolPos[Z.patrolcount] = 64*x + y;
		  Z.patrolcount ++;
	  }

	  sortPatrol();
	  Z.badPatrol = new int[Z.h][Z.w];
  }
  void updatePatrolVars() {
		isOccupiedPatrol = new boolean[Z.patrolcount];
		for (Robot2 R: Z.robots) if (R.team == Z.CUR.team && IS_ATTACKER[Z.type[R.id]])
			if (Z.atkToPatrol[R.id] != -1) isOccupiedPatrol[Z.atkToPatrol[R.id]] = true;

		if (Z.atkToPatrolPrev != null) {
			// figure out who died
			ArrayList<Integer> dead = new ArrayList<Integer>();
			for(int i = 0; i < 4097; i++) {
				if(Z.atkToPatrolPrev[i] != -1 && !isOccupiedPatrol[Z.atkToPatrolPrev[i]]) {
					dead.add(i);
				}
			}

			// update badPatrol
			for(int i = 0; i < dead.size(); i++) {
				int id = dead.get(i);
				int pos = Z.patrolPos[Z.atkToPatrol[id]];
				int x = Z.fdiv(pos,64); int y = pos%64;
				//Z.log("DONT PATROL NEAR: " + Z.coordinates(pos) + ", " + id + " HAS DIED HERE");
				for (int dx = -3; dx <= 3; dx++) for (int dy = -3; dy <= 3; dy++)
          if (dx*dx + dy*dy <= 9) {
            int X = x+dx, Y = y+dy;
					  if(Z.valid(X,Y)) Z.badPatrol[Y][X] = 300;
				  }
      }

			for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++)
				if (Z.badPatrol[y][x] != 0) Z.badPatrol[y][x]--;

			for (int i = 0; i < 4097; i++)
	      if (!isOccupiedPatrol[Z.atkToPatrol[i]]) Z.atkToPatrol[i] = -1;

			// sort again
			// sortPatrol();
		}
	}

  void assignPatrol(int i) {
    //Z.log("assigned smth to patrol at " + Z.coordinates(Z.patrolPos[i]));
    Z.nextSignal = new pi(Z.patrolPos[i]+40000, 2);
    Z.assignedAttackerPos = i;
  }
  boolean tryAssignPatrol() {
	//sortPatrol();
  	for (int i = 0; i < Z.patrolcount; i++)
  		if (!isOccupiedPatrol[Z.sortedPatrol[i]]) {
  			assignPatrol(Z.sortedPatrol[i]);
  			return true;
  		}
  	return false;
  }
  Robot2 newAttacker() {
    int bestDist = MOD; Robot2 P = null;
    for (int dx = -4; dx <= 4; ++dx) for (int dy = -4; dy <= 4; ++dy) {
      int d = dx*dx+dy*dy; if (d > bestDist) continue;
      int x = Z.me.x+dx, y = Z.me.y+dy;
      if (Z.yourRobot(x,y)) {
        Robot2 R = Z.robotMap[y][x];
        if (IS_ATTACKER[R.unit] && IS_ATTACKER[R.signal]) {
          bestDist = d;
          P = R;
        }
      }
    }
    return P;
  }
  void updateAttackerID() {
    if (Z.assignedAttackerPos == -1) return;
    Robot2 R = newAttacker();
    if (R != null) Z.atkToPatrol[R.id] = Z.assignedAttackerPos; // Z.log(R.id+" IS PATROLLER "+Z.coordinates(Z.patrolPos[Z.atkToPatrol[R.id]]));
    Z.assignedAttackerPos = -1;
  }

  Action2 tryBuildEscort() {
    int t = CRUSADER; if (!Z.canBuild(t)) return null;
    Z.nextSignal = new pi(64*Z.escortPos.f+Z.escortPos.s+40000, 2);
    Z.sentEscort[Z.escortPos.f][Z.escortPos.s] = true;
    Z.escortPos = null;
    return Z.tryBuild(t);
  }

  Action2 tryBuildAttacker(int t) {
		if (Z.CUR.unit == CASTLE && Z.me.turn == 900) return null; // turn 900 used to signal attack
    if (!Z.canBuild(t)) return null;
    if (tryAssignPatrol()) return Z.tryBuild(t);
    return null;
  }
}
