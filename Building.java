package bc19;

import static bc19.Consts.*;

public class Building {
    public MyRobot Z;
	public Building(MyRobot z) { Z = z; }

    public Action makePilgrim() {
         if (!Z.canBuild(PILGRIM)) return null;
         int t = 0;
         if (2*Z.type0 < Z.type1 || (10*Z.karbonite < Z.fuel && 2*Z.type1 >= Z.type0)) t = 1;
         else t = 2;
         Z.signal(4*Z.me.turn+t,2);

         Action A = Z.tryBuild(PILGRIM); if (A == null) return A;
         if (2*Z.type0 < Z.type1 || (5*Z.karbonite < Z.fuel && 2*Z.type1 >= Z.type0)) {
             Z.type0 ++; Z.log("KARBONITE");
         } else {
             Z.type1 ++; Z.log("FUEL");
         }

         Z.numPilgrims ++;
         Z.log("Built pilgrim");
         return A;
     }
}