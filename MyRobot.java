package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class MyRobot extends BCAbstractRobot {
    // int turn = 0; turns since start of game
    Robot2 ORI, CUR; 

    // MAP (arrays are by y and then x)
    int w, h; // width, height
    Robot2[] robots;
    Robot2[][] robotMap; // stores last robot seen in pos
    int[][] robotMapID; // stores last id seen in pos

    int[][] bfsDist, nextMove;
    boolean updEnemy = false;
    int[][][] enemyDist;
    ArrayList<Integer> myCastle = new ArrayList<>(), otherCastle = new ArrayList<>();
    ArrayList<Integer> myChurch = new ArrayList<>(), otherChurch = new ArrayList<>();
    pi[] pos = new pi[4097];

    // PERSONAL
    boolean goHome; // whether unit is going home or not
    boolean signaled;

    // FOR CASTLE
    int numAttack;
    int[] numUnits = new int[6];
    int[] sortedKarb, sortedFuel, karbToPil, fuelToPil, karbPos, fuelPos;
    boolean[] isOccupiedKarb , isOccupiedFuel;
    int karbcount=0, fuelcount=0;
    Set<Integer> castle = new HashSet<>();
    Map<Integer,Integer> castleX = new HashMap<>();
    Map<Integer,Integer> castleY = new HashMap<>();
    pi assignedPilgrimPos = new pi(-1,-1);

    // FOR PILGRIM
    int resource = -1; // karbonite or fuel
    pi resourceLoc = new pi(-1,-1);

    // MATH
    int fdiv(int a, int b) { return (a-(a%b))/b; }
    int sq(int x) { return x*x; }
    String coordinates(int t) { 
        int y = t%64, x = fdiv(t,64);
        return "("+x+", "+y+")";
    }
    
    // ACTION
    Action2 moveAction(int dx, int dy) {
        Action2 A = new Action2();
        A.type = 0; A.dx = dx; A.dy = dy;
        return A;
    }
    Action2 mineAction() {
        Action2 A = new Action2();
        A.type = 1; 
        return A;
    }
    Action2 giveAction(int dx, int dy, int karb, int fuel) {
        Action2 A = new Action2();
        A.type = 2; A.dx = dx; A.dy = dy; A.karb = karb; A.fuel = fuel;
        return A;
    }
    Action2 attackAction(int dx, int dy) {
        Action2 A = new Action2();
        A.type = 3; A.dx = dx; A.dy = dy;
        return A;
    }
    Action2 buildAction(int unit, int dx, int dy) {
        Action2 A = new Action2();
        A.type = 4; A.unit = unit; A.dx = dx; A.dy = dy;
        return A;
    }

    // ROBOT
    Robot2 makeRobot(int unit, int team, int x, int y) {
        Robot2 R = new Robot2(null);
        R.id = MOD; R.unit = unit; R.team = team; R.x = x; R.y = y;
        return R;
    } 

    // ARRAYLIST
    void removeDup(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (Integer i : A) if (!B.contains(i)) B.add(i);
        A.clear();
        for (Integer i : B) A.add(i);
    }

    // EUCLID DIST
    int euclidDist(int x1, int y1, int x2, int y2) { return sq(x1-x2) + sq(y1-y2); }
    int euclidDist(Robot2 A, int x, int y) { return A == null ? MOD : euclidDist(A.x,A.y,x,y); }
    int euclidDist(Robot2 A, Robot2 B) { 
        if (A == null || B == null) return MOD;
        return euclidDist(A.x,A.y,B.x,B.y); 
    }
    int euclidDist(Robot2 A) { return euclidDist(CUR,A); }
    boolean adjacent(Robot2 A, Robot2 B) { return euclidDist(A,B) <= 2; }
    boolean inVisionRange(Robot2 A, Robot2 B) { 
        if (A == null || A.unit == -1) return false;
        return euclidDist(A,B) <= VISION_R[A.unit]; 
    }

    // ROBOT
    Action conv(Action2 A) {
        if (A.type < 0) return null;
        if (A.type == 0) return move(A.dx,A.dy);
        if (A.type == 1) return mine();
        if (A.type == 2) return give(A.dx,A.dy,A.karb,A.fuel);
        if (A.type == 3) return attack(A.dx,A.dy);
        if (A.type == 4) return buildUnit(A.unit,A.dx,A.dy);
        return null;
    }
    Robot2 getRobot2(int id) { return new Robot2(getRobot(id)); }

    // DEBUG
    void dumpRobots() { String T = ""; for (Robot2 R: robots) T += R.getInfo(); log(T); }
    void dumpInfo() {
        String T = CUR.getInfo();
        T += myCastle.size()+" "+otherCastle.size();
        if (otherCastle.size() > 0) T += " " + coordinates(otherCastle.get(0));
        T += "\n";
        log(T);
    }

    // MAP
    boolean hsim() { // symmetric with respect to y
        for (int i = 0; i < h-1-i; ++i)
            for (int j = 0; j < w; ++j) {
                if (map[i][j] != map[h-1-i][j]) return false;
                if (karboniteMap[i][j] != karboniteMap[h-1-i][j]) return false;
                if (fuelMap[i][j] != fuelMap[h-1-i][j]) return false;
            }
        return true;
    }
    boolean wsim() { // symmetric with respect to x
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w-1-j; ++j) {
                if (map[i][j] != map[i][w-1-j]) return false;
                if (karboniteMap[i][j] != karboniteMap[i][w-1-j]) return false;
                if (fuelMap[i][j] != fuelMap[i][w-1-j]) return false;
            }
        return true;
    }
    boolean inMap(int x, int y) { return x >= 0 && x < w && y >= 0 && y < h; }
    boolean valid(int x, int y) { return inMap(x,y) && map[y][x]; }
    boolean passable(int x, int y) { return valid(x, y) && robotMapID[y][x] <= 0; }
    boolean containsRobot(int x, int y) { return valid(x, y) && robotMapID[y][x] > 0; }

    boolean teamRobot(int x, int y, int t) { return containsRobot(x,y) && robotMap[y][x].team == t; }
    boolean yourRobot(int x, int y) { return teamRobot(x,y,CUR.team); }
    boolean enemyRobot(int x, int y) { return teamRobot(x,y,1-CUR.team); }
    boolean enemyRobot(int x, int y, int t) { return teamRobot(x,y,1-CUR.team) && robotMap[y][x].unit == t; }

    boolean attacker(int x, int y) { return containsRobot(x,y) && robotMap[y][x].unit > 2; }
    boolean teamAttacker(int x, int y, int t) { return attacker(x,y) && teamRobot(x,y,t);  }
    boolean yourAttacker(int x, int y) { return teamAttacker(x,y,CUR.team); }
    boolean enemyAttacker(int x, int y) { return teamAttacker(x,y,1-CUR.team); }

    int numOpen(int t) { // how many squares around t are free
        int y = t % 64; int x = fdiv(t,64);
        int ret = 0;
        for (int i = x-1; i <= x+1; ++i) for (int j = y-1; j <= y+1; ++j)
            if (passable(i,j)) ret ++;
        return ret;
    }
    Robot2 closestEnemy(Robot2 R) {
        Robot2 bes = null;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
            if (teamRobot(j,i,1-R.team) && euclidDist(R,j,i) < euclidDist(R,bes)) bes = robotMap[i][j];
        return bes;
    }
    Robot2 closestAttacker(Robot2 R, int t) {
        Robot2 bes = null;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
            if (teamAttacker(j,i,t) && euclidDist(R,j,i) < euclidDist(R,bes))
                bes = robotMap[i][j];
        return bes;
    }

    // BFS DIST
    void genBfsDist(int mx) {
        if (bfsDist == null) { bfsDist = new int[h][w];  nextMove = new int[h][w]; }
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) {
            bfsDist[i][j] = MOD; nextMove[i][j] = MOD;
        }
        LinkedList<Integer> Q = new LinkedList<Integer>(); bfsDist[CUR.y][CUR.x] = 0; Q.add(64 * CUR.x + CUR.y);

        while (Q.size() > 0) {
            int x = Q.poll(); int y = x % 64; x = fdiv(x,64);
            for (int dx = -3; dx <= 3; ++dx) {
                int X = x+dx; if (X < 0 || X >= w) continue;
                for (int dy = -3; dy <= 3; ++dy) {
                    int Y = y+dy; if (Y < 0 || Y >= h) continue;
                    if (dx*dx+dy*dy <= mx && map[Y][X] && bfsDist[Y][X] == MOD) {
                        bfsDist[Y][X] = bfsDist[y][x] + 1;
                        nextMove[Y][X] = nextMove[y][x];
                        if (robotMapID[Y][X] <= 0)  {
                            if (nextMove[Y][X] == MOD) nextMove[Y][X] = 64 * X + Y;
                            Q.add(64 * X + Y);
                        }
                    }
                }
            }
        }
    }
    void genEnemyDist() {
        if (enemyDist == null) {
            enemyDist = new int[h][w][2];
            for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
                for (int k = 0; k < 2; ++k) enemyDist[i][j][k] = MOD;
        }
        if (!updEnemy) return;
        updEnemy = false;
        LinkedList<Integer> Q = new LinkedList<Integer>();
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
            for (int k = 0; k < 2; ++k) enemyDist[i][j][k] = MOD;

        for (int i: otherCastle) {
            Q.add(2*i);
            enemyDist[i%64][fdiv(i,64)][0] = 0;
        }
        for (int i: otherChurch) {
            Q.add(2*i);
            enemyDist[i%64][fdiv(i,64)][0] = 0;
        }

        while (Q.size() > 0) {
            int t = Q.poll();
            int k = t % 2; t = fdiv(t,2);
            int x = fdiv(t,64), y = t % 64;
            for (int z = 0; z < 4; ++z) {
                int X = x+xd[z], Y = y+yd[z];
                if (inMap(X,Y)) {
                    int K = k+1; if (map[Y][X]) K = 0;
                    if (K == 2 || enemyDist[Y][X][K] != MOD) continue;
                    enemyDist[Y][X][K] = enemyDist[y][x][k]+1;
                    Q.push(2*(64*X+Y)+K);
                }
            }
        }
    }
    int bfsDist(int x) { return x == MOD ? MOD : bfsDist[x % 64][fdiv(x,64)]; }
    int closest(ArrayList<Integer> A) {
        int bestDist = MOD, bestPos = MOD; if (A == null) return bestPos;
        for (int x : A) if (bfsDist(x) < bestDist) {
            bestDist = bfsDist(x); bestPos = x;
        }
        return bestPos;
    }
    int closestUnseen() {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
            if (passable(j, i) && bfsDist[i][j] < bestDist && robotMapID[i][j] == -1) {
                bestDist = bfsDist[i][j]; bestPos = 64*j+i;
            }
        return bestPos;
    }
    int closestUnused(boolean[][] B) {
        int bestDist = MOD, bestPos = MOD; if (B == null) return bestPos;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j)
            if (B[i][j] && bfsDist[i][j] < bestDist && robotMapID[i][j] <= 0) {
                bestDist = bfsDist[i][j]; bestPos = 64*j+i;
            }
        return bestPos;
    }
    int closeEmpty(int x, int y) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = -10; i <= 10; ++i) for (int j = -10; j <= 10; ++j) {
            int X = x+i, Y = y+j;
            if (passable(X,Y) && i*i+j*j < bestDist) {
                bestDist = i*i+j*j; bestPos = 64*X+Y;
            }
        }
        return bestPos;
    }
    int bfsDistHome() { return Math.min(bfsDist(closest(myCastle)),bfsDist(closest(myChurch))); }
    int closestChurch(boolean ourteam) { return closest(ourteam ? myChurch : otherChurch); }
    int closestCastle(boolean ourteam) { return closest(ourteam ? myCastle : otherCastle); }
    int closestStruct(boolean ourteam) {
        int bestCastle = closestCastle(ourteam);
        int bestChurch = closestChurch(ourteam);
        if (bfsDist(bestCastle) < bfsDist(bestChurch)) return bestCastle;
        return bestChurch;
    }

    public boolean canBuild(int t) {
        if (!(fuel >= CONSTRUCTION_F[t] && karbonite >= CONSTRUCTION_K[t])) return false;
        for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy)
            if (passable(CUR.x + dx, CUR.y + dy)) return true;
        return false;
    }
    public Action2 tryBuild(int t) {
        if (!canBuild(t)) return null;
        if(CAN_ATTACK[t]) {
            signal(encodeEnemyCastleLocations(), 2);
            signaled = true;
        }
        for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy)
            if (passable(CUR.x + dx, CUR.y + dy)) return buildAction(t, dx, dy);
        return null;
    }
    public boolean yesStruct(int x, int y) {
		if (!valid(x,y) || robotMapID[y][x] == 0) return false;
        if (robotMapID[y][x] > 0 && !robotMap[y][x].isStructure()) return false;
        return true;
    }
    public void addYour(ArrayList<Integer> A, Robot2 R) {
        int p = 64*R.x+R.y;
        if (!yesStruct(R.x,R.y) || A.contains(p)) return;
        A.add(p);
        if (robotMapID[R.y][R.x] == -1) { robotMapID[R.y][R.x] = R.id; robotMap[R.y][R.x] = R; }
    }
    public void addOther(ArrayList<Integer> A, Robot2 R) {
        int p = 64*R.x+R.y;
        if (!yesStruct(R.x,R.y) || A.contains(p)) return;
        A.add(p); updEnemy = true;
        if (robotMapID[R.y][R.x] == -1) { robotMapID[R.y][R.x] = R.id; robotMap[R.y][R.x] = R; }
    }
    void rem(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (int i : A) {
            int x = fdiv(i,64), y = i%64;
            if (!inMap(x,y)) {
                String res = "CCC "+x+" "+y+" "+i+ " | ";
                for (int j: A) res += j+" ";
                res += " | ";
                for (Integer j: A) res += j+" ";
                log(res);
                return;
            }
            if (yesStruct(x,y)) B.add(i);
        }
        A.clear();
        for (int i : B) A.add(i);
    }
    public void addStruct(Robot2 R) {
        if (!yesStruct(R.x,R.y)) return;
        if (R.unit == CHURCH) {
            if (R.team == CUR.team) addYour(myChurch,R);
            else if (R.team != CUR.team) addOther(otherChurch,R);
        } else {
            if (R.team == CUR.team) {
                addYour(myCastle,R);
                if (wsim()) addOther(otherCastle,makeRobot(0,1-CUR.team,w-1-R.x,R.y));
                if (hsim()) addOther(otherCastle,makeRobot(0,1-CUR.team,R.x,h-1-R.y));
            } else if(R.team != CUR.team) {
                addOther(otherCastle,R);
                if (wsim()) addYour(myCastle,makeRobot(0,CUR.team,w-1-R.x,R.y));
                if (hsim()) addYour(myCastle,makeRobot(0,CUR.team,R.x,h-1-R.y));
            }
        }
    }

    // CASTLE LOCATIONS
    int encodeEnemyCastleLocations() {
        int res = 0;
        int oppX = CUR.x, oppY = CUR.y;
        if (hsim()) oppY = h - 1 - oppY;
        else oppX = w - 1 - oppX;
        int approxOppID = 8 * fdiv(oppX, 8) + fdiv(oppY, 8);

        pi approxLocs = new pi(-1, -1);
        for(Integer i : otherCastle) {
            int xPos = fdiv(i, 64);
            int yPos = i % 64;

            yPos = fdiv(yPos, 8);
            xPos = fdiv(xPos, 8); // approximating location
            int approxID = xPos * 8 + yPos;
            if(approxID == approxOppID) {
                res |= (1 << 11);
                continue;
            }
            if(approxID > approxOppID) approxID--; // between 0 and 62 now

            if (approxLocs.f == -1) approxLocs.f = approxID;
            else if (approxLocs.f != approxID) approxLocs.s = approxID;
        }
        if(approxLocs.f != -1) {
            if(approxLocs.s == -1) approxLocs.s = approxLocs.f;
            res++; // 0 would be no other castles
            int l1 = Math.min(approxLocs.f, approxLocs.s);
            int l2 = Math.max(approxLocs.f, approxLocs.s);
            res += l1 * 63 + l2;
            res -= l1 * (l1 + 1) / 2;
        }
        res += 7000;
        // end result is between 7000 and 11100 (upper bound is actually a bit lower but just to be safe)
        return res;
    }

    void fill8by8(int approxX, int approxY) {
        for (int i = 0; i < 8; i++) for(int j = 0; j < 8; j++) 
            addStruct(makeRobot(0,1-CUR.team,8 * approxX + i,8 * approxY + j));
    }

    void fill8by8(int approxID) { fill8by8(fdiv(approxID, 8), approxID % 8); }

    void decodeEnemyCastleLocations(Robot2 parentCastle) {
        int oppX = parentCastle.x, oppY = parentCastle.y;
        if(hsim()) oppY = h - 1 - oppY;
        else oppX = w - 1 - oppX;
        oppX = fdiv(oppX, 8);
        oppY = fdiv(oppY, 8);
        int oppID = 8 * oppX + oppY;

        int sig = parentCastle.signal;
        sig -= 7000;
        if((sig >> 11) > 0) {
            fill8by8(oppID);
            sig -= (1 << 11);
        }
        if(sig > 0) {
            sig--;
            int p1 = 0, p2 = 0;
            while(sig > 0) {
                p2++;
                if(p2 == 63) {
                    p1++;
                    p2 = p1;
                }
                sig--;
            }
            if(p1 >= oppID) p1++;
            if(p2 >= oppID) p2++;
            fill8by8(p1);
            if(p2 != p1) fill8by8(p2);
        }
    }

    int getSignal(Robot2 R) {
        return 441*(R.unit-3)+21*(R.x-CUR.x+10)+(R.y-CUR.y+10)+1;
    }
    
    boolean clearVision(Robot2 R) {
        if (CUR.unit == CASTLE && fdiv(R.castle_talk,7) % 2 == 1) return false;
        for (int i = -10; i <= 10; ++i)
            for (int j = -10; j <= 10; ++j) {
                if (i*i+j*j > VISION_R[R.unit]) continue;
                if (enemyRobot(R.x+i,R.y+j)) return false;
            }
        for (Robot2 A: robots) if (A.team == CUR.team && 0 < A.signal && A.signal < 2000)
            if (euclidDist(R,A) <= A.signal_radius) return false;
        return true;
    }

    void checkSignal() {
        for (Robot2 R: robots) {
            if (R.team == CUR.team && 0 < R.signal && R.signal < 2000) {
                int tmp = R.signal-1;
                int type = fdiv(tmp,441)+3; tmp %= 441;
                int x = fdiv(tmp,21)-10; x += R.x;
                int y = (tmp%21)-10; y += R.y;
                robotMapID[y][x] = MOD; robotMap[y][x] = makeRobot(type,1-CUR.team,x,y);
                log("ADDED "+CUR.x+" "+CUR.y+" "+x+" "+y);
            } else if (R.team == CUR.team && R.unit == CASTLE && R.signal >= 7000 && R.signal < 11100 && adjacent(CUR,R)) {
                decodeEnemyCastleLocations(R);
            }
		}
    }

    boolean superseded(int x, int y) {
        for (int i = -6; i <= 6; ++i) for (int j = -6; j <= 6; ++j)
            if (yourRobot(x+i,y+j)) {
                if (i == 0 && j == 0) continue;
                Robot2 R = robotMap[y+j][x+i];
                if (Math.sqrt(VISION_R[me.unit])+Math.sqrt(i*i+j*j) <= Math.sqrt(VISION_R[R.unit])) return true;
            }
        return false;
    }

    void warnOthers() { // CUR.x, CUR.y are new pos, not necessarily equal to me.x, me.y;
        if (fuel < 100 || superseded(CUR.x,CUR.y)) return;
        Robot2 R = closestAttacker(ORI, 1-CUR.team); if (euclidDist(ORI,R) > VISION_R[CUR.unit]) return;
        int needDist = 0;
        for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) {
            int x = CUR.x+i, y = CUR.y+j;
            if (i*i+j*j <= 16 && yourAttacker(x,y) && clearVision(robotMap[y][x]))
                needDist = Math.max(needDist,i*i+j*j);
        }
        if (needDist > 0) {
            log("SIGNAL ENEMY: "+" MY POS ("+me.x+", "+me.y+") ENEMY POS ("+R.x+", "+R.y+") "+getSignal(R));
            signal(getSignal(R),needDist);
            signaled = true;
        }
    }

    void updateData() {
        h = map.length; w = map[0].length; 
        ORI = new Robot2(me); CUR = new Robot2(me);
        signaled = false;
        robots = new Robot2[getVisibleRobots().length];
        for (int i = 0; i < robots.length; ++i) robots[i] = new Robot2(getVisibleRobots()[i]);

        if (robotMap == null) {
            robotMap = new Robot2[h][w];
            robotMapID = new int[h][w];
            for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) robotMapID[i][j] = -1;
        }

        for (Robot2 R: robots) if (R.isStructure()) addStruct(R);

        for (int i = 1; i <= 4096; ++i) pos[i] = null;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) if (robotMapID[i][j] > 0 && robotMapID[i][j] < MOD)
            pos[robotMapID[i][j]] = new pi(j,i);

        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) {
            int t = getVisibleRobotMap()[i][j];
            if (t != -1) {
                robotMapID[i][j] = t;
                if (robotMapID[i][j] == 0) robotMap[i][j] = null;
                else {
                    Robot2 R = getRobot2(t);
                    if (pos[t] != null && robotMapID[pos[t].s][pos[t].f] == t) {
                        robotMapID[pos[t].s][pos[t].f] = -1;
                        robotMap[pos[t].s][pos[t].f] = null;
                        robotMapID[i][j] = t;
                    }
                    pos[t] = new pi(j,i); robotMap[i][j] = R;
                    if (R.unit <= 1) addStruct(R);
                }
            }
        }

        rem(myCastle); rem(otherCastle);
        rem(myChurch); rem(otherChurch);
        checkSignal();
    }

    void sendToCastle(int res) { // 0 to 5: unit, 6: assigned pilgrim
        boolean seeEnemy = false;
        for (int i = -14; i <= 14; ++i) for (int j = -14; j <= 14; ++j)
            if (i*i+j*j <= 196 && enemyRobot(CUR.x+i,CUR.y+j)) seeEnemy = true;
        if (seeEnemy) res += 7;
        if (otherCastle.size()+otherChurch.size() == 0) res += 14;
        castleTalk(res);
    }

    void sendToCastle() {
        int res = CUR.unit; if (res == 0) return;
        sendToCastle(res);
    }

    public Action turn() {
        updateData();
        if(CUR.unit == CASTLE && CUR.team == 0 && myCastle.get(0) == 64 * CUR.x + CUR.y) log("================ ROUND " + CUR.turn + " ================ "+me.time);
        genBfsDist(CUR.unit == CRUSADER ? 9 : 4);
        genEnemyDist();
        if (CUR.turn == 1) log("TYPE: "+CUR.unit);
        if (CUR.unit == CASTLE) warnOthers();

        Action2 A;
        switch (CUR.unit) {
            case CASTLE: {
                Castle C = new Castle(this);
                A = C.run();
                break;
            }
            case CHURCH: {
                Church C = new Church(this);
                A = C.run();
                break;
            }
            case PILGRIM: {
                Pilgrim C = new Pilgrim(this);
                A = C.run();
                break;
            }
            case CRUSADER: {
                Crusader C = new Crusader(this);
                A = C.run();
                break;
            }
            case PROPHET: {
                Prophet C = new Prophet(this);
                A = C.run();
                break;
            }
            case PREACHER: {
                Preacher C = new Preacher(this);
                A = C.run();
                break;
            }
        }
        if (A == null) A = new Action2();
        if (A.type == 0) {
            // log("MOVED "+CUR.x+" "+CUR.y+" "+A.dx+" "+A.dy);
            robotMap[CUR.y][CUR.x] = null; robotMapID[CUR.y][CUR.x] = 0;
            CUR.x += A.dx; CUR.y += A.dy;
            robotMap[CUR.y][CUR.x] = CUR; robotMapID[CUR.y][CUR.x] = CUR.id;
        }
        if (CUR.unit != CASTLE) warnOthers();
        return conv(A);
    }
}