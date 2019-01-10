package bc19;

import static bc19.Consts.*;

public class Building {
    public MyRobot Z;
	public Building(MyRobot z) { Z = z; }

    public void signalResource() {
        double needKarbonite = Z.karbonite, needFuel = (Z.fuel-100.0*Z.myUnits)/5;
        int t = 0;
        if (needKarbonite+50 < needFuel) t = 1;
        else if (needFuel+50 < needKarbonite) t = 2;
        Z.signal(4*Z.me.turn+t,2);
        Z.log("SIGNAL "+needKarbonite+" "+needFuel+" "+t);
    }

    public Action makePilgrim() {
        if (!Z.canBuild(PILGRIM)) return null;
        signalResource();
        Action A = Z.tryBuild(PILGRIM); if (A == null) return A;
        Z.numPilgrims ++; Z.log("Built pilgrim");
        return A;
     }
}