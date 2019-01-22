package bc19;

import static bc19.Consts.*;

public class Building extends Attackable {
  boolean shouldBuild = true;

  public Building(MyRobot z) { super(z); }

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
  int decideUnit() {
	  if (Z.U.closeEnemy[CRUSADER]+Z.U.closeEnemy[PREACHER]-Z.U.closeUnits[PREACHER] > 0) return PREACHER;
    if (Z.U.closeEnemyAttackers() == 0) {
      if (reallyCloseAttackers() < 2) return PROPHET;
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

    if (Z.CUR.team == 1) return Z.tryBuild(CRUSADER);
    return Z.tryBuild(Z.U.decideUnit());
  }

  Action2 spamBuild() {
	  return Z.tryBuild(CRUSADER);
  }

  Action2 panicBuild() {
    if (Z.CUR.unit == CASTLE && Z.CUR.turn < 30) {
      if (Z.U.closeEnemyAttackers() == 0) return null;
      if (Z.U.totUnits[PILGRIM] == 0) return null;
    }
    if (!enoughFuel()) return new Action2();
    int w = decideUnit(); if (w == MOD) return null;
    Action2 A = null;
    if (Z.CUR.unit == CASTLE) A = Z.tryBuild(w);
    else A = Z.tryBuildNoSignal(w);
    if (A == null) A = new Action2();
    return A;
  }
}
