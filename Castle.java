package bc19;

import static bc19.Consts.*;
import java.util.*;

public class Castle extends Building {
    public Castle(MyRobot z) { super(z); }

    /*boolean canSee(Robot2 A, Robot2 B) {
        int dist = (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
    }*/

    int getSignal(Robot2 R) {
        return 1001+21*(R.x-Z.me.x+10)+(R.y-Z.me.y+10);
    }

    boolean clearVision(Robot2 R) {
        if (Z.fdiv(R.castle_talk,6) % 6 == 1) return false;
        for (int i = -10; i <= 10; ++i)
            for (int j = -10; j <= 10; ++j) {
                if (i*i+j*j > VISION_R[R.unit]) continue;
                int x = R.x+i, y = R.y+j;
                if (Z.containsRobot(x,y) && Z.robotMap[y][x] > 0 && Z.seenRobot[y][x].team != Z.me.team) return false;
            }
        return true;
    }

    boolean warnDefenders() {
        Robot2 R = Z.closestEnemy(); if (R == null) return false;
        int needDist = 0;
        for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) if (i*i+j*j <= 16) {
            int x = Z.me.x+i, y = Z.me.y+j;
            if (!Z.containsRobot(x,y)) continue;
            Robot2 A = Z.seenRobot[y][x];
            if (A.unit > 2 && A.team == Z.me.team && clearVision(A)) needDist = Math.max(needDist,i*i+j*j);
        }
        if (needDist > 0) {
            Z.log("SIGNAL ENEMY: "+Z.me.turn+" "+Z.me.x+" "+Z.me.y+" "+R.x+" "+R.y+" "+getSignal(R));
            Z.signal(getSignal(R),needDist);
            return true;
        }
        return false;
    }

    void determineLoc() {
        if (Z.me.turn > 1) return;
        
        for (Robot2 R: Z.robots) if (R.castle_talk > 0 && R.castle_talk <= 64)
            Z.castleX.put(R.id,R.castle_talk-1);

        for (Robot2 R: Z.robots) if (R.castle_talk > 64 && R.castle_talk <= 128) {
            if (Z.castleY.get(R.id) == null) {
                Z.castleY.put(R.id,R.castle_talk-65);
                int t = 64*Z.castleX.get(R.id)+Z.castleY.get(R.id);
                if (!Z.myCastle.contains(t)) {
                    Z.myCastle.add(t);
                    if (Z.wsim()) Z.otherCastle.add(64*(Z.w-1-Z.castleX.get(R.id))+Z.castleY.get(R.id));
                    if (Z.hsim()) Z.otherCastle.add(64*Z.castleX.get(R.id)+(Z.h-1-Z.castleY.get(R.id)));
                }
            }
        }

        if (Z.me.turn == 1) Z.castleTalk(Z.me.x+1);
        else if (Z.me.turn == 2) Z.castleTalk(64+Z.me.y+1);
        else if (Z.me.turn == 3) Z.castleTalk(0);
    }

    boolean shouldMakePilgrim() {
        return 2 * Z.numPilgrims <= Z.numAttack;
    }
    
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
    boolean ourSide(int pos) {
		int x = Z.fdiv(pos,64); int y = pos%64;
		if(Z.hsim()) {
			int mid = Z.fdiv(Z.h,2);
			if(Z.me.y >= mid) {
				return y >= mid;
			} else {
				if(Z.h%2 == 1) return y <= mid;
				else return y < mid;
			}
		} else {
			int mid = Z.fdiv(Z.w,2);
			if(Z.me.x >= mid) {
				return x >= mid;
			} else {
				if(Z.w%2 == 1) return x <= mid;
				else return x < mid;
			}
		}
	}
    boolean better(int pos1, int pos2) {
		boolean b1 = ourSide(pos1);
		boolean b2 = ourSide(pos2);
		if(b1 && !b2) return true;
		else if(!b1 && b2) return false;
		return Z.bfsDist(pos1) < Z.bfsDist(pos2);
	}
    void sortKarb(){
        for (int i = 0; i < Z.karbcount-1; i++) {
            for (int j = 0; j < Z.karbcount - i - 1; j++) {
                if (!better(Z.karbPos[Z.sortedKarb[j]],Z.karbPos[Z.sortedKarb[j+1]])) {
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
                if (!better(Z.fuelPos[Z.sortedFuel[j]],Z.fuelPos[Z.sortedFuel[j+1]])) {
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
        sortFuel();
    }

    Action testPreacherDefense() {
        if (shouldMakePilgrim()) {
            Action A = makePilgrim();
            if (A != null) return A;
        } else {
            if (Z.me.team == 0) {
                if (Z.canBuild(PREACHER)) {
                    Action A = Z.tryBuild(PREACHER);
                    if (A != null) {
                        Z.numAttack ++;
                        Z.log("Built preacher");
                        return A;
                    }
                }
            } else {
                if (Z.canBuild(PROPHET)) {
                    Action A = Z.tryBuild(PROPHET);
                    if (A != null) {
                        Z.numAttack++;
                        Z.log("Built prophet");
                        return A;
                    }
                }
            }
        }
        return null;
    }

    void assignRand(Robot2 R, int d) {
		// assign to random if all positions have been filled
		int tot = Z.karbcount+Z.fuelcount;
		int i = (int)(Math.random()*tot);
		if(i < Z.karbcount) {
			Z.log("assigned " + R.id + " to " + Z.fdiv(Z.karbPos[Z.sortedKarb[i]],64) + "," + Z.karbPos[Z.sortedKarb[i]]%64 + " ("+i+")");
			Z.signal(getMessage(Z.karbPos[Z.sortedKarb[i]]), d);
			Z.karbToPil[Z.sortedKarb[i]] = R.id;
			Z.isOccupiedKarb[Z.sortedKarb[i]] = true;
		} else {
			i -= Z.karbcount;
			Z.log("assigned " + R.id + " to " + Z.fdiv(Z.fuelPos[Z.sortedFuel[i]],64) + "," + Z.fuelPos[Z.sortedFuel[i]]%64 + " ("+i+")");
			Z.signal(getMessage(Z.fuelPos[Z.sortedFuel[i]]), d);
			Z.fuelToPil[Z.sortedFuel[i]] = R.id;
			Z.isOccupiedFuel[Z.sortedFuel[i]] = true;
		}
	}
    
    void updateVars() {
        for(int i = 0; i < Z.karbcount; i++) {
            Z.isOccupiedKarb[i] = false;
        }
        for(int i = 0; i < Z.fuelcount; i++) {
            Z.isOccupiedFuel[i] = false;
        }
        // find current assignments
        for (Robot2 R: Z.robots) if(R.castle_talk == 25) {
            int ind = -1;
            for(int i = 0; i < Z.karbcount; i++) {
                if(Z.karbToPil[i] == R.id) ind = i;
            }
            Z.isOccupiedKarb[ind] = true;
        }
        for (Robot2 R: Z.robots) if(R.castle_talk == 25) {
            int ind = -1;
            for(int i = 0; i < Z.fuelcount; i++) {
                if(Z.fuelToPil[i] == R.id) ind = i;
            }
            Z.isOccupiedFuel[ind] = true;
        }
        
        if(Z.numPilgrims%2 == 0) { // assign new pilgrims to karbonite
			for (Robot2 R: Z.robots) if(R.castle_talk == 24) {            
				int d = Z.euclidDist(R);
				if(d <= 2) { // make sure this is the castle that spawned it
					boolean assigned = false;
					for (int i = 0; i < Z.karbcount; i++) {
						if (!Z.isOccupiedKarb[Z.sortedKarb[i]]) {
							Z.log("assigned " + R.id + " to " + Z.fdiv(Z.karbPos[Z.sortedKarb[i]],64) + "," + Z.karbPos[Z.sortedKarb[i]]%64 + " ("+i+")");
							Z.signal(getMessage(Z.karbPos[Z.sortedKarb[i]]), d);
							Z.karbToPil[Z.sortedKarb[i]] = R.id;
							Z.isOccupiedKarb[Z.sortedKarb[i]] = true;
							assigned = true;
							break;
						}
					}
					if(!assigned) assignRand(R, d);
				}
			}
		} else { // assign new pilgrims to fuel
			for (Robot2 R: Z.robots) if(R.castle_talk == 24) {
				int d = Z.euclidDist(R);
				if(d <= 2) { // make sure this is the castle that spawned it
					boolean assigned = false;
					for (int i = 0; i < Z.fuelcount; i++) {
						if (!Z.isOccupiedFuel[Z.sortedFuel[i]]) {
							Z.log("assigned " + R.id + " to " + Z.fdiv(Z.fuelPos[Z.sortedFuel[i]],64) + "," + Z.fuelPos[Z.sortedFuel[i]]%64 + " ("+i+")");
							Z.signal(getMessage(Z.fuelPos[Z.sortedFuel[i]]), d);
							Z.fuelToPil[Z.sortedFuel[i]] = R.id;
							Z.isOccupiedFuel[Z.sortedFuel[i]] = true;
							assigned = true;
							break;
						}
					}
					if(!assigned) assignRand(R, d);
				}
			}
		}
    }

    int getMessage(int pos) {
        return 2000 + pos;
    }
    
    Action build() {
        if (shouldMakePilgrim()) {
            Action A = makePilgrim();
            if (A != null) return A;
        } else if (Z.turn <= 20) {
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
        }
        return null;
    }
    
    Action run() {
		if(Z.me.turn == 1) initVars();
        determineLoc();
        if(!warnDefenders()) updateVars();
        if(Z.me.turn > 1) return build(); // first turn reserved to determine location of other castles
        return null;
	}
}
