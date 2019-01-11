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
    Robot[] robots;
    Robot[][] seenRobot; // stores last robot seen in pos
    int[][] robotMap, seenMap; // stores last id seen in pos
    int[][] dist, pre; 
    int[][][] enemyDist = null;
    boolean[][] emp; // whether square does not contain structure or not

    ArrayList<Integer> myCastle = new ArrayList<>(), otherCastle = new ArrayList<>();
    ArrayList<Integer> myChurch = new ArrayList<>(), otherChurch = new ArrayList<>();

    // MATH
    int fdiv(int a, int b) { return (a-(a%b))/b; }
    int sq(int x) { return x*x; }

    // UNIT TYPES
    boolean isStructure(Robot r) { return r != null && (r.unit == CASTLE || r.unit == CHURCH); }

    boolean isAttacker(Robot r) { return r != null && r.id > 0 && r.team != me.team && CAN_ATTACK[r.unit]; }

    // SQUARES
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

    boolean adjacent(Robot r) { return Math.abs(me.x - r.x) <= 1 && Math.abs(me.y - r.y) <= 1; }

    int numOpen(int t) { // how many squares around t are free
        int y = t % 64; int x = fdiv(t,64);
        int ret = 0;
        for (int i = x-1; i <= x+1; ++i)
            for (int j = y-1; j <= y+1; ++j)
                if (valid(i,j) && robotMap[j][i] == 0) ret ++;
        return ret;
    }

    int euclidDist(int x, int y) { return sq(me.x-x)+sq(me.y-y); }
    int euclidDist(Robot B) { return euclidDist(B.x,B.y); }
    int moveSpeed(Robot r) { return MOVE_SPEED[r.unit]; }

    boolean withinMoveRadius(Robot r, int dx, int dy) {
        return dx * dx + dy * dy <= moveSpeed(r) && MOVE_F_COST[r.unit] * (dx * dx + dy * dy) <= fuel;
    }

    int bfsDist(int x) {
        if (x == MOD) return MOD;
        return dist[x % 64][fdiv(x,64)];
    }

    int closest(ArrayList<Integer> A) {
        int bestDist = MOD, bestPos = MOD;
        for (int x : A)
            if (bfsDist(x) < bestDist) {
                bestDist = bfsDist(x);
                bestPos = x;
            }
        return bestPos;
    }

    void genBfsDist() { 
        if (dist == null) {
            dist = new int[h][w]; 
            pre = new int[h][w];
        }

        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) {
                dist[i][j] = MOD; pre[i][j] = MOD;
            }

        LinkedList<Integer> L = new LinkedList<>();

        dist[me.y][me.x] = 0; L.push(64 * me.x + me.y);
        while (!L.isEmpty()) {
            int x = L.poll(); int y = x % 64; x = fdiv(x,64);

            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x + dx, Y = y + dy;
                    if (withinMoveRadius(me, dx, dy) && valid(X, Y) && dist[Y][X] == MOD) {
                        dist[Y][X] = dist[y][x] + 1;
                        if (pre[y][x] == MOD) pre[Y][X] = 64 * X + Y;
                        else pre[Y][X] = pre[y][x];
                        if (passable(X,Y)) L.add(64 * X + Y);
                    }
                }
        }
    }

    void genEnemyDist() {
        if (enemyDist == null) enemyDist = new int[h][w][2];
        LinkedList<Integer> todo = new LinkedList<Integer>();
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) 
                for (int k = 0; k < 2; ++k)
                    enemyDist[i][j][k] = MOD;
            
        for (int i: otherCastle) {
            todo.push(2*i);
            enemyDist[i%64][fdiv(i,64)][0] = 0;
        }
        for (int i: otherChurch) {
            todo.push(2*i);
            enemyDist[i%64][fdiv(i,64)][0] = 0;
        }
        int[] xd = {1,0,-1,0};
        int[] yd = {0,1,0,-1};
        while (!todo.isEmpty()) {
            int t = todo.poll(); 
            int k = t % 2; t = fdiv(t,2);
            int y = t % 64; 
            int x = fdiv(t,64);
            for (int z = 0; z < 4; ++z) {
                int X = x+xd[z], Y = y+yd[z];
                if (inMap(X,Y)) {
                    int K = k+1; if (valid(X,Y)) K = 0;
                    if (K == 2 || enemyDist[Y][X][K] != MOD) continue;
                    enemyDist[Y][X][K] = enemyDist[y][x][k]+1;
                    todo.push(2*(64*X+Y)+K);
                }
            }
        }
        // log("HA "+enemyDist.length);
    }

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

    String getInfo(Robot R) {
        String res = R.id+" "+R.unit + " " + R.team + " " + R.x + " " + R.y;
        res += " " + R.castle_talk+" "+R.signal;
        res += " |\n";
        return res;
    }

    void dumpRobots() {
        String T = getInfo(me);
        for (Robot R: robots) T += getInfo(R);
        log(T);
    }

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

    // LOOKING FOR DESTINATION
    Robot closestEnemy() {
        Robot bes = null;
        for (Robot R : robots)
            if (R.team != me.team)
                if (bes == null || euclidDist(R) < euclidDist(bes))
                    bes = R;
        return bes;
    }

    Robot closestAttacker() {
        Robot best = null;
        for (Robot R : robots)
            if(isAttacker(R))
                if(best == null || euclidDist(R) < euclidDist(best))
                    best = R;
        return best;
    }

    Robot closestAlly() { // closest allied soldier
        Robot bes = null;
        for (Robot R : robots)
            if (R.team == me.team && R.unit > 1)
                if (bes == null || euclidDist(R) < euclidDist(bes))
                    bes = R;
        return bes;
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

    int closeEmpty(int x, int y) {
        int bes = MOD, pos = MOD;
        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                if (passable(x + i, y + j)) {
                    int BES = i * i + j * j;
                    if (BES < bes) {
                        bes = BES;
                        pos = 64 * (x + i) + (y + j);
                    }
                }
        return pos;
    }

    int getClosestUnused(boolean[][] B) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j)
                if (B[i][j] && dist[i][j] < bestDist && seenMap[i][j] <= 0) {
                    bestDist = dist[i][j];
                    bestPos = 64 * j + i;
                }
        return bestPos;
    }

    int distHome() { return bfsDist(closest(myCastle)); }

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

    void addStruct(Robot R) {
        int t = 64*R.x+R.y;
        if(R.unit == CHURCH) {
            if(R.team == me.team && !myChurch.contains(t)) myChurch.add(t);
            else if(R.team != me.team && !otherChurch.contains(t)) otherChurch.add(t);
        } else {
            if (R.team == me.team && !myCastle.contains(t)) {
                myCastle.add(t);
                if (wsim() && R.unit == 0 && !emp[R.y][w-1-R.x]) otherCastle.add(64*(w-1-R.x)+R.y);
                if (hsim() && R.unit == 0 && !emp[h-1-R.y][R.x]) otherCastle.add(64*R.x+(h-1-R.y));
            } else if(R.team != me.team && !otherCastle.contains(t)){
                otherCastle.add(t);
                if (wsim() && R.unit == 0 && !emp[R.y][w-1-R.x]) myCastle.add(64*(w-1-R.x)+R.y);
                if (hsim() && R.unit == 0 && !emp[h-1-R.y][R.x]) myCastle.add(64*R.x+(h-1-R.y));
            }
        }
    }

    void updateData() {
        h = map.length; w = map[0].length; 
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();

        if (seenMap == null) {
            seenMap = new int[h][w];
            seenRobot = new Robot[h][w];
            emp = new boolean[h][w];
            for (int i = 0; i < h; ++i)
                for (int j = 0; j < w; ++j)
                    seenMap[i][j] = -1;
        }

        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j)
                if (robotMap[i][j] != -1) {
                    seenMap[i][j] = robotMap[i][j];
                    if (robotMap[i][j] == 0) {
                        emp[i][j] = true;
                        seenRobot[i][j] = null;
                    } else {
                        seenRobot[i][j] = getRobot(robotMap[i][j]);
                        if (isStructure(seenRobot[i][j])) emp[i][j] = false;
                        else emp[i][j] = true;
                    }
                }

        myUnits = 0;
        for (Robot R: robots) {
            if (isStructure(R)) addStruct(R);
            if (R.id <= 0 || R.team == me.team) myUnits ++;
        }
        
        rem(myCastle); rem(otherCastle);

        if (turn == 0 && me.unit != SPECS.CASTLE) {
            for (int dx = -1; dx <= 1; ++dx) for (int dy = -1; dy <= 1; ++dy) {
                int x = me.x+dx, y = me.y+dy;
                if (containsRobot(x,y)) {
                    Robot R = getRobot(robotMap[y][x]);
                    if (isStructure(R) && R.team == me.team && R.signal > 0) turn = R.signal;
                }
            }
        } else {
            turn ++;
        }
    }

    public Action turn() {
        if (me.turn == 1) log("TYPE: "+me.unit);
        updateData();
        genBfsDist();
        genEnemyDist();
        // log(me.turn+" "+me.unit+" "+myCastle.size()+" "+otherCastle.size());
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
