package bc19;

public class Pilgrim extends MyRobot {
	MyRobot Z;
	
	public Pilgrim(MyRobot z) {
		this.Z = z;
	}

    Action run() {
        if (Z.resource == -1) Z.resource = Z.me.id % 2;
        /*String T = me.karbonite+" "+me.fuel+" "+myCastle.size()+ " | ";
        T += closest(myCastle);
        log(T);*/

        if (Z.resource == 0) {
            if (Z.me.karbonite > 15) return Z.moveHome();
            if (Z.karboniteMap[Z.me.y][Z.me.x]) return Z.mine();
            return Z.nextMove(Z.getClosest(Z.karboniteMap));
        } else {
            if (Z.me.fuel > 75) return Z.moveHome();
            if (Z.fuelMap[Z.me.y][Z.me.x]) return Z.mine();
            return Z.nextMove(Z.getClosest(Z.fuelMap));
        }
    }
}
