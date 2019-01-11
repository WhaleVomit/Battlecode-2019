package bc19;

import static bc19.Consts.*;

public class Building {
    public MyRobot Z;
	public Building(MyRobot z) { Z = z; }

    public Action makePilgrim() {
         if (!Z.canBuild(PILGRIM)) return null;
         Action A = Z.tryBuild(PILGRIM); if (A == null) return A;
         Z.numPilgrims ++;
         Z.log("Built pilgrim");
         return A;
     }
}