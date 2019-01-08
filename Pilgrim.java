package bc19;

public class Pilgrim extends MyRobot {
	MyRobot Z;
	
	public Pilgrim(MyRobot z) {
		this.Z = z;
	}

    Action run() { // errors?
        if (Z.resource == -1) Z.resource = Z.me.id % 2;
        Robot R = Z.closestAttacker();
        // if (R != null) log(getInfo(R));
        if (dist(Z.me,R) <= 100) {
            // log("RUN");
            Z.goHome = true;
            return moveAway(R);
        }
        if (Z.me.karbonite < 5 && Z.me.fuel < 25) Z.goHome = false;
        if (Z.me.karbonite > 15 || Z.me.fuel > 75) Z.goHome = true;
        if (Z.goHome) return Z.moveHome();
        if (Z.resource == 0) {
            if (Z.karboniteMap[Z.me.y][Z.me.x]) return Z.mine();
            return Z.nextMove(Z.getClosest(Z.karboniteMap));
        } else {
            if (Z.fuelMap[Z.me.y][Z.me.x]) return Z.mine();
            return Z.nextMove(Z.getClosest(Z.fuelMap));
        }
    }
}
