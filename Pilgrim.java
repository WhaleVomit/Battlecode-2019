package bc19;

import static bc19.Consts.*;
import java.util.*;

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
		int closeChurch = Z.getClosestUnit(CHURCH,true);
		int closeChurchX = (closeChurch-(closeChurch%64))/64;
		int closeChurchY = closeChurch%64;
		int d1 = MOD;
		if(closeChurch != MOD) d1 = Z.dist[closeChurchY][closeChurchX];
		
		int closeCastle = Z.getClosestUnit(CASTLE,true);
		int closeCastleX = (closeCastle-(closeCastle%64))/64;
		int closeCastleY = closeCastle%64;
		int d2 = MOD;
		if(closeCastle != MOD) d2 = Z.dist[closeCastleY][closeCastleX];
		if(Math.min(d1,d2) >= 4) {
			Z.log(d1 + " " + d2);
			return true;
		}
		return false;
	}
	
	boolean shouldMine(int x, int y) { // checks if this 5x5 square is a good spot to mine
		int numr = 0; // number of resource squares
		int nump = 0; // number of pilgrims
		for(int dx = -2; dx <= 2; dx++) {
			for(int dy = -2; dy <= 2; dy++) {
				if(Z.karboniteMap[y+dy][x+dx] || Z.fuelMap[y+dy][x+dx]) numr++;
				if(Z.valid(x+dx,y+dy) && Z.robotMap[y+dy][x+dx] != -1 && Z.robotMap[y+dy][x+dx] != Z.me.id) {
					Robot r = Z.getRobot(Z.robotMap[y+dy][x+dx]);
					if(r.unit == PILGRIM) nump++;
				}
			}
		}
		return nump < numr;
	}
	
	Action runFirst() { // find a suitable spot to mine
		if(possibleSites.isEmpty()) { // put stuff back in possibleSites
			ArrayList<pii> sites;
			for(int i = 0; i < Z.h; i++) for(int j = 0; j < Z.w; j++) {
				if(Z.karboniteMap[i][j] || Z.fuelMap[i][j]) sites.add(new pii(Z.dist[i][j], 64*j+i));
			}
			Collections.sort(sites);
			for(pii p: sites) possibleSites.add(p.s);
		}
		int site = possibleSites.peek();
		int sitey = site%64;
		int sitex = (site-sitey)/64;
		int d = Z.dist[sitey][sitex];
		if(d > 1) return Z.nextMove(sitex,sitey); // will keep moving to site until 5 away, then next round it will look at next site in queue
		possibleSites.poll();
		return null;
	}

    Action run() {
        if (Z.resource == -1) Z.resource = Z.me.id % 2;
        Robot R = Z.closestAttacker();

        if (R != null && Z.dist(R) <= 100) {
            Z.goHome = true;
            return Z.moveAway(R);
        }
        if(!shouldMine(Z.me.x, Z.me.y)) {
			Action A = runFirst();
			if(A != null) return A;
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
