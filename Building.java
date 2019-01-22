package bc19;

import static bc19.Consts.*;
import java.util.*;

public class Building extends Attackable {
  boolean shouldBuild = true;
  boolean isOccupiedPatrol[];

  public Building(MyRobot z) { super(z); }

  int needAttackFuel() {
    return 10*Z.U.closeUnits[CRUSADER]+25*Z.U.closeUnits[PROPHET]+15*Z.U.closeUnits[PREACHER];
  }

  boolean enoughFuel() {
    if (Z.fuel < 2*needAttackFuel()+75) return false;
    if (Z.fuel < 200 && Z.CUR.unit == CASTLE) return false;
    return true;
  }

  int decideUnit() {
	  if (Z.U.closeEnemy[CRUSADER]+Z.U.closeEnemy[PREACHER]-Z.U.closeUnits[PREACHER] > 0) return PREACHER;
    if (Z.U.closeEnemyAttackers() == 0) {
      if (Z.U.closeUnits[CRUSADER]+Z.U.closeUnits[PREACHER]+Z.U.closeUnits[PROPHET] < 2) return PROPHET;
      return MOD;
    }
    if ((Z.karbonite < 25 || Z.fuel < 100) && Z.U.closeEnemy[PREACHER]+Z.U.closeEnemy[CASTLE]+Z.U.closeEnemy[CHURCH] == 0) return CRUSADER;
    if (Z.U.closeEnemy[CHURCH] > 0 && Z.U.closeEnemy[PROPHET] == 0) return PREACHER;
    return PROPHET;

    /*int crus = 2*Z.U.closeEnemy[CRUSADER] - Z.U.closeUnits[CRUSADER]; // if(!Z.canBuild(CRUSADER) || cnt[PREACHER] >= 3) crus = 0; cnt[PREACHER] + cnt[CASTLE]
    int proph = 2*Z.U.closeEnemy[PREACHER] + 2*Z.U.closeEnemy[CRUSADER] + 2*Z.U.closeEnemy[PROPHET]; if(!Z.canBuild(PROPHET)) proph = 0;
    int preach = Z.U.closeEnemy[CASTLE]+Z.U.closeEnemy[CHURCH] + Z.U.closeEnemy[PILGRIM] + 2*Z.U.closeEnemy[CRUSADER] - Z.U.closeUnits[PREACHER]; if(!Z.canBuild(PREACHER)) preach = 0;

    if (crus >= proph && crus >= preach && crus > 0) return CRUSADER;
    if (proph >= crus && proph >= preach && proph > 0) return PROPHET;
    if (preach > 0) return PREACHER;
    return MOD;*/
  }

  Action2 safeBuild() {
	  int numDefenders = Z.U.closeUnits[CRUSADER]+Z.U.closeUnits[PREACHER]+Z.U.closeUnits[PROPHET];
	  if (numDefenders >= 2 && (Z.karbonite < 80 || Z.fuel < 250)) return null; // always reserve room for new church
    if (Z.CUR.unit == CASTLE && Z.U.tooMany()) return null;

    int mn = MOD;
    if (Z.CUR.turn > 4 && Z.CUR.unit == CASTLE) {
      for (Robot2 C: Z.robots) if (Z.myCastleID.contains(C.id))
      mn = Math.min(mn,C.castle_talk);
      if (mn < 30 && Z.U.closeAttackers() > mn+2) return null;
    }

    if (Z.CUR.team == 1) return tryBuildAttacker(CRUSADER);
    return tryBuildAttacker(Z.U.decideUnit());
  }

  Action2 spamBuild() {
	  return tryBuildAttacker(CRUSADER);
  }

  Action2 panicBuild() {
    if (Z.CUR.unit == CASTLE && Z.CUR.turn < 30) {
      if (Z.U.closeEnemyAttackers() == 0) return null;
      if (Z.U.totUnits[PILGRIM] == 0) return null;
    }
    if (!enoughFuel()) return new Action2();
    int w = decideUnit(); if (w == MOD) return null;
    Action2 A = tryBuildAttacker(w);
    if (A == null) A = new Action2();
    return A;
  }
  
