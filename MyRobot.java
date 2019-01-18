package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class MyRobot extends BCAbstractRobot {
  // note: arrays are by y and then x

  // ROBOTS
  Robot2 ORI, CUR;
  Robot2[] robots;
  pi[] lastPos = new pi[4097];
  Robot2[][] robotMap; // stores last robot seen in pos
  int[][] robotMapID, lastTurn; // stores last id seen in pos, last turn seen

  ArrayList<Integer> myCastle = new ArrayList<>(), otherCastle = new ArrayList<>();
  ArrayList<Integer> myChurch = new ArrayList<>(), otherChurch = new ArrayList<>();
  ArrayList<Integer> myStructID = new ArrayList<>(), myCastleID = new ArrayList<>();

  // MAP
  ArrayList<pi> dirs;
  int w, h; // width, height
  boolean wsim, hsim;
  bfsMap bfs, bfsShort;

  // ALL UNITS
  int lastHealth, castle_talk;
  pi nextSignal;
  unitCounter U;

  // MOVABLE
  boolean goHome;

  // NOT PILGRIM
  boolean updEnemy;
  int[][][] enemyDist;

  // ATTACKERS
  boolean avoidCastle, attackMode; // avoidCastle = if castle is too crowded don't patrol next to it

  // CASTLE
  boolean canRush = false;
  int FUEL_RATIO = 150;
  int lastSignalAttack, lastAttack;

  int karbcount, fuelcount;
  int[] sortedKarb, sortedFuel, pilToKarb, pilToFuel, karbPos, fuelPos;
  Map<Integer,Integer> castleX = new HashMap<>();
  Map<Integer,Integer> castleY = new HashMap<>();
  pi assignedPilgrimPos;

  // PILGRIM
  int[][] danger; safeMap safe;
  int resource = -1; // karbonite or fuel
  pi resourceLoc;

  void sortClose(ArrayList<pi> dirs) {
      Collections.sort(dirs, new Comparator<pi>() {
          public int compare(pi a, pi b) {
            return a.norm()-b.norm();
          }
      });
  }
  int getVal(pi x) {
      int val = 10*(Math.abs(x.f)+Math.abs(x.s));
      if (x.f != 0 && x.s != 0) val ++;
      return val;
  }
  void sortMove(ArrayList<pi> dirs) {
    Collections.sort(dirs, new Comparator<pi>() {
        public int compare(pi a, pi b) {
          return getVal(b)-getVal(a);
        }
    });
  }

  boolean genhsim() { // symmetric with respect to y
    for (int i = 0; i < h-1-i; ++i)
      for (int j = 0; j < w; ++j) {
          if (map[i][j] != map[h-1-i][j]) return false;
          if (karboniteMap[i][j] != karboniteMap[h-1-i][j]) return false;
          if (fuelMap[i][j] != fuelMap[h-1-i][j]) return false;
      }
    return true;
  }
  boolean genwsim() { // symmetric with respect to x
    for (int i = 0; i < h; ++i)
      for (int j = 0; j < w-1-j; ++j) {
        if (map[i][j] != map[i][w-1-j]) return false;
        if (karboniteMap[i][j] != karboniteMap[i][w-1-j]) return false;
        if (fuelMap[i][j] != fuelMap[i][w-1-j]) return false;
      }
    return true;
  }

  void initVars() {
    if (me.unit == CASTLE) canRush = true;
    dirs = new ArrayList<>();
    for (int dx = -3; dx <= 3; ++dx) for (int dy = -3; dy <= 3; ++dy)
      if (dx*dx + dy*dy <= 9) dirs.add(new pi(dx,dy));
    sortMove(dirs);
    w = map[0].length; h = map.length;
    wsim = genwsim(); hsim = genhsim();

    if (me.unit == CRUSADER) {
      bfs = new bfsMap(this,9);
      bfsShort = new bfsMap(this,4);
    } else bfs = new bfsMap(this,4);

    robotMap = new Robot2[h][w];
    robotMapID = new int[h][w];
    for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
      robotMapID[i][j] = -1;

    lastTurn = new int[h][w];
    if (me.unit == PILGRIM) {
      danger = new int[h][w];
      safe = new safeMap(this,4);
    } else {
      enemyDist = new int[h][w][2];
    }
  }

  // MATH
  int fdiv(int a, int b) { return (a-(a%b))/b; }
  int sq(int x) { return x*x; }
  String coordinates(int t) {
    if (t == MOD) return "(??)";
    int y = t%64, x = fdiv(t,64);
    return "("+x+", "+y+")";
  }

  // ACTION
  Action2 moveAction(int dx, int dy) {
      Action2 A = new Action2();
      A.type = 0; A.dx = dx; A.dy = dy;
      return A;
  }
  Action2 mineAction() {
      Action2 A = new Action2();
      A.type = 1;
      return A;
  }
  Action2 giveAction(int dx, int dy, int karb, int fuel) {
      Action2 A = new Action2();
      A.type = 2; A.dx = dx; A.dy = dy; A.karb = karb; A.fuel = fuel;
      return A;
  }
  Action2 attackAction(int dx, int dy) {
      Action2 A = new Action2();
      A.type = 3; A.dx = dx; A.dy = dy;
      return A;
  }
  Action2 buildAction(int unit, int dx, int dy) {
      Action2 A = new Action2();
      A.type = 4; A.unit = unit; A.dx = dx; A.dy = dy;
      return A;
  }
  Action conv(Action2 A) {
    if (A == null || A.type < 0) return null;
    if (A.type == 0) return move(A.dx,A.dy);
    if (A.type == 1) return mine();
    if (A.type == 2) return give(A.dx,A.dy,A.karb,A.fuel);
    if (A.type == 3) return attack(A.dx,A.dy);
    if (A.type == 4) return buildUnit(A.unit,A.dx,A.dy);
    return null;
  }

  // ROBOT
  Robot2 makeRobot(int unit, int team, int x, int y) {
      Robot2 R = new Robot2(null);
      R.id = MOD; R.unit = unit; R.team = team; R.x = x; R.y = y;
      return R;
  }
  Robot2 makeRobot(int id, int unit, int team, int x, int y) {
      Robot2 R = makeRobot(unit,team,x,y); R.id = id;
      return R;
  }
  Robot2 getRobot2(int id) { return new Robot2(getRobot(id)); }
  void dumpRobots() { String T = ""; for (Robot2 R: robots) T += R.getInfo(); log(T); }

  // ARRAYLIST
  void removeDup(ArrayList<Integer> A) {
    ArrayList<Integer> B = new ArrayList<>();
    for (Integer i : A) if (!B.contains(i)) B.add(i);
    A.clear(); for (Integer i : B) A.add(i);
  }
  int min(ArrayList<Integer> A) {
      int res = MOD; for (int i: A) res = Math.min(res,i);
      return res;
  }
  String toString(ArrayList<Integer> A) {
      String res = "{";
      for (int i = 0; i < A.size(); ++i) {
          res += coordinates(A.get(i));
          if (i != A.size()-1) res += ", ";
      }
      res += "}";
      return res;
  }

  // EUCLID DIST
  int euclidDist(int x1, int y1, int x2, int y2) {
      if (x1 == -1 || x2 == -1) return MOD;
      return sq(x1-x2) + sq(y1-y2);
  }
  int euclidDist(Robot2 A, int x, int y) { return A == null ? MOD : euclidDist(A.x,A.y,x,y); }
  int euclidDist(Robot2 A, Robot2 B) {
      if (A == null || B == null) return MOD;
      return euclidDist(A.x,A.y,B.x,B.y);
  }
  int euclidDist(Robot2 A) { return euclidDist(CUR,A); }
  int euclidDist(int x) { return x == MOD ? MOD : euclidDist(CUR,fdiv(x,64),x%64); }
  boolean adjacent(Robot2 A, Robot2 B) { return euclidDist(A,B) <= 2; }
  boolean inVisionRange(Robot2 A, Robot2 B) {
      if (A == null || A.unit == -1) return false;
      return euclidDist(A,B) <= VISION_R[A.unit];
  }
  boolean inVisionRange(int x, int y) { return euclidDist(CUR,x,y) <= VISION_R[CUR.unit]; }

  // DEBUG
  void dumpCastles() {
    log(CUR.getInfo()+" "+myCastle.size()+" "+otherCastle.size()+"\n"+toString(myCastle));
  }
  void dumpInfo() {
    String T = CUR.getInfo();
    T += myCastle.size()+" "+otherCastle.size();
    if (otherCastle.size() > 0) T += " " + coordinates(otherCastle.get(0));
    T += "\n";
    log(T);
  }

  // MAP
  boolean inMap(int x, int y) { return x >= 0 && x < w && y >= 0 && y < h; }
  boolean valid(int x, int y) { return inMap(x,y) && map[y][x]; }
  boolean containsResource(int x, int y) { return valid(x,y) && (karboniteMap[y][x] || fuelMap[y][x]); }
  public boolean passable(int x, int y) { return valid(x, y) && (robotMapID[y][x] <= 0 || robotMapID[y][x] == MOD); }
  boolean containsRobot(int x, int y) { return valid(x, y) && robotMapID[y][x] > 0; }

  boolean teamRobot(int x, int y, int t) { return containsRobot(x,y) && robotMap[y][x].team == t; }
  boolean yourRobot(int x, int y) { return teamRobot(x,y,CUR.team); }
  boolean enemyRobot(int x, int y) { return teamRobot(x,y,1-CUR.team); }
  boolean enemyRobot(int x, int y, int t) { return teamRobot(x,y,1-CUR.team) && robotMap[y][x].unit == t; }

  boolean attacker(int x, int y) { return containsRobot(x,y) && robotMap[y][x].unit > 2; }
  boolean teamAttacker(int x, int y, int t) { return attacker(x,y) && teamRobot(x,y,t);  }
  boolean yourAttacker(int x, int y) { return teamAttacker(x,y,CUR.team); }
  boolean enemyAttacker(int x, int y) { return teamAttacker(x,y,1-CUR.team); }

  boolean adjEnemyPreacher(int x, int y) {
      for (int i = -1; i <= 1; ++i) for (int j = -1; j <= 1; ++j)
          if (enemyRobot(x+i,y+j,PREACHER)) return true;
      return false;
  }
  boolean containsEnemy(int x, int y) {
      for (int i = -1; i <= 1; ++i) for (int j = -1; j <= 1; ++j)
          if (enemyRobot(x+i,y+j)) return true;
      return false;
  }

  int numOpen(int t) { // how many squares around t are free
      int y = t % 64; int x = fdiv(t,64);
      int ret = 0;
      for (int i = x-1; i <= x+1; ++i) for (int j = y-1; j <= y+1; ++j)
          if (passable(i,j)) ret ++;
      return ret;
  }
  int dangerRadius(Robot2 R) {
      if (R.unit == 3 || R.unit == 5) return 64;
      return 100;
  }
  int lastDanger(int x, int y) {
      int ret = -MOD;
      for (int i = y-10; i <= y+10; ++i) for (int j = x-10; j <= x+10; ++j)
          if (teamAttacker(j,i,1-CUR.team)) {
              Robot2 R = robotMap[i][j];
              if (euclidDist(R,j,i) <= dangerRadius(R))
                  ret = Math.max(ret,lastTurn[i][j]);
          }
      return ret;
  }
  Robot2 closestRobot(Robot2 R, int t) {
      Robot2 bes = null;
      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
          if (teamRobot(j,i,t) && euclidDist(R,j,i) < euclidDist(R,bes))
            bes = robotMap[i][j];
      return bes;
  }
  Robot2 closestAttacker(Robot2 R, int t) {
      Robot2 bes = null;
      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
          if (teamAttacker(j,i,t) && euclidDist(R,j,i) < euclidDist(R,bes))
              bes = robotMap[i][j];
      return bes;
  }
  int closestEuclid(ArrayList<Integer> A) {
	  int bestDist = MOD, bestPos = MOD; if (A == null) return bestPos;
	  for (int x : A) {
      int d = euclidDist(x);
      if (d < bestDist) { bestDist = d; bestPos = x; }
    }
    return bestPos;
  }

  public boolean yesStruct(int x, int y) {
     if (!valid(x,y) || robotMapID[y][x] == 0) return false;
    if (robotMapID[y][x] > 0 && !robotMap[y][x].isStructure()) return false;
    return true;
  }
  void addYour(ArrayList<Integer> A, Robot2 R) {
      int p = 64*R.x+R.y;
      if (!yesStruct(R.x,R.y)) return;
      // log("WHAT "+R.x+" "+R.y+" "+p);
      if (R.id != MOD && !myStructID.contains(R.id)) myStructID.add(R.id);
      if (R.unit == CASTLE && R.id != MOD && !myCastleID.contains(R.id)) myCastleID.add(R.id);
      if (A.contains(p)) return;
      A.add(p);
      if (robotMapID[R.y][R.x] == -1) { robotMapID[R.y][R.x] = R.id; robotMap[R.y][R.x] = R; }
  }
  void addOther(ArrayList<Integer> A, Robot2 R) {
      int p = 64*R.x+R.y;
      if (!yesStruct(R.x,R.y) || A.contains(p)) return;
      A.add(p); updEnemy = true;
      if (robotMapID[R.y][R.x] == -1) { robotMapID[R.y][R.x] = R.id; robotMap[R.y][R.x] = R; }
  }
  void rem(ArrayList<Integer> A) {
      ArrayList<Integer> B = new ArrayList<>();
      for (int i : A) {
          int x = fdiv(i,64), y = i%64;
          if (!inMap(x,y)) {
              String res = "CCC "+x+" "+y+" "+i+ " | ";
              for (int j: A) res += j+" ";
              res += " | ";
              for (Integer j: A) res += j+" ";
              log(res);
              return;
          }
          if (yesStruct(x,y)) B.add(i);
      }
      A.clear();
      for (int i : B) A.add(i);
  }
  public void addStruct(Robot2 R) {
      if (!yesStruct(R.x,R.y)) return;
      //log("OOPS "+R.getInfo());
      if (R.unit == CHURCH) {
          if (R.team == CUR.team) addYour(myChurch,R);
          else if (R.team != CUR.team) addOther(otherChurch,R);
      } else {
          if (R.team == CUR.team) {
              addYour(myCastle,R);
              if (wsim) addOther(otherCastle,makeRobot(0,1-CUR.team,w-1-R.x,R.y));
              if (hsim) addOther(otherCastle,makeRobot(0,1-CUR.team,R.x,h-1-R.y));
          } else if(R.team != CUR.team) {
              addOther(otherCastle,R);
              if (wsim) addYour(myCastle,makeRobot(0,CUR.team,w-1-R.x,R.y));
              if (hsim) addYour(myCastle,makeRobot(0,CUR.team,R.x,h-1-R.y));
          }
      }
  }

  // BFS DIST
  void genDanger() {
      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) if (lastTurn[i][j] >= CUR.turn-60)
        if (enemyAttacker(j,i)) {
          Robot2 R = robotMap[i][j];
          int d = dangerRadius(R);
          for (int I = -10; I <= 10; ++I) for (int J = -10; J <= 10; ++J) {
            int D = I*I+J*J;
            if (D <= d && inMap(j+J,i+I)) {
              if (D <= MAX_ATTACK_R[R.unit]) danger[i+I][j+J] = 2;
              else danger[i+I][j+J] = 1;
            }
          }
        }

      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) if (lastTurn[i][j] >= CUR.turn-60)
        if (yourAttacker(j,i)) {
          for (int I = -4; I <= 4; ++I) for (int J = -4; J <= 4; ++J) {
            int D = I*I+J*J;
            if (D <= 16 && inMap(j+J,i+I) && danger[i+I][j+J] == 1)
              danger[i+I][j+J] = 0;
          }
        }

      safe.upd();
  }
  void genEnemyDist() {
      if (enemyDist == null) {
          enemyDist = new int[h][w][2];
          for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
              for (int k = 0; k < 2; ++k) enemyDist[i][j][k] = MOD;
      }
      if (!updEnemy) return;
      updEnemy = false;
      LinkedList<Integer> Q = new LinkedList<Integer>();
      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
          for (int k = 0; k < 2; ++k) enemyDist[i][j][k] = MOD;

      for (int i: otherCastle) {
          Q.add(2*i);
          enemyDist[i%64][fdiv(i,64)][0] = 0;
      }
      for (int i: otherChurch) {
          Q.add(2*i);
          enemyDist[i%64][fdiv(i,64)][0] = 0;
      }
      while (Q.size() > 0) {
          int t = Q.poll();
          int k = t % 2; t = fdiv(t,2);
          int x = fdiv(t,64), y = t % 64;
          for (int z = 0; z < 4; ++z) {
              int X = x+DX_EDGE_ONLY[z], Y = y+DY_EDGE_ONLY[z];
              if (inMap(X,Y)) {
                  int K = k+1; if (map[Y][X]) K = 0;
                  if (K == 2 || enemyDist[Y][X][K] != MOD) continue;
                  enemyDist[Y][X][K] = enemyDist[y][x][k]+1;
                  Q.push(2*(64*X+Y)+K);
              }
          }
      }
  }
  int closestOurSide(boolean[][] B) { // prioritize all locations on our side first
    if(B == null) return MOD;
    if (hsim) {
      // check our side
      int bestPos = MOD, bestDist = MOD;
      int mid = fdiv(h,2);
      if(CUR.y >= mid) {
        for(int x = 0; x < w; x++) for(int y = mid; y < h; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      } else {
        for(int x = 0; x < w; x++) for(int y = 0; y < mid; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      }
      if(bestPos != MOD) return bestPos;
      // check other side
      if(CUR.y >= mid) {
        for(int x = 0; x < w; x++) for(int y = 0; y < mid; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      } else {
        for(int x = 0; x < w; x++) for(int y = mid; y < h; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      }
      return bestPos;
    } else {
      // check our side
      int bestPos = MOD, bestDist = MOD;
      int mid = fdiv(w,2);
      if(CUR.x >= mid) {
        for(int x = mid; x < w; x++) for(int y = 0; y < h; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      } else {
        for(int x = 0; x < mid; x++) for(int y = 0; y < h; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      }
      if(bestPos != MOD) return bestPos;
      // check other side
      if(CUR.x >= mid) {
        for(int x = 0; x < mid; x++) for(int y = 0; y < h; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      } else {
        for(int x = mid; x < w; x++) for(int y = 0; y < h; y++) {
          if(B[y][x] && bfs.dist[y][x] < bestDist && robotMapID[y][x] <= 0) {
            bestDist = bfs.dist[y][x]; bestPos = 64*x+y;
          }
        }
      }
      return bestPos;
    }
  }

  boolean canMove(Robot2 R, int dx, int dy) {
      if (R == null || R.unit == -1) return false;
      if (dx == 0 && dy == 0) return false;
      int u = R.unit, d = dx*dx+dy*dy;
      return passable(R.x+dx,R.y+dy) && d <= MOVE_SPEED[u] && d*MOVE_F_COST[u] <= fuel;
  }

  // CASTLE LOCATIONS / ATTACK
  int farthestDefenderRadius() {
      int t = 0;
      for (int i = -10; i <= 10; ++i) for (int j = -10; j <= 10; ++j)
          if (i*i+j*j <= 100 && yourAttacker(CUR.x+i,CUR.y+j)) {
              Robot2 R = robotMap[CUR.y+j][CUR.x+i];
              if (fdiv(R.castle_talk,14) % 2 == 0) t = Math.max(t,i*i+j*j);
          }
      return t;
  }
  void startAttack() {
    if (CUR.unit != CASTLE) return;
    for (Robot2 R: robots)
        if (R.team == CUR.team && R.unit == CASTLE && R.castle_talk == 255)
            lastAttack = CUR.turn;
    if (nextSignal != null || lastSignalAttack >= CUR.turn-10) return;
    if (U.shouldBeginAttack()) { // (CUR.team == 0 && attackMode))
        int r = farthestDefenderRadius();
        if (r > 0 && fuel >= r) {
            lastSignalAttack = CUR.turn;
            log("SIGNAL ATTACK "+CUR.x+" "+CUR.y+" "+r+" "+fuel+" "+U.closeAttackers());
            nextSignal = new pi(20000,r); castle_talk = 255;
        }
    }
  }
  public boolean isRushing() {
    int x = bfs.closestStruct(true);
    return canRush && CUR.unit != PILGRIM && CUR.turn <= 30 && enemyDist[x%64][fdiv(x,64)][0] <= 25;
  }

  void updateAttackMode() {
      if (CUR.unit == CASTLE && enemyDist[CUR.y][CUR.x][0] > 25) canRush = false;
      if (isRushing() && U.closeAttackers() >= 3) attackMode = true;
      // log(CUR.getInfo()+toString(myCastle)+" "+toString(otherCastle));
  }
  int compress(int i) {
      int x = fdiv(i, 64), y = i % 64;
      x = fdiv(x, 8); y = fdiv(y, 8);// approximating location
      return 8*x+y;
  }
  int encodeCastleLocations() {
      int myLoc = compress(64*CUR.x+CUR.y);
      ArrayList<Integer> locs = new ArrayList<>();

      for (int i : myCastle) {
          int loc = compress(i);
          if (loc != myLoc && !locs.contains(loc)) locs.add(loc);
      }

      while (locs.size() < 2) locs.add(myLoc);
      // log("SEND "+locs.get(0)+" "+locs.get(1));
      // end result is between 7000 and 11100 (upper bound is actually a bit lower but just to be safe)
      int ret = 64*locs.get(0)+locs.get(1)+7000;
      if (isRushing()) ret += 4096;
      return ret;
  }
  void fill8by8(int approxX, int approxY) {
      // log("FILL "+approxX+" "+approxY);
    for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++) {
      int x = 8 * approxX + i;
      int y = 8 * approxY + j;
      addStruct(makeRobot(0,CUR.team,x,y));
    }
  }
  void fill8by8(int approxID) { fill8by8(fdiv(approxID, 8), approxID % 8); }
  void decodeCastleLocations(Robot2 parentCastle) {
      int sig = parentCastle.signal; sig -= 7000;
      if (sig >= 4096) {
        sig -= 4096;
        canRush = true;
      }
      int t = compress(64*parentCastle.x+parentCastle.y);
      // log("RECEIVED "+sig+" "+t);
      int a = sig%64; if (t != a) fill8by8(a);
      a = fdiv(sig,64); if (t != a) fill8by8(a);
  }

  // BUILD
  public boolean canBuild(int t) {
      if (!(fuel >= CONSTRUCTION_F[t] && karbonite >= CONSTRUCTION_K[t])) return false;
      for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
          int x = CUR.x+dx, y = CUR.y+dy;
          if (t == CHURCH && containsResource(x,y)) continue;
          if (passable(x,y)) return true;
	    }
      return false;
  }
  public Action2 tryBuildChurch() {
    if(!canBuild(CHURCH)) return null;
    int bestDx = MOD, bestDy = MOD, bestCnt = -MOD; // try to build adjacent to as many as possible
    for (int dx = -1; dx <= 1; dx++) {
    	for(int dy = -1; dy <= 1; dy++) {
    		int x = CUR.x+dx; int y = CUR.y+dy;
    		if(passable(x,y) && !karboniteMap[y][x] && !fuelMap[y][x]) {
    			int cnt = 0;
    			for(int dx2 = -1; dx2 <= 1; dx2++) {
    				for(int dy2 = -1; dy2 <= 1; dy2++) {
    					if(!(dx2 == 0 && dy2 == 0)) {
    						int x2 = x+dx2; int y2 = y+dy2;
    						if(valid(x2,y2) && (karboniteMap[y2][x2] || fuelMap[y2][x2])) cnt++;
    					}
    				}
    			}
    			if(cnt > bestCnt) {
    				bestDx = dx;
    				bestDy = dy;
    				bestCnt = cnt;
    			}
    		}
    	}
    }
    if(bestDx == MOD) return null;
    return buildAction(CHURCH, bestDx, bestDy);
  }
  public Action2 tryBuild(int t) {
      if (!canBuild(t)) return null;
      if (CAN_ATTACK[t]) nextSignal = new pi(encodeCastleLocations(),2);
      for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
          int x = CUR.x+dx, y = CUR.y+dy;
          if (t == CHURCH && containsResource(x,y)) continue;
          if (passable(x,y)) return buildAction(t, dx, dy);
      }
      return null;
  }
  public Action2 tryBuildNoSignal(int t) {
      if (!canBuild(t)) return null;
      for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy)
          if (passable(CUR.x + dx, CUR.y + dy)) return buildAction(t, dx, dy);
      return null;
  }

  int getSignal(Robot2 R) {
      return 625*(R.unit-3)+25*(R.x-CUR.x+12)+(R.y-CUR.y+12)+1;
  }

  boolean clearVision(Robot2 R) {
      if (CUR.unit == CASTLE && fdiv(R.castle_talk,7) % 2 == 1) return false;
      for (int i = -10; i <= 10; ++i)
          for (int j = -10; j <= 10; ++j) {
              if (i*i+j*j > VISION_R[R.unit]) continue;
              if (enemyRobot(R.x+i,R.y+j)) return false;
          }
      for (Robot2 A: robots) if (A.team == CUR.team && 0 < A.signal && A.signal < 2000)
          if (euclidDist(R,A) <= A.signal_radius) return false;
      return true;
  }

  void checkSignal() {
    for (Robot2 R: robots)
      if (R.team == CUR.team && 0 < R.signal && R.signal < 2000) {
          int tmp = R.signal-1;
          int type = fdiv(tmp,625)+3; tmp %= 625;
          int x = fdiv(tmp,25)-12; x += R.x;
          int y = (tmp%25)-12; y += R.y;
          // log("ADDED "+CUR.coordinates()+" "+R.coordinates()+" "+x+" "+y);
          robotMapID[y][x] = MOD; robotMap[y][x] = makeRobot(type,1-CUR.team,x,y);
          lastTurn[y][x] = CUR.turn;
      } else if (R.team == CUR.team && R.unit == CASTLE && R.signal >= 7000 && R.signal < 20000 && adjacent(CUR,R)) {
          if (CUR.unit == CASTLE) continue;
          decodeCastleLocations(R);
      } else if (myStructID.contains(R.id) && R.signal == 20000) attackMode = true;
  }

  boolean superseded(int x, int y) {
        for (int i = -6; i <= 6; ++i) for (int j = -6; j <= 6; ++j)
            if (yourRobot(x+i,y+j)) {
                if (i == 0 && j == 0) continue;
                Robot2 R = robotMap[y+j][x+i];
                if (Math.sqrt(VISION_R[me.unit])+Math.sqrt(i*i+j*j) <= Math.sqrt(VISION_R[R.unit])) return true;
            }
        return false;
    }

  // COMMUNICATION

  void warnOthers() { // CUR.x, CUR.y are new pos, not necessarily equal to me.x, me.y;
    if (fuel < 100 || superseded(CUR.x,CUR.y) || nextSignal != null) return;
    Robot2 R = closestAttacker(ORI,1-CUR.team); if (euclidDist(ORI,R) > VISION_R[CUR.unit]) return;
    int numEnemies = U.closeEnemyAttackers();
    // try to activate around 2*numEnemies allies
    // count number allies already activated
    int cnt = 0;
    for (int dx = -10; dx <= 10; dx++) for(int dy = -10; dy <= 10; dy++) {
  		int x = CUR.x+dx; int y = CUR.y+dy;
  		if(yourAttacker(x, y) && !clearVision(robotMap[y][x])) cnt++;
  	}
		int necessary = Math.max(2*numEnemies - cnt, 0);
		// activate as much as necessary

    ArrayList<pi> allies = new ArrayList<>();
    for(int dx = -4; dx <= 4; dx++) for(int dy = -4; dy <= 4; dy++) {
			int x = CUR.x+dx, y = CUR.y+dy;
			if (dx*dx + dy*dy <= 16 && yourAttacker(x,y) && clearVision(robotMap[y][x]))
        allies.add(new pi(dx,dy));
		}
		sortClose(allies);

		int ind = Math.min(necessary-1, dirs.size()-1); if(ind == -1) return;
		int dx = dirs.get(ind).f; int dy = dirs.get(ind).s;
    int needDist = dx*dx + dy*dy;
    if (needDist > 0) {
        // log("SIGNAL ENEMY: OPOS "+ORI.coordinates()+", CPOS "+CUR.coordinates()+", EPOS "+R.coordinates()+" "+getSignal(R));
        nextSignal = new pi(getSignal(R),needDist);
    }
  }

  void pr() {
		for(int i = 0; i < h; i++) {
			String s = "";
			for(int j = 0 ; j < w; j++) {
				s += " " + robotMapID[i][j];
			}
			log(s);
		}
	}

  // TURN
  void updateVars() {
      ORI = new Robot2(me); CUR = new Robot2(me);
      robots = new Robot2[getVisibleRobots().length];
      for (int i = 0; i < robots.length; ++i) robots[i] = new Robot2(getVisibleRobots()[i]);

      for (int i = 1; i <= 4096; ++i) lastPos[i] = null;
      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
        if (robotMapID[i][j] > 0 && robotMapID[i][j] < MOD)
          lastPos[robotMapID[i][j]] = new pi(j,i);

      checkSignal(); // info might not be accurate: some troops may be dead already
      for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) {
          int t = getVisibleRobotMap()[i][j];
          if (t != -1) {
              lastTurn[i][j] = CUR.turn;
              robotMapID[i][j] = t;
              if (robotMapID[i][j] == 0) robotMap[i][j] = null;
              else {
                  Robot2 R = getRobot2(t);
                  if (lastPos[t] != null && robotMapID[lastPos[t].s][lastPos[t].f] == t) {
                      robotMapID[lastPos[t].s][lastPos[t].f] = -1;
                      robotMap[lastPos[t].s][lastPos[t].f] = null;
                      robotMapID[i][j] = t;
                  }
                  lastPos[t] = new pi(j,i); robotMap[i][j] = R;
                  addStruct(R);
              }
          }
      }
      rem(myCastle); rem(otherCastle); rem(myChurch); rem(otherChurch);

      bfs.upd(); if (CUR.unit == CRUSADER) bfsShort.upd();
      castle_talk = -1; nextSignal = null;
      U = new unitCounter(this);
      if (CUR.unit == PILGRIM) genDanger();
      else genEnemyDist();
      updateAttackMode();
  }

  public Action2 chooseAction() {
    Action2 A = null;
      switch (CUR.unit) {
          case CASTLE: {
              Castle C = new Castle(this);
              A = C.run();
              break;
          }
          case CHURCH: {
              Church C = new Church(this);
              A = C.run();
              break;
          }
          case PILGRIM: {
              Pilgrim C = new Pilgrim(this);
              A = C.run();
              break;
          }
          case CRUSADER: {
              Crusader C = new Crusader(this);
              A = C.run();
              break;
          }
          case PROPHET: {
              Prophet C = new Prophet(this);
              A = C.run();
              break;
          }
          case PREACHER: {
              Preacher C = new Preacher(this);
              A = C.run();
              break;
          }
      }
      if (A == null) A = new Action2();
      if (A.type == 0) {
          robotMap[CUR.y][CUR.x] = null; robotMapID[CUR.y][CUR.x] = 0;
          CUR.x += A.dx; CUR.y += A.dy;
          robotMap[CUR.y][CUR.x] = CUR; robotMapID[CUR.y][CUR.x] = CUR.id;
      }
      return A;
  }
  boolean seeEnemy() {
    boolean b = false;
    for (int i = -14; i <= 14; ++i) for (int j = -14; j <= 14; ++j)
        if (i*i+j*j <= 196 && enemyRobot(CUR.x+i,CUR.y+j)) {
					b = true;
				}
    return b;
  }
  void finish() {
    lastHealth = CUR.health;
    if (castle_talk == -1) {
      if (CUR.unit == CASTLE) {
        castle_talk = Math.min(U.closeAttackers(),254);
      } else {
        castle_talk = CUR.unit;
        if (CUR.unit == PILGRIM && CUR.turn == 1) castle_talk = 6;
        if (seeEnemy()) castle_talk += 7;
        if (attackMode) castle_talk += 14;
      }
    }
    // log("CASTLE TALK "+castle_talk);
    castleTalk(castle_talk);
    if (nextSignal != null) signal(nextSignal.f,nextSignal.s);
  }

  public Action turn() {
    if (me.turn == 1) initVars();
    updateVars();
    if (me.turn == 1) log("UNIT: "+CUR.unit);

    Action2 A = chooseAction();
    warnOthers();
    startAttack(); finish();
    return conv(A);
  }
}
