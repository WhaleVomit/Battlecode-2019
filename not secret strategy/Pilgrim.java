package bc19;

import static bc19.Consts.*;
import java.util.*;
import java.awt.*;

public class Pilgrim extends Movable {
  ArrayList<Integer> sites;

  public Pilgrim (MyRobot z) {
    super(z);
    sites = new ArrayList<>();
  }

  Action2 mine() {
    if (Z.fuel == 0) return null;
    if (Z.CUR.karbonite <= 18 && Z.karboniteMap[Z.CUR.y][Z.CUR.x]) {
      Z.lastAction = Z.CUR.turn; Z.giveup = false;
      return Z.mineAction();
    }
    if (Z.CUR.fuel <= 90 && Z.fuelMap[Z.CUR.y][Z.CUR.x]) {
      Z.lastAction = Z.CUR.turn; Z.giveup = false;
      return Z.mineAction();
    }
    return null;
  }

  boolean shouldBuildChurch() { // has to be on resource square with no resource next to it
    if (!Z.containsResource(Z.CUR.x,Z.CUR.y)) return false;
    if (Z.bfs.distHome() < churchThreshold) return false;
    Z.castle_talk = 30;
    return Z.canBuild(CHURCH);
  }

  int closeFreeResource(boolean karb, boolean fuel) {
    boolean[][] b = new boolean[Z.h][Z.w];
    for (int x = 0; x < Z.w; x++) for (int y = 0; y < Z.h; y++)
      if (((karb && Z.karboniteMap[y][x]) || (fuel && Z.fuelMap[y][x])) && Z.robotMapID[y][x] <= 0)
        b[y][x] = true;
    return Z.bfs.closestUnused(b);
  }

  Action2 greedy() {
    int x = closeFreeResource(Z.CUR.karbonite != 20, Z.CUR.fuel != 100);
    return Z.bfs.move(x);
  }

  int getResource(pi p) {
    if (p == null) return (Z.id+Z.CUR.turn) % 2;
    if (Z.karboniteMap[p.s][p.f]) return 0;
    return 1;
  }

  void init() {  // Z.CUR.turn == 1
    boolean closeToChurch = false;
    for (Robot2 r : Z.robots) {
      int s = r.signal; // Z.log("signal recieved: "+s);
      if (r.team == Z.CUR.team && r.unit == CASTLE && s >= 2000 && s < 7000) {
        int a = s - 2000;
        Z.resourceLoc = new pi(Z.fdiv(a,64),a%64);
        Z.log("ASSIGNED TO "+Z.resourceLoc.f+" "+Z.resourceLoc.s);
      }
      if (Z.euclidDist(r) <= 2) closeToChurch = true;
    }
    if (Z.resourceLoc == null) {
      if (closeToChurch) Z.log("SERVE CHURCH");
      else Z.log("NOT ASSIGNED?");
    }
    Z.resource = getResource(Z.resourceLoc);
  }

  Action2 react() {
    Robot2 R = Z.closestNotPilgrim(Z.CUR,1-Z.CUR.team);
    if (Z.danger[Z.CUR.y][Z.CUR.x] > 0) {
      Z.goHome = true;
      Action2 A = tryGive(); if (A != null) return A;
      return moveAway(R);
    }
    if (shouldBuildChurch()) {
      Z.lastAction = Z.CUR.turn; Z.giveup = false;
      return Z.tryBuildChurch();
    }
  }

  Action2 considerResourceLoc() {
    if (Z.resourceLoc == null) return null;
    if (Z.CUR.karbonite == 20 && Z.karboniteMap[Z.resourceLoc.s][Z.resourceLoc.f]) return null;
    if (Z.CUR.fuel == 100 && Z.fuelMap[Z.resourceLoc.s][Z.resourceLoc.f]) return null;
    if (Z.CUR.x == Z.resourceLoc.f && Z.CUR.y == Z.resourceLoc.s) return mine();
    if (Z.safe.dist[Z.resourceLoc.s][Z.resourceLoc.f] == MOD) return null;
    return Z.safe.move(Z.resourceLoc.f, Z.resourceLoc.s);
  }

  Action2 moveTowardResource() {
    int bestKarb = MOD, bestFuel = MOD;
    for (int i = 0; i < Z.h; ++i) for (int j = 0; j < Z.w; ++j) {
      if ((Z.passable(j,i) || Z.CUR.x == j && Z.CUR.y == i) && Z.karboniteMap[i][j] && Z.CUR.karbonite < 20) {
        if (Z.safe.dist[i][j] < Z.safe.dist(bestKarb)) bestKarb = 64*j+i;
      }
      if ((Z.passable(j,i) || Z.CUR.x == j && Z.CUR.y == i) && Z.fuelMap[i][j] && Z.CUR.fuel < 100) {
        if (Z.safe.dist[i][j] < Z.safe.dist(bestFuel)) bestFuel = 64*j+i;
      }
    }
    int distKarb = Z.safe.dist(bestKarb), distFuel = Z.safe.dist(bestFuel);
    if (Z.CUR.karbonite < 5 && Z.CUR.fuel < 25) Z.goHome = false;
    if (Z.CUR.karbonite > 16 || Z.CUR.fuel > 80) Z.goHome = true;
    if (Z.bfs.distHome() >= 15) {
  		Z.goHome = Z.CUR.karbonite == 20 && Z.CUR.fuel == 100;
  	}
    if (Z.goHome) return goHome();

    if (!Z.giveup) {
      Action2 A = considerResourceLoc(); if (A != null) return A;

      if (Math.min(distKarb,distFuel) == MOD) return greedy();
      if (Math.min(distKarb,distFuel) <= 2) {
        if (distKarb <= distFuel) return Z.safe.move(bestKarb);
        return Z.safe.move(bestFuel);
      }
      if (Z.resource == 0 && distKarb != MOD) return Z.safe.move(bestKarb);
      return Z.safe.move(bestFuel);
    } else { // inactive pilgrim
      if (Math.min(distKarb,distFuel) <= 5) {
        if (distKarb <= distFuel) return Z.safe.move(bestKarb);
        return Z.safe.move(bestFuel);
      }
      if (Z.euclidDist(Z.bfs.closestStruct(true)) > 100) return Z.bfs.moveYourStruct();
      return null;
    }
  }

  boolean isolated() {
      int minx = MOD, maxx = -MOD, miny = MOD, maxy = -MOD;
      for (int i = Z.CUR.turn-20; i <= Z.CUR.turn; ++i) if (Z.posRecord[i] != null) {
        minx = Math.min(minx,Z.posRecord[i].f);
        maxx = Math.max(maxx,Z.posRecord[i].f);
        miny = Math.min(miny,Z.posRecord[i].s);
        maxy = Math.max(maxy,Z.posRecord[i].s);
      }
      return minx+6 >= maxx && miny+6 >= maxy;
  }

  Action2 run() {
    if (Z.CUR.turn == 1) init();
    Action2 A = react(); if (A != null) return A;
    if (!Z.giveup) {
      if (Z.lastAction <= Z.CUR.turn-100) Z.giveup = true;
      if (Z.lastAction <= Z.CUR.turn-25 && isolated()) Z.giveup = true;
    }
    A = moveTowardResource(); if (A != null) return A;
    return mine();
  }
}