  boolean betterPatrol(int pos1, int pos2) { // true if pos1 is better than pos2
	  return Z.bfs.dist(pos1) < Z.bfs.dist(pos2);
  }
  
  void sortPatrol() {
	ArrayList<Integer> temp = new ArrayList<>();
	for(int i = 0; i < Z.patrolcount; i++) temp.add(Z.sortedPatrol[i]);
    Collections.sort(temp, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
		int p1 = Z.patrolPos[a]; int p2 = Z.patrolPos[b];
        if(betterPatrol(p1, p2)) return -1;
        else if(betterPatrol(p2,p1)) return 1;
        return 0;
      }
    });
    for(int i = 0; i < Z.patrolcount; i++) Z.sortedPatrol[i] = temp.get(i);
  }
  
  void initPatrol() {
	  Z.patrolcount = 0;
	  for(int x = 0; x < Z.w; x++) {
		  for(int y = 0; y < Z.h; y++) {
			  if(Z.containsStruct(x,y) || Z.containsResource(x,y) || (x+y)%2 == 1) continue;
			  Z.patrolcount++;
		  }
	  }
	  
	  Z.sortedPatrol = new int[Z.patrolcount];
	  Z.atkToPatrol = new int[4097]; for(int i = 0; i < 4097; i++) Z.atkToPatrol[i] = -1;
	  Z.patrolPos = new int[Z.patrolcount];
	  
	  Z.patrolcount = 0;
	  for(int x = 0; x < Z.w; x++) {
		  for(int y = 0; y < Z.h; y++) {
			  if(Z.containsStruct(x,y) || Z.containsResource(x,y) || (x+y)%2 == 1) continue;
			  Z.sortedPatrol[Z.patrolcount] = Z.patrolcount;
			  Z.patrolPos[Z.patrolcount] = 64*x + y;
			  Z.patrolcount ++;
		  }
	  }
	  sortPatrol();
  }
  
  void updatePatrolVars() {
		isOccupiedPatrol = new boolean[Z.patrolcount];
		for (Robot2 R: Z.robots) {
      if (R.team == Z.CUR.team && IS_ATTACKER[Z.type[R.id]]) {
      if (Z.atkToPatrol[R.id] != -1) isOccupiedPatrol[Z.atkToPatrol[R.id]] = true;
      }
		}
	}
  
  void assignPatrol(int i) {
    Z.log("assigned smth to patrol at " + Z.coordinates(Z.patrolPos[i]));
    Z.nextSignal = new pi(Z.patrolPos[i]+40000, 2);
    Z.assignedAttackerPos = i;
  }
  
  boolean tryAssignPatrol() {
		for(int i = 0; i < Z.patrolcount; i++) {
			if(!isOccupiedPatrol[Z.sortedPatrol[i]]) {
				assignPatrol(Z.sortedPatrol[i]);
				return true;
			}
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
    if (R == null) {
      Z.log("NO ATTACKER? " + Z.CUR.x + " " + Z.CUR.y);
      Z.assignedAttackerPos = -1;
      return;
    }
    Z.atkToPatrol[R.id] = Z.assignedAttackerPos;
    Z.log(R.id+" IS PATROLLER "+Z.coordinates(Z.patrolPos[Z.atkToPatrol[R.id]]));

    Z.assignedAttackerPos = -1;
  }
  
  Action2 tryBuildAttacker(int t) {
    if (!Z.canBuild(t)) return null;
    if(tryAssignPatrol()) {
      for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
          int x = Z.CUR.x+dx, y = Z.CUR.y+dy;
          if (t == CHURCH && Z.containsResource(x,y)) continue;
          if (Z.passable(x,y)) return Z.buildAction(t, dx, dy);
      }
      return null;
    } else {
      return null;
    }
  }
}
