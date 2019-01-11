package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class MyRobot extends BCAbstractRobot {
    // DATA
    int turn = 0; // turns since start of game
    int w, h; // width, height
    int myUnits = 0;
    int numCastles = 0, numPilgrims = 0, numAttack = 0, numChurches = 0, numCrusaders = 0;

    int resource = -1; // karbonite or fuel
    boolean goHome; // whether unit is going home or not

    // note that arrays are by y and then x
    Robot2[] robots;
    Robot2[][] seenRobot; // stores last robot seen in pos
    int[][] robotMap, seenMap; // stores last id seen in pos
    int[][] dist, pre; 

    boolean updEnemy = false;
    int[][][] enemyDist = null;
    boolean[][] emp; // whether square does not contain structure or not
    Queue Q = new Queue(8192);

    ArrayList<Integer> myCastle = new ArrayList<>(), otherCastle = new ArrayList<>();
    ArrayList<Integer> myChurch = new ArrayList<>(), otherChurch = new ArrayList<>();

    // MATH
    int fdiv(int a, int b) { return (a-(a%b))/b; }
    int sq(int x) { return x*x; }

    // ROBOT
    Robot2 makeRobot(int unit, int team, int x, int y) {
        Robot2 R = new Robot2(null);
        R.unit = unit; R.team = team; R.x = x; R.y = y;
        return R;
    }
    Robot2 getRobot2(int id) { return new Robot2(getRobot(id)); }
    boolean withinMoveRadius(Robot2 R, int dx, int dy) { return R != null && R.withinMoveRadius(dx,dy,fuel); }

    // ARRAYLIST

    void removeDup(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (Integer i : A) if (!B.contains(i)) B.add(i);
        A.clear();
        for (Integer i : B) A.add(i);
    }

    void rem(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (Integer i : A) {
            int y = i % 64; int x = (i-y)/64;
            if (!emp[y][x]) B.add(i);
        }
        A.clear();
        for (Integer i : B) A.add(i);
    }

    // MAP
    boolean hsim() { // symmetric with respect to y
        for (int i = 0; i < h - 1 - i; ++i)
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
    boolean containsRobot(int x, int y) { return valid(x, y) && seenMap[y][x] > 0; }
    boolean passable(int x, int y) { return valid(x, y) && seenMap[y][x] <= 0; }
    boolean adjacent(Robot2 r) { return Math.abs(me.x - r.x) <= 1 && Math.abs(me.y - r.y) <= 1; }
    int euclidDist(int x, int y) { return sq(me.x-x)+sq(me.y-y); }
    int euclidDist(Robot2 B) { return B == null ? MOD : euclidDist(B.x,B.y); }
    int numOpen(int t) { // how many squares around t are free
        int y = t % 64; int x = fdiv(t,64);
        int ret = 0;
        for (int i = x-1; i <= x+1; ++i)
            for (int j = y-1; j <= y+1; ++j)
                if (valid(i,j) && robotMap[j][i] == 0) ret ++;
        return ret;
    }


    // BFS DIST
    void genBfsDist() { 
        if (dist == null) {
            dist = new int[h][w]; 
            pre = new int[h][w];
        }
        // log(h+" "+w);
        
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) {
                dist[i][j] = MOD; 
                pre[i][j] = MOD;
            }

        Q.reset();
        dist[me.y][me.x] = 0; Q.push(64 * me.x + me.y);
        int t = 0;
        while (Q.size() > 0) {
            t ++;
            int x = Q.poll(); int y = x % 64; x = fdiv(x,64);
            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x + dx, Y = y + dy;
                    if (withinMoveRadius(new Robot2(me), dx, dy) && valid(X, Y) && dist[Y][X] == MOD) {
                        dist[Y][X] = dist[y][x] + 1;
                        if (pre[y][x] == MOD) pre[Y][X] = 64 * X + Y;
                        else pre[Y][X] = pre[y][x];
                        if (passable(X,Y)) Q.push(64 * X + Y);
                    }
                }
        }
    }
    void genEnemyDist() {
        if (enemyDist == null) {
            enemyDist = new int[h][w][2];

            for (int i = 0; i < h; ++i)
                for (int j = 0; j < w; ++j) 
                    for (int k = 0; k < 2; ++k)
                        enemyDist[i][j][k] = MOD;
        }
        if (!updEnemy) return;
        // log("UPDATING ENEMY");
        Q.reset();
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) 
                for (int k = 0; k < 2; ++k)
                    enemyDist[i][j][k] = MOD;
            
        for (int i: otherCastle) {
            Q.push(2*i);
            enemyDist[i%64][fdiv(i,64)][0] = 0;
        }
        for (int i: otherChurch) {
            Q.push(2*i);
            enemyDist[i%64][fdiv(i,64)][0] = 0;
        }

        int[] xd = {1,0,-1,0}, yd = {0,1,0,-1};
        while (Q.size() > 0) {
            int t = Q.poll();
            int k = t % 2; t = fdiv(t,2);
            int y = t % 64; 
            int x = fdiv(t,64);
            for (int z = 0; z < 4; ++z) {
                int X = x+xd[z], Y = y+yd[z];
                if (inMap(X,Y)) {
                    int K = k+1; if (valid(X,Y)) K = 0;
                    if (K == 2 || enemyDist[Y][X][K] != MOD) continue;
                    enemyDist[Y][X][K] = enemyDist[y][x][k]+1;
                    Q.push(2*(64*X+Y)+K);
                }
            }
        }

        updEnemy = false;
    }
    int bfsDist(int x) { return x == MOD ? MOD : dist[x % 64][fdiv(x,64)]; }
    int closest(ArrayList<Integer> A) {
        int bestDist = MOD, bestPos = MOD;
        for (int x : A)
            if (bfsDist(x) < bestDist) {
                bestDist = bfsDist(x);
                bestPos = x;
            }
        return bestPos;
    }
    int closestUnseen() {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j)
                if (passable(j, i) && dist[i][j] < bestDist && seenMap[i][j] == -1) {
                    bestDist = dist[i][j];
                    bestPos = 64 * j + i;
                }
        return bestPos;
    }
    int closestUnused(boolean[][] B) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j)
                if (B[i][j] && dist[i][j] < bestDist && seenMap[i][j] <= 0) {
                    bestDist = dist[i][j];
                    bestPos = 64 * j + i;
                }
        return bestPos;
    }
    int closeEmpty(int x, int y) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) {
            int X = x+i, Y = y+j;
            if (valid(X,Y) && dist[Y][X] != MOD && i*i+j*j < bestDist) {
                bestDist = i*i+j*j;
                bestPos = 64*X+Y;
            }
        }
        return bestPos;
    }
    int distHome() { return bfsDist(closest(myCastle)); }

    // DEBUG
    void dumpSurroundings() {
        log("POS: " + me.x + " " + me.y);
        for (int i = me.x - 5; i <= me.x + 5; ++i) {
            String t = "";
            for (int j = me.x - 5; j <= me.x + 5; ++j) {
                t += (char) ('0' + (map[i][j] ? 1 : 0));
            }
            log(t);
        }
    }
    void dumpRobots() {
        String T = "";
        for (Robot2 R: robots) T += R.getInfo();
        log(T);
    }

    // LOOKING FOR DESTINATION
    Robot2 closestEnemy() {
        Robot2 bes = null;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) if (containsRobot(j,i)) {
            Robot2 R = seenRobot[i][j];
            if (R.team == 1-me.team && euclidDist(R) < euclidDist(bes)) bes = R;
            // if (me.unit == PREACHER && euclidDist(R) > 16) log("WHOA");
        }
        return bes;
    }
    Robot2 closestAttacker(int t) {
        Robot2 bes = null;
        for (int i = 0; i < h; ++i) for (int j = 0; j < w; ++j) if (containsRobot(j,i)) {
            Robot2 R = seenRobot[i][j];
            if (R.isAttacker(1-me.team) && euclidDist(R) < euclidDist(bes)) bes = R;
        }
        return bes;
    }

    // MOVEMENT
    int getClosestChurch(boolean ourteam) {
        int bestDist = MOD; int bestPos = MOD;
        ArrayList<Integer> A;
        if (ourteam) A = myChurch;
        else A = otherChurch;
        for(int i : A) if(bfsDist(i) < bestDist) {
            bestDist = bfsDist(i);
            bestPos = i;
        }
        return bestPos;
    }
    int getClosestCastle(boolean ourteam) {
        int bestDist = MOD; int bestPos = MOD;
        ArrayList<Integer> A;
        if (ourteam) A = myCastle;
        else A = otherCastle;
        for(int i : A) if(bfsDist(i) < bestDist) {
            bestDist = bfsDist(i);
            bestPos = i;
        }
        return bestPos;
    }
    int getClosestStruct(boolean ourteam) {
        int bestCastle = getClosestCastle(ourteam);
        int bestChurch = getClosestChurch(ourteam);
        if(bfsDist(bestCastle) < bfsDist(bestChurch)) return bestCastle;
        else return bestChurch;
    }

    public boolean canBuild(int t) {
        return fuel >= CONSTRUCTION_F[t] && karbonite >= CONSTRUCTION_K[t];
    }

    public Action tryBuild(int type) {
        signal(turn,2);
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                if (passable(me.x + dx, me.y + dy))
                    return buildUnit(type, dx, dy);
        return null;
    }

    Map<Integer,Integer> castleX = new HashMap<>();
    Map<Integer,Integer> castleY = new HashMap<>();

    void addStruct(Robot2 R) {
        int t = 64*R.x+R.y;
        if(R.unit == CHURCH) {
            if(R.team == me.team && !myChurch.contains(t)) {
                myChurch.add(t);
            } else if(R.team != me.team && !otherChurch.contains(t)) {
                otherChurch.add(t);
                updEnemy = true;
            }
        } else {
            if (R.team == me.team && !myCastle.contains(t)) {
                myCastle.add(t);
                if (wsim() && R.unit == 0 && !emp[R.y][w-1-R.x]) {
                    updEnemy = true;
                    otherCastle.add(64*(w-1-R.x)+R.y);
                }
                if (hsim() && R.unit == 0 && !emp[h-1-R.y][R.x]) {
                    updEnemy = true;
                    otherCastle.add(64*R.x+(h-1-R.y));
                }
            } else if(R.team != me.team && !otherCastle.contains(t)) {
                updEnemy = true;
                otherCastle.add(t);
                if (wsim() && R.unit == 0 && !emp[R.y][w-1-R.x]) myCastle.add(64*(w-1-R.x)+R.y);
                if (hsim() && R.unit == 0 && !emp[h-1-R.y][R.x]) myCastle.add(64*R.x+(h-1-R.y));
            }
        }
    }

    void updateData() {
        h = map.length; w = map[0].length; 
        Robot[] tmp = getVisibleRobots();
        robots = new Robot2[getVisibleRobots().length];
        for (int i = 0; i < robots.length; ++i) robots[i] = new Robot2(tmp[i]);
        robotMap = getVisibleRobotMap();

        if (seenMap == null) {
            seenMap = new int[h][w];
            seenRobot = new Robot2[h][w];
            emp = new boolean[h][w];
            for (int i = 0; i < h; ++i)
                for (int j = 0; j < w; ++j)
                    seenMap[i][j] = -1;
        }

        myUnits = 0;
        for (Robot2 R: robots) {
            if (R.isStructure()) addStruct(R);
            if (R.team == me.team) myUnits ++;
            if (R.isStructure() && R.team == me.team && 1000 < R.signal && R.signal <= 1441) {
                int t = R.signal-1001;
                int y = (t%21)-10; y += R.y;
                int x = fdiv(t,21)-10; x += R.x;
                seenMap[y][x] = MOD;
                seenRobot[y][x] = makeRobot(3,1-me.team,x,y);
                log("OH "+me.x+" "+me.y+" "+x+" "+y);
            }
        }
        
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j)
                if (robotMap[i][j] != -1) {
                    seenMap[i][j] = robotMap[i][j];
                    if (robotMap[i][j] == 0) {
                        emp[i][j] = true;
                        seenRobot[i][j] = null;
                    } else {
                        seenRobot[i][j] = getRobot2(robotMap[i][j]);
                        if (seenRobot[i][j].isStructure()) emp[i][j] = false;
                        else emp[i][j] = true;
                    }
                }


        rem(myCastle); rem(otherCastle);

        if (turn == 0 && me.unit != CASTLE) {
            for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
                int x = me.x+dx, y = me.y+dy;
                if (containsRobot(x,y)) {
                    Robot2 R = getRobot2(robotMap[y][x]);
                    if (R.isStructure() && R.team == me.team && R.signal > 0) turn = R.signal;
                }
            }
        } else {
            turn ++;
        }
    }

    void sendInfo() {
        int res = me.unit;
        boolean seeEnemy = false;
        for (int i = -14; i <= 14; ++i) for (int j = -14; j <= 14; ++j) if (i*i+j*j <= 196) {
            int X = me.x+i, Y = me.y+j;
            if (containsRobot(X,Y) && seenRobot[Y][X].team != me.team) seeEnemy = true;
        }
        if (seeEnemy) res += 6;
        if (otherCastle.size() == 0) res += 12;
        castleTalk(res);
    }

    public Action turn() {
        if (me.turn == 1) log("TYPE: "+me.unit+" "+me.time);
        updateData();
        genBfsDist();
        genEnemyDist();
        if (me.unit != 0 && turn > 3) sendInfo();
        switch (me.unit) {
            case CASTLE: {
                Castle C = new Castle(this);
                return C.run();
            }
            case CHURCH: {
                Church C = new Church(this);
                return C.run();
            }
            case PILGRIM: {
                Pilgrim C = new Pilgrim(this);
                return C.run();
            }
            case CRUSADER: {
                Crusader C = new Crusader(this);
                return C.run();
            }
            case PROPHET: {
                Prophet C = new Prophet(this);
                return C.run();
            }
            case PREACHER: {
                Preacher C = new Preacher(this);
                return C.run();
            }
        }
        return null;
    }
}

/*
to fix:
* pilgrims don't flee from attackers
* look at last year's code and make other troops attack
*/

// file path:
// cd /usr/local/lib/node_modules/bc19/bots
// bcr
