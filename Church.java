package bc19;

import static bc19.Consts.*;

public class Church extends Building {
    public Church(MyRobot z) { super(z); }
    int openResources() {
      int ret = 0;
      for (int i = -5; i <= 5; ++i) for (int j = -5; j <= 5; ++j)
        if (Z.containsResource(Z.CUR.x+i,Z.CUR.y+j)) ret ++;
      return ret;
    }

    int closePilgrim() {
      int ret = 0;
      for (int i = -5; i <= 5; ++i) for (int j = -5; j <= 5; ++j)
        if (Z.teamRobot(Z.CUR.x+i,Z.CUR.y+j,Z.CUR.team) &&
          Z.robotMap[Z.CUR.y+j][Z.CUR.x+i].unit == PILGRIM) ret ++;
      return ret;
    }

    boolean seesEnemyStruct(Robot2 R) {
      for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) if (i*i+j*j <= 16) {
        int x = Z.CUR.x+i, y = Z.CUR.y+j;
        if (Z.teamRobot(x,y,1-Z.CUR.team)) {
          Robot2 E = Z.robotMap[y][x];
          if (E.isStructure()) return true;
        }
      }
      return false;
    }

    public Action2 run() {
      if (Z.CUR.turn == 1) initPatrol();
      if (Z.isSuperSecret) {
        Action2 A = Z.tryBuild(PREACHER);
        if (A != null) {
          Z.log("HUH "+Z.destination+" "+A.dx+" "+A.dy+" "+(Z.CUR.x+A.dx)+" "+(Z.CUR.y+A.dy));
          Robot2 R = Z.makeRobot(PREACHER,Z.CUR.team,Z.CUR.x+A.dx,Z.CUR.y+A.dy);
          if (seesEnemyStruct(R)) {
            Z.log("MADE PREACHER!");
            return A;
          }
        }
        return Z.tryBuild(PILGRIM);
      }
	    updatePatrolVars();
      Action2 A = panicBuild(); if (A != null) return A;
      if (openResources() > closePilgrim()) A = Z.tryBuildNoSignal(PILGRIM);
      if (A == null && Z.U.closeAttackers() < 20 && Z.fuel > 2000) return safeBuild();
      if (Z.me.turn >= 900 && A == null) A = spamBuild();
      return A;
    }
}
