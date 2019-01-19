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

    public Action2 run() {
	    Action2 A = panicBuild();
      if (A != null && openResources() > Z.U.closeUnits[PILGRIM])
        A = Z.tryBuildNoSignal(PILGRIM);
      return A;
    }
}
