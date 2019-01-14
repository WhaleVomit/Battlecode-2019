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
                Z.addStruct(Z.makeRobot(0,Z.CUR.team,Z.castleX.get(R.id),Z.castleY.get(R.id)));
            }
        }

        if (Z.CUR.turn == 1) Z.castleTalk(Z.CUR.x+1);
        else if (Z.CUR.turn == 2) Z.castleTalk(64+Z.CUR.y+1);
        else if (Z.CUR.turn == 3) Z.castleTalk(0);
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


    int getMessage(int x) { return x+2000; }

    void assignFuel(int i) {
        Z.log("assigned smth to fuel at " + Z.coordinates(Z.fuelPos[Z.sortedFuel[i]]));
        Z.signal(getMessage(Z.fuelPos[Z.sortedFuel[i]]), 2);
        Z.signaled = true;
        Z.assignedPilgrimPos = new pi(1,i);
    }

    void assignKarb(int i) {
        Z.log("assigned smth to karbonite at " + Z.coordinates(Z.karbPos[Z.sortedKarb[i]]));
        Z.signal(getMessage(Z.karbPos[Z.sortedKarb[i]]), 2);
        Z.signaled = true;
        Z.assignedPilgrimPos = new pi(0,i);
    }

    void assignRand() { // assign to random if all positions have been filled
        int tot = Z.karbcount+Z.fuelcount;
        int i = (int)(Math.random()*tot);
        if(i < Z.karbcount) assignKarb(i);
        else assignFuel(i-Z.karbcount);
    }

    boolean tryAssignKarb() {
        for (int i = 0; i < Z.karbcount; i++) {
            if (!Z.isOccupiedKarb[Z.sortedKarb[i]]) {
                assignKarb(i);
                return true;
            }
        }
        return false;
    }

    boolean tryAssignFuel() {
        for (int i = 0; i < Z.fuelcount; i++) {
            if (!Z.isOccupiedFuel[Z.sortedFuel[i]]) {
                assignFuel(i);
                return true;
            }
        }
        return false;
    }

    Action2 makePilgrim() {
        int a = Z.karbonite, b = Z.fuel-100*Z.movableUnits(); 
        boolean assigned = false;
        if (Z.CUR.turn <= 30 || 2*Z.numKarb <= Z.numFuel) assigned = tryAssignKarb();
        else if (2*Z.numFuel <= Z.numKarb) assigned = tryAssignFuel();
        else if (a < b) assigned = tryAssignKarb();
        else assigned = tryAssignFuel();
        if (!assigned) assignRand();
        return Z.tryBuild(PILGRIM);
    }

    Robot2 newPilgrim() {
        // closest pilgrim with signal % 7 == 6
        // within distance 4
        int bestDist = MOD; Robot2 P = null;
        for (int dx = -3; dx <= 3; ++dx) for (int dy = -3; dy <= 3; ++dy) {
            int d = dx*dx+dy*dy; if (d > bestDist) continue;
            int x = Z.me.x+dx, y = Z.me.y+dy;
            if (Z.yourRobot(x,y)) {
                Robot2 R = Z.robotMap[y][x];
                if (R.unit == 2 && R.castle_talk % 7 == 6) {
                    bestDist = d;
                    P = R;
                }
            }
        }
        return P;
    }

    void updatePilgrimID() {
        if (Z.assignedPilgrimPos.f == -1) return;
        Robot2 R = newPilgrim();
        if (R == null) {
            Z.log("NO PILGRIM?");
            return;
        }
        if (Z.assignedPilgrimPos.f == 0) {
            Z.karbToPil[Z.sortedKarb[Z.assignedPilgrimPos.s]] = R.id;
            Z.log(R.id+" IS KARB PILGRIM");
        } else {
            Z.fuelToPil[Z.sortedFuel[Z.assignedPilgrimPos.s]] = R.id;
            Z.log(R.id+" IS FUEL PILGRIM");
        }
        Z.assignedPilgrimPos = new pi(-1,-1);
    }

    boolean shouldPilgrim() {
        if (Z.signaled || !Z.canBuild(PILGRIM)) return false;
        if (3*Z.numUnits[PILGRIM] > 2*(Z.karbcount+Z.fuelcount)) return false;
        if (Z.euclidDist(Z.CUR,Z.closestAttacker(Z.CUR,1-Z.CUR.team)) <= 64) return false;
        return 2*Z.closeUnits[PILGRIM] <= Z.closeUnits[3]+Z.closeUnits[4]+Z.closeUnits[5];
    }
    boolean shouldRush() { return Z.CUR.turn <= 20; }
    boolean shouldProphet() {
        boolean canTake = Z.fuel >= CONSTRUCTION_F[CHURCH] + CONSTRUCTION_F[PROPHET] && Z.karbonite >= CONSTRUCTION_K[CHURCH] + CONSTRUCTION_K[PROPHET];
        return Z.numAttack < Math.max(6, Z.fdiv(Z.me.turn,10)) || canTake;
    }
    Action2 build() {
        if (Z.movableUnits() >= 30 && Z.numUnits[2] >= 10 && Z.fuel < 100*Z.movableUnits()) return null;
        if (shouldPilgrim()) return makePilgrim();
        if (shouldRush()) {
            Action2 A = Z.tryBuild(PREACHER); if (A != null) return A;
            return Z.tryBuild(CRUSADER);
        } 
        double a = Z.closeUnits[3], b = Z.closeUnits[4]/2.0, c = Z.closeUnits[5];
        if (b <= Math.min(a,c)) return Z.tryBuild(PROPHET);
        if (a <= Math.min(b,c)) return Z.tryBuild(CRUSADER);
        return Z.tryBuild(PREACHER);
    }
    Action2 testPreacherDefense() {
        if (shouldPilgrim()) return makePilgrim();
        if (Z.CUR.team == 0) {
            Action2 A = Z.tryBuild(PREACHER); if (A != null) return A;
            return Z.tryBuild(CRUSADER);
        } else if (shouldProphet()) {
            return Z.tryBuild(PROPHET);
        }
        return null;
    }
    Action2 testProphet() {
        if (shouldPilgrim()) return makePilgrim();
        if (shouldProphet()) return Z.tryBuild(PROPHET);
        return null;
    }
    Action2 testCrusader() {
        if (shouldPilgrim()) return makePilgrim();
        return Z.tryBuild(CRUSADER);
    }
    Action2 testPreacher() {
        if (shouldPilgrim()) return makePilgrim();
        return Z.tryBuild(PREACHER);
    }

    void dumpTroopInfo() {
        String T = "";
        for (int i: Z.myCastle) T += Z.coordinates(i)+" ";
        T += "| " + Z.numAttack+" | ";
        for (int i = 0; i < 6; ++i) T += Z.numUnits[i]+" ";
        Z.log(Z.CUR.turn+" "+T);
    }

    Action2 run() {
		if (Z.me.turn == 1) initVars();
        determineLoc();
        updatePilgrimID();

        for (int i = 0; i < Z.karbcount; i++) Z.isOccupiedKarb[i] = false;
        for (int i = 0; i < Z.fuelcount; i++) Z.isOccupiedFuel[i] = false;

        // find current assignments
        for (Robot2 R: Z.robots) if (!Z.castle.contains(R.id) && R.castle_talk % 7 == 2) {
            int ind = -1;
            for (int i = 0; i < Z.karbcount; i++) if(Z.karbToPil[i] == R.id) ind = i;
            Z.isOccupiedKarb[ind] = true;
            ind = -1;
            for (int i = 0; i < Z.fuelcount; i++) if (Z.fuelToPil[i] == R.id) ind = i;
            Z.isOccupiedFuel[ind] = true;
        }

        Z.numKarb = 0; Z.numFuel = 0;
        for (int i = 0; i < Z.karbcount; i++) if (Z.isOccupiedKarb[i]) Z.numKarb ++;
        for (int i = 0; i < Z.fuelcount; i++) if (Z.isOccupiedFuel[i]) Z.numFuel ++;

        Z.numAttack = 0; 
        for (int i = 0; i < 6; ++i) {
            Z.numUnits[i] = 0;
            Z.closeUnits[i] = 0;
        }
        for (Robot2 R: Z.robots) if (R.team == Z.CUR.team) {
            if (Z.castle.contains(R.id)) {
                Z.numUnits[0] ++;
                if (Z.euclidDist(Z.CUR,R) <= 100) Z.closeUnits[0] ++;
            } else {
                int t = R.castle_talk % 7; if (t == 6) t = 2;
                Z.numUnits[t] ++; if (t >= 3) Z.numAttack ++;
                if (Z.euclidDist(Z.CUR,R) <= 100) Z.closeUnits[t] ++;
            }
        }
        Z.closeUnits[2] = Math.max(Z.closeUnits[2],Z.numKarb+Z.numFuel);

        if (Z.me.turn > 1) { // first turn reserved to determine location of other castles
            return build();
            //if (Z.me.team == 0) return testCrusader();
            //else return testPreacher();
        }
            // return testRanger();
        return null;
	}
}
