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

    public Action2 run() {
	    Action2 A = panicBuild(); if (A != null) return A;
      if (openResources() > closePilgrim()) A = Z.tryBuildNoSignal(PILGRIM);
      if (A == null && Z.U.closeAttackers() < 20 && Z.fuel > 2000) return safeBuild();
      if (Z.me.turn >= 900 && A == null) A = spamBuild();
      return A;
    }
}
