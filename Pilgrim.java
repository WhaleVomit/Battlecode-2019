package bc19;

import static bc19.Consts.*;
import java.util.*;
import java.awt.*;

public class Pilgrim {

    MyRobot Z;
    Queue<Integer> possibleSites;

    public Pilgrim(MyRobot z) {
        this.Z = z;
        possibleSites = new LinkedList<>();
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
	
	boolean ok(int x, int y) { // checks if this 5x5 square is a good spot to mine
		int numr = 0, nump = 0; // number of resource squares, number of pilgrims
		for(int dx = -2; dx <= 2; dx++) {
			for(int dy = -2; dy <= 2; dy++) if(Z.valid(x+dx,y+dy)) {
				if(Z.karboniteMap[y+dy][x+dx] || Z.fuelMap[y+dy][x+dx]) numr++;
				if(Z.isNotEmpty(x+dx,y+dy) && Z.robotMap[y+dy][x+dx] != Z.me.id) {
					Robot r = Z.getRobot(Z.robotMap[y+dy][x+dx]);
					if (r.unit == PILGRIM) nump++;
				}
			}
		}
		return nump < numr+2;
	}
	
    int getClosest(boolean[][] B) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < Z.h; ++i)
            for (int j = 0; j < Z.w; ++j)
                if (B[i][j] && ok(j,i) && Z.dist[i][j] < bestDist) {
                    bestDist = Z.dist[i][j];
                    bestPos = 64 * j + i;
                }
        // Z.log("AH "+bestDist+" "+bestPos);
        return bestPos;
    }

	void bubblesort(ArrayList<Integer> arr) {
		int n = arr.size();
		for(int i = 0; i < n-1; i++) {
			for(int j = 0; j < n-i-1; j++) {
				int y = arr.get(j)%64;
				int x = (arr.get(j)-y)/64;
				
				int y1 = arr.get(j+1)%64;
				int x1 = (arr.get(j+1)-y1)/64;
				if(Z.dist[y][x] > Z.dist[y1][x1]) {
					int temp = arr.get(j);
					arr.set(j,arr.get(j+1));
					arr.set(j+1,temp);
				}
			}
		}
	}
	void fakeshuffle(ArrayList<Integer> arr) {
		for(int i = 0; i < arr.size()-1; i++) {
			if(Math.random() < .3) {
				int a = arr.get(i);
				arr.set(i,arr.get(i+1));
				arr.set(i+1,a);
			}
		}
	}
	
	Action runFirst() { // find a suitable spot to mine
		if(possibleSites.isEmpty()) { // put stuff back in possibleSites
 			ArrayList<Integer> sites = new ArrayList<>();
			for(int y = 0; y < Z.h; y++) for(int x = 0; x < Z.w; x++) {
				if(Z.karboniteMap[y][x] || Z.fuelMap[y][x]) sites.add(64*x+y);
			}
 			bubblesort(sites);
			fakeshuffle(sites);
 			for(int p: sites) possibleSites.add(p);
 		}
		int site = possibleSites.peek();
		int sitey = site%64;
		int sitex = (site-sitey)/64;
		int d = Z.dist[sitey][sitex];
		if(d > 1) return Z.nextMove(sitex,sitey); // will keep moving to site until 1 away, then next round it will look at next site in queue
		possibleSites.poll();
		return null;
	}


	void setResource() { 
    	for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
    		int x = Z.me.x+dx, y = Z.me.y+dy;
    		if (Z.valid(x,y)) {
    			Robot R = Z.getRobot(Z.robotMap[y][x]);
    			if (R != null && Z.isStructure(R) && R.signal > 0) Z.resource = R.signal-1;
    		}
    	}
    	if (Z.resource == -1) Z.resource = 1;
    	Z.log("RESOURCE: "+Z.resource);
	}
	
    Action run() {
        if (Z.resource == -1) setResource();

        Robot R = Z.closestAttacker();
        if (R != null && Z.dist(R) <= 100) {
            Z.goHome = true;
            return Z.moveAway(R);
        }

        if (Z.canBuild(CHURCH) && shouldBuildChurch()) {
        	Action A = Z.tryBuild(CHURCH);
        	if(A != null) {
				Z.numChurches++;
				Z.log("Built church");
				return A;
			}
        }

        if (Z.me.karbonite <= 18 && Z.karboniteMap[Z.me.y][Z.me.x]) return Z.mine();
        if (Z.me.fuel <= 90 && Z.fuelMap[Z.me.y][Z.me.x]) return Z.mine();
        if (Z.me.karbonite < 5 && Z.me.fuel < 25) Z.goHome = false;
        if (Z.me.karbonite > 16 || Z.me.fuel > 80) Z.goHome = true;
        if (Z.goHome) return Z.moveHome();

        if (Z.resource == 0 || Z.karbonite < 50) return Z.nextMove(getClosest(Z.karboniteMap));
        else return Z.nextMove(getClosest(Z.fuelMap));
    }
}
