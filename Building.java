package bc19;

import static bc19.Consts.*;

public class Building extends Attackable {
  boolean shouldBuild = true;

  public Building(MyRobot z) { super(z); }

  int decideUnit() {
    if (Z.U.closeUnits[CRUSADER]+Z.U.closeUnits[PREACHER]+Z.U.closeUnits[PROPHET] <= 3) return PROPHET;
    int numEnemy = Z.U.closeEnemy[CRUSADER]+Z.U.closeEnemy[PROPHET]+Z.U.closeEnemy[PREACHER];
    if (numEnemy == 0) return MOD;

    int crus = 2*Z.U.closeEnemy[CRUSADER] - Z.U.closeUnits[CRUSADER]; // if(!Z.canBuild(CRUSADER) || cnt[PREACHER] >= 3) crus = 0; cnt[PREACHER] + cnt[CASTLE]
    int proph = 2*Z.U.closeEnemy[PREACHER] + 2*Z.U.closeEnemy[CRUSADER] + 2*Z.U.closeEnemy[PROPHET]; if(!Z.canBuild(PROPHET)) proph = 0;
    int preach = Z.U.closeEnemy[CASTLE]+Z.U.closeEnemy[CHURCH] + Z.U.closeEnemy[PILGRIM] + 2*Z.U.closeEnemy[CRUSADER] - Z.U.closeUnits[PREACHER]; if(!Z.canBuild(PREACHER)) preach = 0;

    if (crus >= proph && crus >= preach && crus > 0) return CRUSADER;
    if (proph >= crus && proph >= preach && proph > 0) return PROPHET;
    if (preach > 0) return PREACHER;
    return MOD;
  }

  Action2 panicBuild() {
    int w = decideUnit();
    if (w == MOD) return null;
    if (Z.me.unit == CASTLE) return Z.tryBuild(w);
    else return Z.tryBuildNoSignal(w);
  }
}
