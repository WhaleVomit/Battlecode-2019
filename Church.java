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

    int decideSuperSecret() {
      int t = Z.mostDangerousPreacher(Z.CUR);
      if (t == MOD) return PROPHET;
      return PREACHER;
  	}

    Action2 runSuperSecret() {
      /*if (Math.random() < 0.2) {
        Z.log("TEST CHURCH TLE "+Z.CUR.x+" "+Z.CUR.y);
        return new Action2();
      }*/
      boolean buildPilgrim = true;
      if (Z.continuedChain) buildPilgrim = false;
      if (Z.shouldStopChain()) buildPilgrim = false;
      // if (Z.numOpen(64*Z.CUR.x+Z.CUR.y) <= 1) buildPilgrim = false;

      if (buildPilgrim) return Z.tryBuildSecret(PILGRIM);
      Action2 A = Z.tryBuildSecret(decideSuperSecret());
      if (A == null) return new Action2();
      return A;
    }

    public Action2 run() {
      if (Z.CUR.turn == 1) initPatrol();
	    updatePatrolVars();
      if (Z.isSuperSecret && (Z.CUR.turn <= 3 || !Z.continuedChain)) {
        Action2 A = runSuperSecret();
        if (A != null) return A;
      }
      Action2 A = panicBuild(); if (A != null) return A;
      if (Z.isSuperSecret) return null;
      if (openResources() > closePilgrim()) A = Z.tryBuildNoSignal(PILGRIM);
      if (A == null && Z.U.closeAttackers() < 20 && Z.fuel > 1800) return safeBuild();
      if (Z.me.turn >= 900 && A == null) A = spamBuild();
      return A;
    }
}
