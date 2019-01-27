package bc19;

import static bc19.Consts.*;
import java.util.*;

public class Castle extends Building {
  boolean[] isOccupiedKarb, isOccupiedFuel;
  int numKarb, numFuel;

  public Castle(MyRobot z) { super(z); }

  void determineCastleLoc() {
    if (Z.me.turn > 3) return;
    if (Z.me.turn == 1) {
      for (Robot2 R: Z.robots) if (R.team == Z.me.team) {
        Z.myStructID.add(R.id);
        Z.myCastleID.add(R.id);
      }
    }
    for (Robot2 R: Z.robots) if (Z.myCastleID.contains(R.id) && R.castle_talk > 0) {
      int z = R.castle_talk;
      if (Z.me.turn <= 2 && z <= 64) Z.canRush = false;
      if (z > 64) z -= 128;
      z --;

      if (!Z.castleX.containsKey(R.id)) Z.castleX.put(R.id,z);
      else if (!Z.castleY.containsKey(R.id)) {
        Z.castleY.put(R.id,z);
        Z.addStruct(Z.makeRobot(R.id,0,Z.CUR.team,Z.castleX.get(R.id),Z.castleY.get(R.id)));
      }
    }
    int message = 128 * (Z.canRush ? 1 : 0);  message ++;
    if (Z.CUR.turn == 1) message += Z.CUR.x;
    else if (Z.CUR.turn == 2) message += Z.CUR.y;
    else message = 0;
    Z.castle_talk = message;
  }

  boolean ourSide(int pos) {
    int x = Z.fdiv(pos,64); int y = pos%64;
    // Z.log("WHAT "+Z.hsim+" "+Z.wsim);
    if(Z.hsim) {
      int mid = Z.fdiv(Z.h,2);
      if(Z.me.y >= mid) {
        return y >= mid;
      } else {
        if(Z.h % 2 == 1) return y <= mid;
        else return y < mid;
      }
    } else {
      int mid = Z.fdiv(Z.w,2);
      if(Z.me.x >= mid) {
        return x >= mid;
      } else {
        if(Z.w%2 == 1) return x <= mid;
        else return x < mid;
      }
    }
  }

  double countAssigned(int pos) { // how many pilgrims assigned to vicinity?
	  int x = Z.fdiv(pos,64); int y = pos%64;
	  double res = 0;
	  for(int dx = -2; dx <= 2; dx++) {
		  for(int dy = -2; dy <= 2; dy++) {
			  if(Z.valid(x+dx, y+dy) && Z.assigned[y+dy][x+dx]) res += 1.0;
		  }
	  }
	  return res;
  }

  double crowdedFactor(int pos) {
	  double val = Z.sq(countAssigned(pos)+1.0);
	  return val;
  }

  boolean better(int pos1, int pos2) {
    boolean b1 = ourSide(pos1), b2 = ourSide(pos2);
    if (b1 && !b2) return true;
	else if (!b1 && b2) return false;
    return Z.bfs.dist(pos1)*crowdedFactor(pos1) < Z.bfs.dist(pos2)*crowdedFactor(pos2);
  }

