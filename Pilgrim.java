package bc19;

import static bc19.Consts.*;

public class Pilgrim {

    MyRobot Z;

    public Pilgrim(MyRobot z) {
        this.Z = z;
    }
    
    boolean shouldBuildChurch() {
		// has to be on resource square with no resource next to it
		if(!Z.karboniteMap[Z.me.y][Z.me.x] && !Z.fuelMap[Z.me.y][Z.me.x]) return false;
		boolean isNextToEmpty = false;
		for(int dx = -1; dx <= 1; dx++) for(int dy = -1; dy <= 1; dy++) {
			int newx = Z.me.x+dx; int newy = Z.me.y+dy;
			if(Z.isEmpty(newx, newy)) {
				if(!Z.karboniteMap[newy][newx] && !Z.fuelMap[newy][newx]) isNextToEmpty = true;
			}
		}
		if(!isNextToEmpty) return false;
		
		// has to be at least 4 away from nearest deposit
		int closeChurch = Z.getClosestUnit(true);
		int closeChurchX = (closeChurch-(closeChurch%64))/64;
		int closeChurchY = closeChurch%64;
		int d1 = MOD;
		if(closeChurch != MOD) d1 = Z.dist[closeChurchY][closeChurchX];
		
		/*
		int closeCastle = Z.getClosestUnit(CASTLE,true);
		int closeCastleX = (closeCastle-(closeCastle%64))/64;
		int closeCastleY = closeCastle%64;
		int d2 = MOD;
		if(closeCastle != MOD) d2 = Z.dist[closeCastleY][closeCastleX];
		if(Math.min(d1,d2) >= 4) {
			Z.log(d1 + " " + d2);
			return true;
		}*/
		
		return false;
	}

    Action run() {
        /*if (Z.me.turn <= 2) {
            Z.log(""+Z.me.turn);
            Z.dumpRobots();
            return null;
        }*/
        /*if (Z.resource == -1) {
        	for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
        		int x = Z.me.x+dx, y = Z.me.y+dy;
        		if (Z.valid(x,y)) {
        			Robot R = Z.getRobot(Z.robotMap[y][x]);
        			if (R != null && Z.isStructure(R) && R.signal > 0) Z.resource = R.signal-1;
        		}
        	}
        	if (Z.resource == -1) 
            Z.log("HUH "+Z.resource);
        }*/
        if (Z.karbonite < 10) Z.resource = 0;
        else if (Z.karbonite > 100) Z.resource = Z.id % 2;
        Robot R = Z.closestAttacker();

        if (R != null && Z.dist(R) <= 100) {
            Z.goHome = true;
            return Z.moveAway(R);
        }
        if(Z.canBuild(CHURCH) && shouldBuildChurch()) {
        	Action A = Z.tryBuild(CHURCH);
        	if(A != null) {
				Z.numChurches++;
				Z.log("Built church");
				return A;
			}
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
