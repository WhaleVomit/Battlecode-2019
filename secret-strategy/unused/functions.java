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


boolean shouldRush() { return Z.CUR.turn <= 20; }

Action2 rushBuild() {
  if (shouldPilgrim() && Z.U.totUnits[PILGRIM] < 2) return makePilgrim();
  Robot2 R = Z.closestAttacker(Z.CUR,1-Z.CUR.team);
  if (Z.CUR.id != Z.min(Z.myCastleID)) return null;
  return Z.tryBuild(PROPHET);
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

PATROL:

// don't patrol next to robots at the front
/*for(int dx = -3; dx <= 3; dx++) {
  for(int dy = -3; dy <= 3; dy++) {
    int newx = X + dx; int newy = Y + dy;
    if(Z.yourAttacker(newx, newy)) {
      if(Z.robotMap[newy][newx].signal == 42424) return MOD;
    }
  }
}
if(Z.inVisionRange(X,Y)) {
  int cnt = 0;
  for(int dx = -1; dx <= 1; dx++) {
    for(int dy = -1; dy <= 1; dy++) {
      int newx = Z.CUR.x + dx; int newy = Z.CUR.y + dy;
      if(!((newx+newy)%2 == 0)) continue;
      if(Z.yourAttacker(newx, newy) && !(Z.robotMapID[newy][newx] == Z.CUR.id)) cnt++;
    }
  }
  if(cnt == 0) return MOD;
}*/
/*if(Z.atFront > 0) {
int distNeeded = 0;
for(int dx = -4; dx <= 4; dx++) {
  for(int dy = -4; dy <= 4; dy++) {
    int X = Z.CUR.x + dx; int Y = Z.CUR.y + dy;
    if(Z.yourAttacker(X,Y) && (X+Y)%2 == 1) {
      distNeeded = Math.max(distNeeded, dx*dx + dy*dy);
    }
  }
}
if(distNeeded > 0) {
  if(Z.nextSignal == null) {
    Z.nextSignal = new pi(42424, distNeeded);
  }
}
}

if (Z.endPos == 64*Z.CUR.x+Z.CUR.y) return Z.moveAction(0,0);*/
