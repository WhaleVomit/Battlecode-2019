package bc19;

import static bc19.Consts.*;
import java.util.*;

public class Castle extends Building {
    public Castle(MyRobot z) { super(z); }

    /*boolean canSee(Robot2 A, Robot2 B) {
        int dist = (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
    }*/

    void determineLoc() {
        if (Z.me.turn > 3) return;
        if (Z.me.turn == 1) {
            for (Robot2 R: Z.robots) if (R.team == Z.me.team) Z.castle.add(R.id);
        }

        for (Robot2 R: Z.robots) if (Z.castle.contains(R.id)) {
            if (R.castle_talk > 0 && R.castle_talk <= 64) Z.castleX.put(R.id,R.castle_talk-1);
            if (R.castle_talk > 64 && R.castle_talk <= 128) {
                Z.castleY.put(R.id,R.castle_talk-65);
                Z.addStruct(Z.makeRobot(0,Z.ME.team,Z.castleX.get(R.id),Z.castleY.get(R.id)));
            }
        }

        if (Z.ME.turn == 1) Z.castleTalk(Z.ME.x+1);
        else if (Z.ME.turn == 2) Z.castleTalk(64+Z.ME.y+1);
        else if (Z.ME.turn == 3) Z.castleTalk(0);
    }


    boolean ourSide(int pos) {
		int x = Z.fdiv(pos,64); int y = pos%64;
		if(Z.hsim()) {
			int mid = Z.fdiv(Z.h,2);
			if(Z.me.y >= mid) {
				return y >= mid;
			} else {
				if(Z.h % 2 == 1) return y <= mid;
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
        for (int x = 0; x < Z.w; x++) for(int y = 0; y < Z.h; y++){
            if (Z.karboniteMap[y][x]) Z.karbcount++;
            if (Z.fuelMap[y][x]) Z.fuelcount++;
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

        for(int x = 0; x < Z.w; x++) for (int y = 0; y < Z.h; y++) {
            if (Z.karboniteMap[y][x]){
                Z.sortedKarb[Z.karbcount] = Z.karbcount;
                Z.karbPos[Z.karbcount] = 64*x + y;
                Z.karbcount ++;
            }
            if (Z.fuelMap[y][x]){
                Z.sortedFuel[Z.fuelcount] = Z.fuelcount;
                Z.fuelPos[Z.fuelcount] = 64*x + y;
                Z.fuelcount ++;
            }
        }

        sortKarb(); sortFuel();
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

    int getMessage(int x) { return x+2000; }

    void updateVars() {
        for (int i = 0; i < Z.karbcount; i++) Z.isOccupiedKarb[i] = false;
        for (int i = 0; i < Z.fuelcount; i++) Z.isOccupiedFuel[i] = false;

        // find current assignments
        for (Robot2 R: Z.robots) if (!Z.castle.contains(R.id) && R.castle_talk % 7 == 6) {
            int ind = -1;
            for(int i = 0; i < Z.karbcount; i++) if(Z.karbToPil[i] == R.id) ind = i;
            Z.isOccupiedKarb[ind] = true;
            ind = -1;
            for (int i = 0; i < Z.fuelcount; i++) if (Z.fuelToPil[i] == R.id) ind = i;
            Z.isOccupiedFuel[ind] = true;
        }

        for (Robot2 R: Z.robots) if (!Z.castle.contains(R.id) && R.castle_talk % 7 == PILGRIM) {
            int d = Z.euclidDist(R);
            if (d <= 2) { // make sure this is the castle that spawned it
                boolean assigned = false;
                if (Z.ME.turn <= 10 || Z.karbcount <= Z.fuelcount)  {
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
                } else {
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
                }
                if (!assigned) assignRand(R, d);
            }
        }
    }

    boolean shouldPilgrim() {
        if (3*Z.numUnits[PILGRIM] > 2*(Z.karbcount+Z.fuelcount)) return false;
        if (Z.euclidDist(Z.closestAttacker(1-Z.ME.team)) <= 64) return false;
        return 2*Z.numUnits[PILGRIM] <= Z.numAttack;
    }
    boolean shouldRush() { return Z.ME.turn <= 20; }
    boolean shouldProphet() {
        boolean canTake = Z.fuel >= CONSTRUCTION_F[CHURCH] + CONSTRUCTION_F[PROPHET] && Z.karbonite >= CONSTRUCTION_K[CHURCH] + CONSTRUCTION_K[PROPHET];
        return Z.numAttack < Math.max(6, Z.fdiv(Z.me.turn,10)) || canTake;
    }
    Action build() {
        if (shouldPilgrim()) {
            return Z.tryBuild(PILGRIM);
        } else if (shouldRush()) {
            Action A = Z.tryBuild(PREACHER); if (A != null) return A;
            return Z.tryBuild(CRUSADER);
        } else if (shouldProphet()) {
            return Z.tryBuild(PROPHET);
        }
        return null;
    }
    Action testPreacherDefense() {
        if (shouldPilgrim()) {
            return Z.tryBuild(PILGRIM);
        } else if (Z.ME.team == 0) {
            Action A = Z.tryBuild(PREACHER); if (A != null) return A;
            return Z.tryBuild(CRUSADER);
        } else if (shouldProphet()) {
            return Z.tryBuild(PROPHET);
        }
        return null;
    }
    Action testRanger() {
        if (shouldPilgrim()) {
            return Z.tryBuild(PILGRIM);
        } else if (shouldProphet()) {
            return Z.tryBuild(PROPHET);
        }
        return null;
    }
    Action testCrusader() {
        if (shouldPilgrim()) {
            return Z.tryBuild(PILGRIM);
        } else return Z.tryBuild(CRUSADER);
    }
    Action testPreacher() {
        if (shouldPilgrim()) {
            return Z.tryBuild(PILGRIM);
        } else return Z.tryBuild(PREACHER);
    }

    Action run() {
		if (Z.me.turn == 1) initVars();
        determineLoc();

        Z.numAttack = 0; for (int i = 0; i < 6; ++i) Z.numUnits[i] = 0;
        for (Robot2 R: Z.robots) if (R.team == Z.ME.team) {
            if (Z.castle.contains(R.id)) Z.numUnits[0] ++;
            else {
                int t = R.castle_talk % 7; if (t == 6) t = 2;
                Z.numUnits[t] ++; if (t >= 3) Z.numAttack ++;
            }
        }

        /*if (Z.ME.turn <= 5) {
            String T = "";
            for (int i: Z.myCastle) T += Z.coordinates(i)+" ";
            T += "| " + Z.numAttack+" | ";
            for (int i = 0; i < 6; ++i) T += Z.numUnits[i]+" ";
            Z.log(Z.ME.turn+" "+T);
        }*/

        if (!Z.signaled) updateVars();
        if (Z.me.turn > 1) { // first turn reserved to determine location of other castles
            // return build();
            if (Z.me.team == 0) return testCrusader();
            else return testPreacher();
        }
            // return testRanger();
        return null;
	}
}
