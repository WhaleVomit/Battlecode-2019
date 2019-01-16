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

	/*int getkarboscore(int x, int y) { // checks if this 5x5 square is a good spot to mine
		int numr = 0, nump = 0; // number of resource squares, number of pilgrims
		for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++)
			if (Z.valid(x+dx,y+dy)) {
				if (Z.karboniteMap[y+dy][x+dx]) numr ++;
				int id = Z.robotMapID[y+dy][x+dx];
				if (id > 0 && id != Z.CUR.id) {
					Robot2 r = Z.robotMap[y+dy][x+dx];
					if (r.unit == PILGRIM) nump ++;
				}
			}
		return numr - nump;
	}

	double getfuelscore(int x, int y) { // checks if this 5x5 square is a good spot to mine
		double numr = 0, nump = 0; // number of resource squares, number of pilgrims
		for (int dx = -2; dx <= 2; dx++) for(int dy = -2; dy <= 2; dy++)
			if (Z.valid(x+dx,y+dy)) {
				if (Z.fuelMap[y+dy][x+dx]) numr++;
				int id = Z.robotMapID[y+dy][x+dx];
				if (id > 0 && id != Z.CUR.id) {
					Robot2 r = Z.robotMap[y+dy][x+dx];
					if (r.unit == PILGRIM) nump++;
				}
			}
		return numr - nump;
	}*/

	Action2 mine() {
		if (Z.fuel == 0) return null;
        if (Z.CUR.karbonite <= 18 && Z.karboniteMap[Z.CUR.y][Z.CUR.x]) return Z.mineAction();
        if (Z.CUR.fuel <= 90 && Z.fuelMap[Z.CUR.y][Z.CUR.x]) return Z.mineAction();
        return null;
	}

    boolean shouldBuildChurch() {
		// has to be on resource square with no resource next to it
		if (!Z.containsResource(Z.CUR.x,Z.CUR.y)) return false;
		if (Z.bfsDist(Z.closestStruct(true)) >= churchThreshold) {
			Z.castleTalk(34);
			if(Z.canBuild(CHURCH)) return true;
		}
		return false;
	}

	int closeFreeResource(boolean karb, boolean fuel) {
		boolean[][] b = new boolean[Z.h][Z.w];
		for (int x = 0; x < Z.w; x++) for (int y = 0; y < Z.h; y++)
			if (((karb && Z.karboniteMap[y][x]) || (fuel && Z.fuelMap[y][x])) && Z.robotMapID[y][x] <= 0)
				b[y][x] = true;
		return Z.closestUnused(b);
	}

	Action2 greedy() {
		int x = closeFreeResource(Z.CUR.karbonite != 20, Z.CUR.fuel != 100);
		if (Z.bfsDist(x) <= 2) return nextMove(x);
		return null;
	}

	int getResource(pi p) {
		if (p.f == -1) return (Z.id+Z.CUR.turn) % 2;
		if (Z.karboniteMap[p.s][p.f]) return 0;
		return 1;
	}
	void init() {
		if (Z.resource == -1) { // Z.CUR.turn == 1
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
	        Z.resource = getResource(Z.resourceLoc);
        } else Z.sendToCastle();
	}

	Action2 react() {
		Robot2 R = Z.closestAttacker(Z.CUR,1-Z.CUR.team);
		if (Z.euclidDist(R) <= 100) {
      		Z.goHome = true;
      		Action2 A = tryGive(); if (A != null) return A;
			return moveAway(R);
		}
		// Z.log("TRI "+Z.bfsDist.length+" "+Z.nextMoveSafe.length+" "+Z.dangerous.length);
        if (shouldBuildChurch()) return Z.tryBuildChurch();
	}

	Action2 moveTowardResource() {
        int bestKarb = MOD, bestFuel = MOD;
        for (int i = 0; i < Z.h; ++i) for (int j = 0; j < Z.w; ++j) {
        	if ((Z.passable(j,i) || Z.CUR.x == j && Z.CUR.y == i) && Z.karboniteMap[i][j] && Z.CUR.karbonite < 20) {
        		if (Z.bfsDistSafe[i][j] < Z.bfsDistSafe(bestKarb)) bestKarb = 64*j+i;
        	}
        	if ((Z.passable(j,i) || Z.CUR.x == j && Z.CUR.y == i) && Z.fuelMap[i][j] && Z.CUR.fuel < 100) {
        		if (Z.bfsDistSafe[i][j] < Z.bfsDistSafe(bestFuel)) bestFuel = 64*j+i;
        	}
        }
        int distKarb = Z.bfsDistSafe(bestKarb), distFuel = Z.bfsDistSafe(bestFuel);

        if (Z.CUR.karbonite < 5 && Z.CUR.fuel < 25) Z.goHome = false;
        if (Z.resource == 0 && Z.CUR.karbonite > 16) Z.goHome = true;
        if (Z.resource == 1 && Z.CUR.fuel > 80) Z.goHome = true;

		if (Z.CUR.karbonite > 16 || Z.CUR.fuel > 80) Z.goHome = true;
        /*if (Math.min(distKarb,distFuel) == MOD) {
        	if (Z.CUR.karbonite > 16 || Z.CUR.fuel > 80) Z.goHome = true;
        	else return greedy();
        }*/
        
        if(Z.bfsDistHome() >= churchThreshold) Z.goHome = false;
        if (Z.goHome) return moveHome();
        if (Z.resourceLoc.f != -1 && (Z.passable(Z.resourceLoc.f,Z.resourceLoc.s) || Z.CUR.x == Z.resourceLoc.f && Z.CUR.y == Z.resourceLoc.s)) {
			if(Z.bfsDistSafe[Z.resourceLoc.s][Z.resourceLoc.f] != MOD) return nextMoveSafe(Z.resourceLoc.f, Z.resourceLoc.s);
		}
        if (Math.min(distKarb,distFuel) <= 2) {
        	if (distKarb <= distFuel) return nextMoveSafe(bestKarb);
        	return nextMoveSafe(bestFuel);
        }
        if (Z.resource == 0 && distKarb != MOD) return nextMoveSafe(bestKarb);
        return nextMoveSafe(bestFuel);
	}

    Action2 run() {
    	init(); Action2 A = react(); if (A != null) return A;
        A = moveTowardResource(); if(A != null) return A;
        return mine();
    }
}
