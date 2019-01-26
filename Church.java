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

    int totEnemyUnits(int x, int y) {
      int ret = 0;
      for (int X = x-1; X <= x+1; ++X) for (int Y = y-1; Y <= y+1; ++Y)
        if (Z.teamRobot(X,Y,1-Z.CUR.team)) ret ++;
      return ret;
    }

    boolean containsEnemyStruct(int x, int y) {
      for (int X = x-1; X <= x+1; ++X) for (int Y = y-1; Y <= y+1; ++Y)
        if (Z.teamRobot(X,Y,1-Z.CUR.team))
          if (Z.robotMap[Y][X].isStructure()) return true;
      return false;
    }

    boolean dangerousPreacher(Robot2 R) {
      if (R == null) return false;
      for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) if (i*i+j*j <= 16) {
        int x = R.x+i, y = R.y+j;
        if (Z.passable(x,y)) {
          if (totEnemyUnits(x,y) >= 3) return true;
          if (containsEnemyStruct(x,y)) return true;
        }
      }
      return false;
    }

    boolean canBuildDangerousPreacher(Robot2 R) {
      if (R == null || R.unit != CHURCH) return false;
      for (int i = R.x-1; i <= R.x+1; ++i) for (int j = R.y-1; j <= R.y+1; ++j) if (Z.passable(i,j)) {
        Robot2 P = Z.makeRobot(PREACHER,Z.CUR.team,i,j);
        if (dangerousPreacher(P)) return true;
      }
      return false;
    }

    int potentialAttackers() {
      int ret = 0;
      for (int i = -10; i <= 10; ++i) for (int j = -10; j <= 10; ++j) if (i*i+j*j <= 100) {
        int x = Z.CUR.x+i, y = Z.CUR.y+j;
        if (Z.teamRobot(x,y,Z.CUR.team))
          if (Z.robotMap[y][x].unit == PREACHER || canBuildDangerousPreacher(Z.robotMap[y][x]))
            ret ++;
      }
      return ret;
    }

    public Action2 run() {
      if (Z.CUR.turn == 1) initPatrol();
      if (Z.isSuperSecret) {
        Action2 A = Z.tryBuild(PREACHER);
        if (canBuildDangerousPreacher(Z.CUR)) {
          if (Z.continuedChain) {
            return A;
          } else {
            if (Z.numOpen(64*Z.CUR.x+Z.CUR.y) == 1) return A;
            if (Z.fuel <= 200+50+50+(potentialAttackers()+1)*15 || Z.karbonite <= 50+10+30) return A;
          }
        }
        if (!Z.continuedChain) return Z.tryBuild(PILGRIM);
        if (Z.CUR.turn <= 10) return null;
      }
	    updatePatrolVars();
      Action2 A = panicBuild(); if (A != null) return A;
      if (Z.isSuperSecret) return null;
      if (openResources() > closePilgrim()) A = Z.tryBuildNoSignal(PILGRIM);
      if (A == null && Z.U.closeAttackers() < 20 && Z.fuel > 2000) return safeBuild();
      if (Z.me.turn >= 900 && A == null) A = spamBuild();
      return A;
    }
}
