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
        return Z.CUR.karbonite <= 18 && Z.karboniteMap[Z.CUR.y][Z.CUR.x] || Z.CUR.fuel <= 90 && Z.fuelMap[Z.CUR.y][Z.CUR.x];
    }

    boolean shouldBuildChurch() {
		// has to be on resource square with no resource next to it
		if(!Z.karboniteMap[Z.CUR.y][Z.CUR.x] && !Z.fuelMap[Z.CUR.y][Z.CUR.x]) return false;
		boolean isNextToEmpty = false;
		for(int dx = -1; dx <= 1; dx++) for(int dy = -1; dy <= 1; dy++) {
			int newx = Z.CUR.x+dx; int newy = Z.CUR.y+dy;
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
				if(Z.robotMap[y+dy][x+dx] != null && Z.robotMapID[y+dy][x+dx] != Z.CUR.id) {
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
				if(Z.robotMapID[y+dy][x+dx] > 0 && Z.robotMapID[y+dy][x+dx] != Z.CUR.id) {
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
        } else Z.resource = (Z.id+Z.CUR.turn) % 2;
	}
	
	int closeFreeResource(boolean karb, boolean fuel) {
		boolean[][] b = new boolean[Z.h][Z.w];
		for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++) 
			if (((karb && Z.karboniteMap[y][x]) || (fuel && Z.fuelMap[y][x])) && Z.robotMapID[y][x] <= 0) 
				b[y][x] = true;
		return Z.closestUnused(b);
	}

	Action2 mine() {
        if (Z.CUR.karbonite <= 18 && Z.karboniteMap[Z.CUR.y][Z.CUR.x] && Z.fuel > 0) return Z.mineAction();
        if (Z.CUR.fuel <= 90 && Z.fuelMap[Z.CUR.y][Z.CUR.x] && Z.fuel > 0) return Z.mineAction();
        return null;
	}

	Action2 greedy() {
		//Z.log("karb: " + Z.CUR.karbonite + ", fuel: " + Z.CUR.fuel + " | get karb? " + (Z.CUR.karbonite != 20) + " get fuel? " + (Z.CUR.fuel != 100));
		int x = closeFreeResource(Z.CUR.karbonite != 20, Z.CUR.fuel != 100);
		//Z.log("currently at: " + Z.CUR.x + " " + Z.CUR.y + ", want to go to " + Z.fdiv(x,64) + " " + (x%64) + " which is " + Z.karboniteMap[(x%64)][Z.fdiv(x,64)] + " " + Z.fuelMap[(x%64)][Z.fdiv(x,64)]);
		if (Z.bfsDist(x) <= 2) return nextMove(x);
		return null;
	}
	
	boolean inDanger() {
        for (int i = 0; i < Z.h; ++i) for (int j = 0; j < Z.w; ++j)
            if (Z.teamAttacker(j,i,1-Z.CUR.team)) {
            	Robot2 R = Z.robotMap[i][j];
            	int dis = Z.euclidDist(R);
            	int dangerous = Z.sq((int)Math.sqrt(VISION_R[R.unit])+2);
            	if (R.unit == PREACHER) dangerous = 64;
            	if (dis <= dangerous) return true;
            }
		return false;
	}

    Action2 run() {
		if (Z.resourceLoc.f == -1) { // Z.CUR.turn == 1
            for (Robot2 r : Z.robots) {
				int s = r.signal; // Z.log("signal recieved: "+s);
                if (r.team == Z.CUR.team && r.unit == CASTLE && s >= 2000 && s < 7000) {
                    int a = s - 2000;
                    Z.resourceLoc = new pi(Z.fdiv(a,64),a%64);
                }
            }
            if (Z.resourceLoc.f == -1) Z.log("DID NOT GET ASSIGNMENT??");
            else Z.log(Z.CUR.id + " received instructions to go to (" + Z.resourceLoc.f + "," + Z.resourceLoc.s+")");
			Z.sendToCastle(6);
        } else Z.sendToCastle();
		setResource();
        
        if (inDanger()) {
			Robot2 R = Z.closestAttacker(Z.CUR,1-Z.CUR.team); 
			Z.goHome = true; 
			return moveAway(R);
		}
        Action2 A = null;
        if (shouldBuildChurch()) { A = Z.tryBuild(CHURCH); if (A != null) return A; }
        A = mine(); if (A != null) return A;

        if (Z.CUR.karbonite < 5 && Z.CUR.fuel < 25) Z.goHome = false;
        if (Z.CUR.karbonite > 16 && b+100 >= a) Z.goHome = true;
        if (Z.CUR.fuel > 80 && a+100 >= b) Z.goHome = true;
        if (Z.CUR.fuel == 100 || Z.CUR.karbonite == 20) Z.goHome = true;
        if (Z.goHome) return moveHome();
        
        if (Z.resourceLoc.f != -1 && Z.robotMapID[Z.resourceLoc.s][Z.resourceLoc.f] <= 0 
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