  void sortKarb() {
    ArrayList<Integer> temp = new ArrayList<Integer>();
	for(int i = 0; i < Z.karbcount; i++) temp.add(Z.sortedKarb[i]);
	Collections.sort(temp, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
		int p1 = Z.karbPos[a]; int p2 = Z.karbPos[b];
        if(better(p1, p2)) return -1;
        else if(better(p2,p1)) return 1;
        return 0;
      }
    });
    for(int i = 0; i < Z.karbcount; i++) Z.sortedKarb[i] = temp.get(i);
  }

  void sortFuel() {
	ArrayList<Integer> temp = new ArrayList<Integer>();
	for(int i = 0; i < Z.fuelcount; i++) temp.add(Z.sortedFuel[i]);
	Collections.sort(temp, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
		int p1 = Z.fuelPos[a]; int p2 = Z.fuelPos[b];
        if(better(p1, p2)) return -1;
        else if(better(p2,p1)) return 1;
        return 0;
      }
    });
    for(int i = 0; i < Z.fuelcount; i++) Z.sortedFuel[i] = temp.get(i);
  }

  double sideFactor(int pos) { // decreases gradually from our side, steep incline on other side
	if(Z.wsim) {
	  int x = Z.fdiv(pos,64);
	  int disMid = Math.abs(x - Z.fdiv(Z.w,2));
	  if(ourSide(pos)) return disMid;
	  else return 2*disMid;
	} else {
	  int y = pos%64;
	  int disMid = Math.abs(y - Z.fdiv(Z.h,2));
	  if(ourSide(pos)) return disMid;
	  else return 2*disMid;
	}
  }

  boolean betterAgg(int pos1, int pos2) {
    double val1 = Z.bfs.dist(pos1);
    double val2 = Z.bfs.dist(pos2);
    val1 *= sideFactor(pos1);
    val2 *= sideFactor(pos2);
    val1 *= crowdedFactor(pos1);
    val2 *= crowdedFactor(pos2);
    return val1 < val2;
  }

  void sortKarbAgg() {
    ArrayList<Integer> temp = new ArrayList<Integer>();
	for(int i = 0; i < Z.karbcount; i++) temp.add(Z.sortedKarb[i]);
	Collections.sort(temp, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
		int p1 = Z.karbPos[a]; int p2 = Z.karbPos[b];
        if(betterAgg(p1, p2)) return -1;
        else if(betterAgg(p2,p1)) return 1;
        return 0;
      }
    });
    for(int i = 0; i < Z.karbcount; i++) Z.sortedKarb[i] = temp.get(i);
  }

  void sortFuelAgg(){
    ArrayList<Integer> temp = new ArrayList<Integer>();
	for(int i = 0; i < Z.fuelcount; i++) temp.add(Z.sortedFuel[i]);
	Collections.sort(temp, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
		int p1 = Z.fuelPos[a]; int p2 = Z.fuelPos[b];
        if(betterAgg(p1, p2)) return -1;
        else if(betterAgg(p2,p1)) return 1;
        return 0;
      }
    });
    for(int i = 0; i < Z.fuelcount; i++) Z.sortedFuel[i] = temp.get(i);
  }

  void initVars() {
    for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++){
      if (Z.karboniteMap[y][x]) Z.karbcount ++;
      if (Z.fuelMap[y][x]) Z.fuelcount ++;
    }

    Z.sortedKarb = new int[Z.karbcount]; Z.sortedFuel = new int[Z.fuelcount];
    Z.pilToKarb = new int[4097]; Z.pilToFuel = new int[4097];
    for (int i = 0; i < 4097; ++i) Z.pilToKarb[i] = Z.pilToFuel[i] = -1;
    Z.karbPos = new int[Z.karbcount]; Z.fuelPos = new int[Z.fuelcount];
    Z.assigned = new boolean[Z.h][Z.w];

    Z.karbcount = 0; Z.fuelcount = 0;

    for(int x = 0; x < Z.w; x++) for (int y = 0; y < Z.h; y++) {
      if (Z.karboniteMap[y][x]){
        Z.sortedKarb[Z.karbcount] = Z.karbcount;
        Z.karbPos[Z.karbcount] = 64*x + y;
        Z.karbcount ++;
      }
      if (Z.fuelMap[y][x]){
        Z.sortedFuel[Z.fuelcount] = Z.fuelcount;
        Z.fuelPos[Z.fuelcount] = 64*x + y;
        Z.fuelcount ++;
      }
    }

    sortKarb(); sortFuel();
    /*for (int i = 0; i < Z.karbcount; ++i) {
      int p = Z.karbPos[Z.sortedKarb[i]];
      Z.log("AA "+Z.karbPos[Z.sortedKarb[i]]+" "+Z.coordinates(p)+" "+ourSide(p)+" "+Z.bfs.dist(p));
    }*/

    initPatrol();
  }

  void dumpRound() {
      Z.log("================ ROUND " + Z.CUR.turn + " ================ ");
      if (Z.CUR.turn == 3) Z.log("H: "+Z.h+" W: "+Z.w);
      Z.log("SHOULD SAVE: "+Z.shouldSave);
      Z.log("TIME: "+Z.me.time);
      Z.log("KARBONITE: "+Z.karbonite);
      Z.log("FUEL: "+Z.fuel);
      String T = "UNITS "; for (int i = 0; i < 6; ++i) T += " "+Z.U.totUnits[i];
      Z.log(T);
  }

  void updateVars() {
    shouldBuild = true;
    isOccupiedKarb = new boolean[Z.karbcount];
    isOccupiedFuel = new boolean[Z.fuelcount];
    Z.assigned = new boolean[Z.h][Z.w];

    // find current assignments
    for (Robot2 R: Z.robots)
      if (R.team == Z.CUR.team && Z.type[R.id] == 2) {
    	if (R.castle_talk == 30) shouldBuild = false;
        if (Z.pilToKarb[R.id] != -1) {
			isOccupiedKarb[Z.pilToKarb[R.id]] = true;
			int pos = Z.karbPos[Z.pilToKarb[R.id]];
			Z.assigned[pos%64][Z.fdiv(pos,64)] = true;
		}
        if (Z.pilToFuel[R.id] != -1) {
			isOccupiedFuel[Z.pilToFuel[R.id]] = true;
			int pos = Z.fuelPos[Z.pilToFuel[R.id]];
			Z.assigned[pos%64][Z.fdiv(pos,64)] = true;
		}
      }

    for (int i = 0; i < Z.karbcount; i++) if (isOccupiedKarb[i]) numKarb ++;
    for (int i = 0; i < Z.fuelcount; i++) if (isOccupiedFuel[i]) numFuel ++;

    updatePatrolVars();

    if (Z.CUR.unit == CASTLE && Z.myCastle.get(0) == 64 * Z.CUR.x + Z.CUR.y && Z.CUR.turn%10 == 0)
      dumpRound();
  }

  int getMessage(int x) { return x+2000; }

  void assignKarb(int i) {
    Z.log("assigned smth to karbonite at " + Z.coordinates(Z.karbPos[i]));
    Z.nextSignal = new pi(getMessage(Z.karbPos[i]), 2);
    Z.assignedPilgrimPos = new pi(0,i);
  }

  void assignFuel(int i) {
    Z.log("assigned smth to fuel at " + Z.coordinates(Z.fuelPos[i]));
    Z.nextSignal = new pi(getMessage(Z.fuelPos[i]), 2);
    Z.assignedPilgrimPos = new pi(1,i);
  }

  void assignRand() { // assign to random if all positions have been filled
	sortKarb(); sortFuel();
    int tot = Z.karbcount+Z.fuelcount;
    int i = (int)(Math.random()*tot);
    if(i < Z.karbcount) assignKarb(i);
    else assignFuel(i-Z.karbcount);
  }

  boolean tryAssignKarb() {
	sortKarb();
    for (int i = 0; i < Z.karbcount; i++)
      if (!isOccupiedKarb[Z.sortedKarb[i]]) {
        assignKarb(Z.sortedKarb[i]); return true;
      }
    return false;
  }

  boolean tryAssignFuel() {
	sortFuel();
    for (int i = 0; i < Z.fuelcount; i++)
      if (!isOccupiedFuel[Z.sortedFuel[i]]) {
        assignFuel(Z.sortedFuel[i]); return true;
      }
    return false;
  }

  boolean tryAssignAggressive() {
	sortKarbAgg(); sortFuelAgg();
    int tot = Z.karbcount+Z.fuelcount;
    int x = (int)(Math.random()*tot);
    if(x < Z.karbcount) {
      for(int i = 0; i < Z.karbcount; i++) {
        if (!isOccupiedKarb[Z.sortedKarb[i]]) {
          assignKarb(Z.sortedKarb[i]);
          return true;
        }
      }
    } else {
      for (int i = 0; i < Z.fuelcount; i++) {
        if (!isOccupiedFuel[Z.sortedFuel[i]]) {
          assignFuel(Z.sortedFuel[i]);
          return true;
        }
      }
    }
    return false;
  }

  Action2 makePilgrim() {
    double a = Z.karbonite, b = (Z.fuel-Z.FUEL_RATIO*Z.U.totAttackers())/5.0;
    boolean assigned = false;
    if (Math.random() <= .3) {
      assigned = tryAssignAggressive();
    } else {
      if (Z.CUR.turn <= 30 || 2*numKarb <= numFuel) assigned = tryAssignKarb();
      else if (2*numFuel <= numKarb) assigned = tryAssignFuel();
      else if (a < b) assigned = tryAssignKarb();
      else assigned = tryAssignFuel();
    }

    if (!assigned) assignRand();
    return Z.tryBuild(PILGRIM);
  }

  Robot2 newPilgrim() { // closest pilgrim with signal PILGRIM, within distance 3
    int bestDist = MOD; Robot2 P = null;
    for (int dx = -3; dx <= 3; ++dx) for (int dy = -3; dy <= 3; ++dy) {
      int d = dx*dx+dy*dy; if (d > bestDist) continue;
      int x = Z.me.x+dx, y = Z.me.y+dy;
      if (Z.yourRobot(x,y)) {
        Robot2 R = Z.robotMap[y][x];
        if (R.unit == PILGRIM && R.castle_talk == PILGRIM) {
          bestDist = d;
          P = R;
        }
      }
    }
    return P;
  }

  void updatePilgrimID() {
    if (Z.assignedPilgrimPos == null) return;
    Robot2 R = newPilgrim();
    if (R == null) {
      Z.log("NO PILGRIM?");
      Z.assignedPilgrimPos = null;
      return;
    }
    if (Z.assignedPilgrimPos.f == 0) {
      Z.pilToKarb[R.id] = Z.assignedPilgrimPos.s;
      Z.log(R.id+" IS KARB PILGRIM "+Z.pilToKarb[R.id]);
    } else {
      Z.pilToFuel[R.id] = Z.assignedPilgrimPos.s;
      Z.log(R.id+" IS FUEL PILGRIM "+Z.pilToFuel[R.id]);
    }

    Z.assignedPilgrimPos = null;
  }

  void dumpTroopInfo() {
    String T = "";
    T += Z.CUR.turn+" "+Z.toString(Z.myCastle);
    T += "| ";
    for (int i = 0; i < 6; ++i) T += Z.U.totUnits[i]+" ";
    Z.log(T);
  }

  int myPilgrim() {
    int res = 0;
    for (Robot2 R: Z.robots) if (Z.pilToKarb[R.id] != -1 || Z.pilToFuel[R.id] != -1) res ++;
    return res;
  }

  boolean shouldPilgrim() {
    if (Z.nextSignal != null || !Z.canBuild(PILGRIM)) return false;
    int totResource = Z.fuelcount+Z.karbcount;
    if (Z.U.totUnits[PILGRIM] >= totResource/2+3) return false;
    if (Z.CUR.turn <= 20 && Z.U.totUnits[PILGRIM] < Math.min(Z.fdiv(totResource,2),4)) return true;
    if (Z.euclidDist(Z.CUR,Z.closestAttacker(Z.CUR,1-Z.CUR.team)) <= 64) return false;
    return myPilgrim() <= Z.U.closeAttackers() && 20*Z.U.totUnits[PILGRIM] <= Z.fuel;
  }

  Action2 castleBuild() {
    // if (Z.CUR.team == 1) return Z.tryBuild(CRUSADER);
    if (Z.CUR.turn == 1) return null;
    Action2 A = panicBuild(); if (A != null) return A;
    if (!shouldBuild && (Z.karbonite < 80 || Z.fuel < 250)) return null;
    if (Z.U.closeAttackers() < 20 && Z.fuel > 1800) return safeBuild();
    if (shouldPilgrim()) return makePilgrim();
    if (Z.shouldSave || Z.lastSecretAttack >= Z.CUR.turn-30) return null;
    if (Z.me.turn >= 920 && Z.fuel >= 6000-50*(1000-Z.me.turn)) return spamBuild();
    return safeBuild();
  }

  Action2 run() {
    if (Z.me.turn == 1) initVars();
    determineCastleLoc();
    updatePilgrimID(); updateAttackerID(); updateVars();
    if (Z.isSuperSecret && !Z.continuedChain) return Z.tryBuildSecret(PILGRIM);
    Action2 A = castleBuild(); if (A != null && A.type != -1) return A;
    return tryAttack();
  }
}
