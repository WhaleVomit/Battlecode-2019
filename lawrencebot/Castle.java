package bc19;
import static bc19.Consts.*;
import java.util.*;
import java.lang.*;

public class Castle extends Building {
    public Castle(MyRobot z) { super(z); }
    void bfs2(int maxdis) { // only used by castle
        Z.dist = new int[Z.h][Z.w]; Z.pre = new int[Z.h][Z.w];

        for (int i = 0; i < Z.h; ++i)
            for (int j = 0; j < Z.w; ++j) {
                Z.dist[i][j] = MOD; Z.pre[i][j] = MOD;
            }

        LinkedList<Integer> L = new LinkedList<>();

        Z.dist[Z.me.y][Z.me.x] = 0; L.add(64 * Z.me.x + Z.me.y);
        while (!L.isEmpty()) {
            int x = L.poll(); int y = x % 64; x = Z.fdiv(x,64);

            for (int dx = -3; dx <= 3; ++dx) {
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x + dx, Y = y + dy;
                    if (dx*dx+dy*dy <= maxdis && Z.valid(X, Y) && Z.dist[Y][X] == MOD) {
                        Z.dist[Y][X] = Z.dist[y][x] + 1;
                        if (Z.pre[y][x] == MOD) Z.pre[Y][X] = 64 * X + Y;
                        else Z.pre[Y][X] = Z.pre[y][x];
                        L.add(64 * X + Y);
                    }
                }
			}
        }
    }
    void sortKarb(){
        for (int i = 0; i < Z.karbcount-1; i++) {
            for (int j = 0; j < Z.karbcount - i - 1; j++) {
				Z.log(Z.bfsDist(Z.sortedKarb[j]) + " " + Z.bfsDist(Z.sortedKarb[j+1]));
                if (Z.bfsDist(Z.sortedKarb[j]) > Z.bfsDist(Z.sortedKarb[j+1])) {
                    // swap arr[j+1] and arr[i]
                    int temp = Z.sortedKarb[j];
                    Z.sortedKarb[j] = Z.sortedKarb[j + 1];
                    Z.sortedKarb[j + 1] = temp;
                }
            }
        }
    }
    void sortFuel(){
        for (int i = 0; i < Z.fuelcount-1; i++) {
            for (int j = 0; j < Z.fuelcount - i - 1; j++) {
                if (Z.bfsDist(Z.sortedFuel[j]) > Z.bfsDist(Z.sortedFuel[j+1])) {
                    // swap arr[j+1] and arr[i]
                    int temp = Z.sortedFuel[j];
                    Z.sortedFuel[j] = Z.sortedFuel[j + 1];
                    Z.sortedFuel[j + 1] = temp;
                }
            }
        }
    }
    
    void initVars() {
		bfs2(MOVE_SPEED[PILGRIM]);
        for(int x = 0;x < Z.w; x++){
            for(int y = 0;y < Z.h; y++){
                if(Z.karboniteMap[y][x]){
                    Z.karbcount++;
                }
                if(Z.fuelMap[y][x]){
                    Z.fuelcount++;
                }
            }
        }
        Z.sortedKarb = new int[Z.karbcount];
        Z.sortedFuel = new int[Z.fuelcount];
        Z.karbToPil = new int[Z.karbcount];
        Z.fuelToPil = new int[Z.fuelcount];
        Z.karbPos = new int[Z.karbcount];
        Z.fuelPos = new int[Z.fuelcount];
        Z.isOccupiedKarb = new boolean[Z.karbcount];
        Z.isOccupiedFuel = new boolean[Z.fuelcount];
        
        Z.karbcount = 0;
        Z.fuelcount = 0;
        for(int x = 0;x < Z.w; x++){
            for(int y = 0;y < Z.h; y++){
                if(Z.karboniteMap[y][x]){
                    Z.sortedKarb[Z.karbcount] = Z.karbcount;
                    Z.karbPos[Z.karbcount] = 64*x + y;
                    Z.karbcount++;
                }
                if(Z.fuelMap[y][x]){
                    Z.sortedFuel[Z.fuelcount] = Z.fuelcount;
                    Z.fuelPos[Z.fuelcount] = 64*x + y;
                    Z.fuelcount++;
                }
            }
        }
        sortKarb();
        Z.log(Z.me.x + " " + Z.me.y + "!!!");
        for(int i: Z.sortedKarb) {
			int pos = Z.karbPos[i];
			Z.log(Z.fdiv(pos,64)+" "+pos%64);
		}
		Z.log("----");
        sortFuel();
    }
    void updateVars() {
        for(int i = 0; i < Z.karbcount; i++) {
            Z.isOccupiedKarb[i] = false;
        }
        for(int i = 0; i < Z.fuelcount; i++) {
            Z.isOccupiedFuel[i] = false;
        }
        // find current assignments
        for (Robot R: Z.robots) if(R.castle_talk == PILGRIM) {
            int ind = -1;
            for(int i = 0; i < Z.karbcount; i++) {
                if(Z.karbToPil[i] == R.id) ind = i;
            }
            if(ind != -1) Z.isOccupiedKarb[ind] = true;
        }
        for (Robot R: Z.robots) if(R.castle_talk == PILGRIM) {
            int ind = -1;
            for(int i = 0; i < Z.fuelcount; i++) {
                if(Z.fuelToPil[i] == R.id) ind = i;
            }
            if(ind != -1) Z.isOccupiedFuel[ind] = true;
        }
        
        // for robots that are not yet assigned
        for (Robot R: Z.robots) if(R.castle_talk == PILGRIM) {
			int ind = -1;
            for(int i = 0; i < Z.karbcount; i++) {
                if(Z.karbToPil[i] == R.id) ind = i;
            }
            for(int i = 0; i < Z.fuelcount; i++) {
                if(Z.fuelToPil[i] == R.id) ind = i;
            }
            if(ind != -1) continue; // already been assigned
            
            int d = Z.euclidDist(R);
			if(d <= 2) { // make sure this is the castle that spawned it
				if(Math.random() < 0.5) { // make half of them target karbonite
					for (int i = 0; i < Z.karbcount; i++) {
						if (!Z.isOccupiedKarb[Z.sortedKarb[i]]) {
							Z.log("assigned " + R.id + " to " + Z.fdiv(Z.karbPos[Z.sortedKarb[i]],64) + "," + Z.karbPos[Z.sortedKarb[i]]%64 + " ("+i+")");
							Z.signal(getMessage(Z.karbPos[Z.sortedKarb[i]]), d);
							Z.karbToPil[Z.sortedKarb[i]] = R.id;
							Z.isOccupiedKarb[Z.sortedKarb[i]] = true;
							break;
						}
					}
				}
			}
        }
        for (Robot R: Z.robots) if(R.castle_talk == PILGRIM) {
			int ind = -1;
            for(int i = 0; i < Z.karbcount; i++) {
                if(Z.karbToPil[i] == R.id) ind = i;
            }
            for(int i = 0; i < Z.fuelcount; i++) {
                if(Z.fuelToPil[i] == R.id) ind = i;
            }
            if(ind != -1) continue; // already been assigned
            
            int d = Z.euclidDist(R);
			if(d <= 2) { // make sure this is the castle that spawned it
				for (int i = 0; i < Z.fuelcount; i++) {
					if (!Z.isOccupiedFuel[Z.sortedFuel[i]]) {
						Z.log("assigned " + R.id + " to " + Z.fdiv(Z.fuelPos[Z.sortedFuel[i]],64) + "," + Z.fuelPos[Z.sortedFuel[i]]%64 + " ("+i+")");
						Z.signal(getMessage(Z.fuelPos[Z.sortedFuel[i]]), d);
						Z.fuelToPil[Z.sortedFuel[i]] = R.id;
						Z.isOccupiedFuel[Z.sortedFuel[i]] = true;
						break;
					}
				}
			}
        }
    }
    int getMessage(int pos) {
        return 11+16*pos;
    }
    Action run() {
        if(Z.me.turn == 1) initVars();
        updateVars();
        if ( 2* Z.numPilgrims <= 10*  Z.numAttack) {
            Action A = makePilgrim();
            Z.numPilgrims++;
            if (A != null) return A;
        } else if(Z.turn <= 20) {
            if (Z.canBuild(PREACHER)) {
                Action A = Z.tryBuild(PREACHER);
                if (A != null) {
                    Z.numAttack ++;
                    Z.log("Built preacher");
                    return A;
                }
            }
            if (Z.canBuild(CRUSADER)) {
                Action A = Z.tryBuild(CRUSADER);
                if (A != null) {
                    Z.numAttack ++;
                    Z.log("Built crusader");
                    return A;
                }
            }
        } else {
            boolean canTake = Z.fuel >= CONSTRUCTION_F[CHURCH] + CONSTRUCTION_F[PROPHET] && Z.karbonite >= CONSTRUCTION_K[CHURCH] + CONSTRUCTION_K[PROPHET];
            if((Z.numAttack < 6 && Z.canBuild(PROPHET)) || canTake) {
                Action A = Z.tryBuild(PROPHET);
                if (A != null) {
                    Z.numAttack++;
                    Z.log("Built prophet");
                    return A;
                }
            }
        }/*else {
            if (Z.canBuild(CRUSADER)) {
                Action A = Z.tryBuild(CRUSADER);
                if (A != null) {
                    Z.numAttack ++;
                    Z.log("Built crusader");
                    return A;
                }
            }
        }*/
        return null;
    }
}
