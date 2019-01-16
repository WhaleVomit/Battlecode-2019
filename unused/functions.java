boolean inDanger() {
    for(int dx = -10; dx <= 10; dx++) for(int dy = -10; dy <= 10; dy++) {
        int x = CUR.x + dx, y = CUR.y + dy;
        if (enemyAttacker(x,y)) {
            Robot2 R = Z.robotMap[y][x];
            int dis = dangerRadius(R);
            boolean hasSupport = false;
            for(int dx2 = -10; dx2 <= 10; dx2++) for(int dy2 = -10; dy2 <= 10; dy2++) {
                if (dx2*dx2 + dy2*dy2 < dangerous) {
                    int x2 = R.x + dx2;
                    int y2 = R.y + dy2;
                    if(Z.yourAttacker(x2,y2)) hasSupport = true;
                }
            }
            if (dis <= dangerous && !hasSupport) return true;
        }
    }
    return false;
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

    /*boolean canSee(Robot2 A, Robot2 B) {
        int dist = (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
    }*/
    /*boolean shouldProphet() {
        boolean canTake = Z.fuel >= CONSTRUCTION_F[CHURCH] + CONSTRUCTION_F[PROPHET] && Z.karbonite >= CONSTRUCTION_K[CHURCH] + CONSTRUCTION_K[PROPHET];
        return Z.numAttack < Math.max(6, Z.fdiv(Z.me.turn,10)) || canTake;
    }*/
    /*Action2 testPreacherDefense() {
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
    }*/
