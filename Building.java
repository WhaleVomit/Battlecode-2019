package bc19;

import static bc19.Consts.*;

public class Building {
  public MyRobot Z;
  public Building(MyRobot z) { Z = z; }
  int decideUnit() {
    // if (Z.CUR.team == 1) return MOD;

    int[] cnto = new int[6];
    int[] cntc = new int[6];
    for(int dx = -10; dx <= 10; dx++) {
      for(int dy = -10; dy <= 10; dy++) if(dx*dx + dy*dy <= VISION_R[Z.CUR.unit]) {
        int x = Z.CUR.x + dx; int y = Z.CUR.y + dy;
        if(!Z.valid(x,y)) continue;
        if(Z.robotMapID[y][x] > 0) {
          Robot2 R = Z.robotMap[y][x];
          if (R.team != Z.CUR.team) cnto[R.unit]++;
          else cntc[R.unit]++;
        }
      }
    }

    int crus = 2*cnto[CRUSADER] - cntc[CRUSADER]; // if(!Z.canBuild(CRUSADER) || cnt[PREACHER] >= 3) crus = 0; cnt[PREACHER] + cnt[CASTLE]
    int proph = 2*cnto[PREACHER] + 2*cnto[CRUSADER] + 2*cnto[PROPHET]; if(!Z.canBuild(PROPHET)) proph = 0;
    int preach = cnto[CASTLE]+cnto[CHURCH] + cnto[PILGRIM] + 2*cnto[CRUSADER] - cntc[PREACHER]; if(!Z.canBuild(PREACHER)) preach = 0;

	int toto = 0;
	for(int i: cnto) toto += i;
    if (toto == 0) return MOD;
    if (cnto[CRUSADER]+cnto[PROPHET]+cnto[PREACHER] == 0 && Z.euclidDist(Z.closestAttacker(Z.CUR,Z.CUR.team)) <= 2) return MOD;
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
