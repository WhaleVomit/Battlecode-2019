package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public abstract class moveMap {
  MyRobot Z;
  int[][] dist, next, close;
  ArrayList<pi> dirs;

  public moveMap(MyRobot Z, int mx) {
    this.Z = Z;
    dist = new int[Z.h][Z.w];
    next = new int[Z.h][Z.w];
    close = new int[Z.h][Z.w];
    dirs = new ArrayList<>();
    for (pi p: Z.dirs) if (p.f*p.f+p.s*p.s <= mx) dirs.add(p);
  }

  abstract boolean ok(int x, int y);

  void upd() {
    for (int y = 0; y < Z.h; ++y)
      for (int x = 0; x < Z.w; ++x)
        dist[y][x] = next[y][x] = close[y][x] = MOD;

    LinkedList<Integer> Q = new LinkedList<>(), Q2 = new LinkedList<>();
    dist[Z.CUR.y][Z.CUR.x] = 0; Q.push(64*Z.CUR.x+Z.CUR.y);

    while (Q.size() > 0) {
      int t = Q.poll(); Q2.push(t);
      int x = Z.fdiv(t,64), y = t % 64; close[y][x] = t;
      for (pi p: dirs) {
        int X = x+p.f; int Y = y+p.s;
        if (ok(X,Y) && dist[Y][X] == MOD) {
          dist[Y][X] = dist[y][x] + 1; next[Y][X] = next[y][x];
          if (next[Y][X] == MOD) next[Y][X] = 64*X+Y;
          Q.push(64*X+Y);
        }
      }
    }
    while (Q2.size() > 0) {
      int t = Q2.poll();
      int x = Z.fdiv(t,64), y = t % 64;
      for (int i = 0; i < 4; ++i) {
        int X = x+xd[i], Y = y+yd[i];
        if (Z.inMap(X,Y) && close[Y][X] == MOD) {
          close[Y][X] = close[y][x];
          Q2.push(64*X+Y);
        }
      }
    }
  }

  int dist(int x, int y) { return dist[y][x]; }
  int dist(int t) { return t == MOD ? MOD : dist(Z.fdiv(t,64),t%64); }
  int next(int x, int y) { return next[y][x]; }
  int next(int t) { return t == MOD ? MOD : next(Z.fdiv(t,64),t%64); }

  int close(int x, int y) { return close[y][x]; }
  int close(int x) { return x == MOD ? MOD : close(Z.fdiv(x,64),x%64); }
  int distClose(int x, int y) { return dist(close(x,y)); }
  int distClose(int x) { return x == MOD ? MOD : distClose(Z.fdiv(x,64),x%64); }

  int closestUnseen() {
    int bestDist = MOD, bestPos = MOD;
    for (int y = 0; y < Z.h; ++y) for (int x = 0; x < Z.w; ++x)
      if (ok(x,y) && Z.robotMapID[y][x] == -1 && dist[y][x] < bestDist) {
        bestDist = dist[y][x]; bestPos = 64*x+y;
      }
    return bestPos;
  }
  int closest(ArrayList<Integer> A) {
    int bestDist = MOD, bestPos = MOD; if (A == null) return bestPos;
    for (int x : A) {
      int d = distClose(x);
      if (d < bestDist) { bestDist = d; bestPos = x; }
    }
    return bestPos;
  }
  int closestUnused(boolean[][] B) {
    int bestDist = MOD, bestPos = MOD; if (B == null) return bestPos;
    for (int i = 0; i < Z.h; ++i) for (int j = 0; j < Z.w; ++j)
        if (B[i][j] && dist[i][j] < bestDist) {
            bestDist = dist[i][j]; bestPos = 64*j+i;
        }
    return bestPos;
  }
  int closestChurch(boolean ourteam) { return closest(ourteam ? Z.myChurch : Z.otherChurch); }
  int closestCastle(boolean ourteam) { return closest(ourteam ? Z.myCastle : Z.otherCastle); }
  int closestStruct(boolean ourteam) {
      int bestCastle = closestCastle(ourteam), bestChurch = closestChurch(ourteam);
      if (distClose(bestCastle) < distClose(bestChurch)) return bestCastle;
      return bestChurch;
  }
  int distHome() { return dist(closestStruct(true)); }

  Action2 move(int x, int y) {
    int t = next(close(x,y)); if (t == MOD) return null;
    int dx = Z.fdiv(t,64)-Z.CUR.x, dy = (t%64)-Z.CUR.y;
    if (!Z.canMove(Z.CUR,dx,dy)) return null;
    return Z.moveAction(dx,dy);
  }
  Action2 move(int t) { return t == MOD ? null : move(Z.fdiv(t,64),t%64); }
  Action2 moveUnseen() { return move(closestUnseen()); }
  Action2 moveYourStruct() {
    int x = closestStruct(true);
    if (x == MOD) return moveUnseen();
    return move(x);
  }
  Action2 moveEnemyStruct() {
    int x = closestStruct(false);
    if (x == MOD) return moveUnseen();
    return move(x);
  }
}
