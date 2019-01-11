package bc19;

import static bc19.Consts.*;
import java.util.*;
import java.awt.*;

public class Pilgrim extends Movable {
    ArrayList<Integer> sites;
    
    public Pilgrim (MyRobot z) { 
    	super(z); 
	    sites = new ArrayList<>();
	}

    boolean shouldMine() {
        return Z.me.karbonite <= 18 && Z.karboniteMap[Z.me.y][Z.me.x] || Z.me.fuel <= 90 && Z.fuelMap[Z.me.y][Z.me.x];
    }

    boolean shouldBuildChurch() {
		// has to be on resource square with no resource next to it
		if(!Z.karboniteMap[Z.me.y][Z.me.x] && !Z.fuelMap[Z.me.y][Z.me.x]) return false;
		boolean isNextToEmpty = false;
		for(int dx = -1; dx <= 1; dx++) for(int dy = -1; dy <= 1; dy++) {
			int newx = Z.me.x+dx; int newy = Z.me.y+dy;
			if(Z.passable(newx, newy) && !Z.karboniteMap[newy][newx] && !Z.fuelMap[newy][newx]) isNextToEmpty = true;
		}
		if(!isNextToEmpty) return false;

		// has to be at least 4 away from nearest deposit
		int closeChurch = Z.getClosestChurch(true);
		int closeChurchX = (closeChurch-(closeChurch%64))/64;
		int closeChurchY = closeChurch%64;
		int d1 = MOD;
		if(closeChurch != MOD) d1 = Z.dist[closeChurchY][closeChurchX];

		int closeCastle = Z.getClosestCastle(true);
		int closeCastleX = (closeCastle-(closeCastle%64))/64;
		int closeCastleY = closeCastle%64;
		int d2 = MOD;
		if(closeCastle != MOD) d2 = Z.dist[closeCastleY][closeCastleX];

		if(Math.min(d1,d2) >= 4) {
			return true;
		}

		return false;
	}

	double getkarboscore(int x, int y) { // checks if this 5x5 square is a good spot to mine
		double numr = 0, nump = 0; // number of resource squares, number of pilgrims
		for(int dx = -2; dx <= 2; dx++) {
			for(int dy = -2; dy <= 2; dy++) if(Z.valid(x+dx,y+dy)) {
				if(Z.karboniteMap[y+dy][x+dx]) numr++;
				if(Z.seenRobot[y+dy][x+dx] != null && Z.seenMap[y+dy][x+dx] != Z.me.id) {
					Robot r = Z.seenRobot[y+dy][x+dx];
					if (r.unit == PILGRIM) nump++;
				}
			}
		}
		return numr - nump;
	}

	double getfuelscore(int x, int y) { // checks if this 5x5 square is a good spot to mine
		double numr = 0, nump = 0; // number of resource squares, number of pilgrims
		for(int dx = -2; dx <= 2; dx++) {
			for(int dy = -2; dy <= 2; dy++) if(Z.valid(x+dx,y+dy)) {
				if(Z.fuelMap[y+dy][x+dx]) numr++;
				if(Z.seenMap[y+dy][x+dx] > 0 && Z.seenMap[y+dy][x+dx] != Z.me.id) {
					Robot r = Z.seenRobot[y+dy][x+dx];
					if (r.unit == PILGRIM) nump++;
				}
			}
		}
		return numr - nump;
	}
	
	Action closeFreeKarbonite() {
		boolean[][] b = new boolean[Z.h][Z.w];
		for(int x = 0; x < Z.w; x++) {
			for(int y = 0; y < Z.h; y++) {
				if(Z.karboniteMap[y][x] && Z.seenMap[y][x] == 0) b[y][x] = true;
			}
		}
		return nextMove(Z.getClosestUnused(b));
	}

	double a,b;

	void setResource() {
        if (Z.resource != -1) return;
        if (a+100 < b) {
        	Z.resource = 0;
        } else if (b+100 < a) {
        	Z.resource = 1;
        } else Z.resource = (Z.id+Z.turn) % 2;
        Z.log("RESOURCE: "+a+" "+b+" "+Z.resource);
    	/*for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
    		int x = Z.me.x+dx, y = Z.me.y+dy;
    		if (Z.valid(x,y) && Z.robotMap[y][x] > 0) {
    			Robot R = Z.getRobot(Z.robotMap[y][x]);
    			if (R != null && Z.isStructure(R) && R.signal > 0) Z.resource = (R.signal%4)-1;
    		}
    	}
    	if (Z.resource == -1) Z.resource = 1;
    	Z.log("RESOURCE: "+Z.resource);*/
	}

    Action run() {
        if(Z.rx == -1){ // don't move until found destination
			if(Z.me.turn == 1) return null;
			Z.castleTalk(2); // haven't been assigned!
            for(Robot r : Z.robots) {
				int s = r.signal;
				//Z.log("signal recieved: "+s);
                if((r.team == Z.me.team) && (r.unit == CASTLE) && (Z.isRadioing(r)) && ((s % 16) == 11)) {
                    int a = (s - (s%16)) / 16;
                    Z.rx = (a - (a%64)) /64;
                    Z.ry = a%64;
                }
            }
            if(Z.rx == -1) return null;
            else {
				Z.log(Z.me.id + " recieved instructions to go to (" + Z.rx + "," + Z.ry+")");
			}
        }
        Z.castleTalk(1); // assigned!
        
        Robot R = Z.closestAttacker();
        if (R != null) { Z.goHome = true; return moveAway(R); }

        if (Z.canBuild(CHURCH) && shouldBuildChurch()) {
        	Action A = Z.tryBuild(CHURCH);
        	if (A != null) { Z.numChurches++; return A; }
        }

        if (Z.me.karbonite <= 18 && Z.karboniteMap[Z.me.y][Z.me.x] && Z.fuel > 0) return Z.mine();
        if (Z.me.fuel <= 90 && Z.fuelMap[Z.me.y][Z.me.x] && Z.fuel > 0) return Z.mine();

        if (Z.me.karbonite < 5 && Z.me.fuel < 25) Z.goHome = false;
        if (Z.me.karbonite > 16 && b+100 >= a) Z.goHome = true;
        if (Z.me.fuel > 80 && a+100 >= b) Z.goHome = true;
        if (Z.goHome) return moveHome();

		if(Z.seenMap[Z.ry][Z.rx] == 0) return nextMove(Z.rx , Z.ry);
		return closeFreeKarbonite();
    }
}
