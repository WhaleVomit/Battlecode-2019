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
		if (!isNextToEmpty) return false;

		return Z.bfsDist(Z.closestStruct(true)) >= 4; // has to be at least 4 away from nearest deposit
	}

	double getkarboscore(int x, int y) { // checks if this 5x5 square is a good spot to mine
		double numr = 0, nump = 0; // number of resource squares, number of pilgrims
		for(int dx = -2; dx <= 2; dx++) {
			for(int dy = -2; dy <= 2; dy++) if(Z.valid(x+dx,y+dy)) {
				if(Z.karboniteMap[y+dy][x+dx]) numr++;
				if(Z.robotMap[y+dy][x+dx] != null && Z.robotMapID[y+dy][x+dx] != Z.me.id) {
					Robot2 r = Z.robotMap[y+dy][x+dx];
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
				if(Z.robotMapID[y+dy][x+dx] > 0 && Z.robotMapID[y+dy][x+dx] != Z.me.id) {
					Robot2 r = Z.robotMap[y+dy][x+dx];
					if (r.unit == PILGRIM) nump++;
				}
			}
		}
		return numr - nump;
	}

	double a,b;

	void setResource() {
        a = Z.karbonite; b = (Z.fuel)/5.0; // -100*Z.myUnits
        if (Z.resource != -1) return;
        if (a+100 < b) {
        	Z.resource = 0;
        } else if (b+100 < a) {
        	Z.resource = 1;
        } else Z.resource = (Z.id+Z.ME.turn) % 2;
        // Z.log("RESOURCE: "+a+" "+b+" "+Z.resource);
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
	
	int closeFreeResource() {
		boolean[][] b = new boolean[Z.h][Z.w];
		for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++) 
			if ((Z.karboniteMap[y][x] || Z.fuelMap[y][x]) && Z.robotMapID[y][x] <= 0) 
				b[y][x] = true;
		return Z.closestUnused(b);
	}

	Action mine() {
        if (Z.me.karbonite <= 18 && Z.karboniteMap[Z.me.y][Z.me.x] && Z.fuel > 0) return Z.mine();
        if (Z.me.fuel <= 90 && Z.fuelMap[Z.me.y][Z.me.x] && Z.fuel > 0) return Z.mine();
        return null;
	}

	Action greedy() {
		int x = closeFreeResource();
		if (Z.bfsDist(x) <= 2) return nextMove(x);
		return null;
	}
	
	boolean inDanger() {
		for(Robot2 R: Z.robots) if(R.isAttacker(1-Z.me.team)) {
			int dis = Z.sq((int)Math.sqrt(VISION_R[R.unit])+2);
			if(Z.euclidDist(R.x,R.y) <= dis) return true;
		}
		return false;
	}

    Action run() {
		if (Z.resourceLoc.f == -1) { // don't move until found destination
			if (Z.ME.turn == 1) {
				Z.sendToCastle();
				return null;
			}
            for (Robot2 r : Z.robots) {
				int s = r.signal; // Z.log("signal recieved: "+s);
                if (r.team == Z.me.team && r.unit == CASTLE && s >= 2000 && s < 7000) {
                    int a = s - 2000;
                    Z.resourceLoc = new pi(Z.fdiv(a,64),a%64);
                }
            }
            if (Z.resourceLoc.f == -1) {
				Z.sendToCastle();
            	return null;
            } else Z.log(Z.me.id + " received instructions to go to (" + Z.resourceLoc.f + "," + Z.resourceLoc.s+")");
        }
		Z.sendToCastle(); setResource();
        
<<<<<<< HEAD
        Robot2 R = Z.closestAttacker(1-Z.me.team);
        if (Z.euclidDist(R) <= 100) { Z.goHome = true; return moveAway(R); }
        Action A = null;
        if (shouldBuildChurch()) { A = Z.tryBuild(CHURCH); if (A != null) return A; }
        A = mine(); if (A != null) return A;
=======
        if(inDanger()) {
			Robot2 R = Z.closestAttacker(1-Z.me.team);
			Z.goHome = true; return moveAway(R);
		}

        if (Z.canBuild(CHURCH) && shouldBuildChurch()) {
        	Action A = Z.tryBuild(CHURCH);
        	if (A != null) { Z.numChurches++; return A; }
        }

        if (Z.me.karbonite <= 18 && Z.karboniteMap[Z.me.y][Z.me.x] && Z.fuel > 0) return Z.mine();
        if (Z.me.fuel <= 90 && Z.fuelMap[Z.me.y][Z.me.x] && Z.fuel > 0) return Z.mine();
>>>>>>> e7be60abc8b3c9ec2649bf26b42bda932f7d2f93

        if (Z.me.karbonite < 5 && Z.me.fuel < 25) Z.goHome = false;
        if (Z.me.karbonite > 16 && b+100 >= a) Z.goHome = true;
        if (Z.me.fuel > 80 && a+100 >= b) Z.goHome = true;
        if (Z.goHome) return moveHome();
        
        if (Z.robotMapID[Z.resourceLoc.s][Z.resourceLoc.f] <= 0 
        	&& nextMove(Z.resourceLoc.f, Z.resourceLoc.s) != null) 
        	return nextMove(Z.resourceLoc.f, Z.resourceLoc.s);

        A = greedy(); if (A != null) return A;
        if (Z.resource == 0) {
			boolean[][] karboMap = new boolean[Z.h][Z.w];
			for(int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++) karboMap[y][x] = (Z.robotMapID[y][x] <= 0 && getkarboscore(x,y) > 0);
			return nextMove(Z.closestUnused(karboMap));
		} else {
			boolean[][] fuelMap = new boolean[Z.h][Z.w];
			for(int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++) fuelMap[y][x] = (Z.robotMapID[y][x] <= 0 && getfuelscore(x,y) > 0);
			return nextMove(Z.closestUnused(fuelMap));
		}
    }
}
