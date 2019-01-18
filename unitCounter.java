package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class unitCounter {
  MyRobot Z;
  int[] totUnits, closeUnits, closeEnemy;

  public unitCounter(MyRobot Z) {
    this.Z = Z;
    if (Z.CUR.unit == CASTLE) {
      totUnits = new int[6];
      for (Robot2 R: Z.robots) if (R.team == Z.CUR.team)
        if (Z.myCastleID.contains(R.id)) {
          totUnits[0] ++;
        } else {
          if (0 < R.castle_talk && R.castle_talk < 6) Z.type[R.id] = R.castle_talk;
          if (R.unit != -1) Z.type[R.id] = R.unit;
          if (Z.type[R.id] != 0) totUnits[Z.type[R.id]] ++;
        }
    }
    closeUnits = new int[6]; closeEnemy = new int[6];
    for (Robot2 R: Z.robots)  if (Z.euclidDist(R) <= VISION_R[Z.CUR.unit]) {
      if (R.team == Z.CUR.team) closeUnits[R.unit] ++;
      else closeEnemy[R.unit] ++;
    }
  }

  int totAttackers() {
      int res = 0; for (int i = 3; i < 6; ++i) res += totUnits[i];
      return res;
  }
  int totMovable() { return totAttackers()+totUnits[2]; }
  int closeAttackers() {
      int res = 0; for (int i = 3; i < 6; ++i) res += closeUnits[i];
      return res;
  }
  int closeEnemyAttackers() {
      int res = 0; for (int i = 3; i < 6; ++i) res += closeEnemy[i];
      return res;
  }
  int needAttackers() {
      int t = (int)Math.floor(2*Z.enemyDist[Z.CUR.y][Z.CUR.x][0]/3)-3;
      for (int i = 0; i < Z.numAttacks; ++i) t *= 2;
      return t;
  }

  boolean tooMany() {
    return closeAttackers() >= needAttackers()
    && totUnits[2] >= 10
    && Z.fuel < Z.FUEL_RATIO*totAttackers();
  }

  boolean shouldBeginAttack() {
    if (Z.enemyDist[Z.CUR.y][Z.CUR.x][0] < 10) return false;
    if (Z.CUR.turn > 800) return true;
    if (Z.lastAttack >= Z.CUR.turn-5 && closeAttackers() >= 0.75*needAttackers())
      return true;
    return closeAttackers() >= needAttackers() && Z.fuel >= 0.9*Z.FUEL_RATIO*totAttackers();
  }

  int decideUnit() {
    double a = closeUnits[3], b = closeUnits[4]/2.0, c = closeUnits[5];
    if (b <= Math.min(a,c)) return PROPHET;
    if (a <= Math.min(b,c)) return CRUSADER;
    return PREACHER;
  }
}
