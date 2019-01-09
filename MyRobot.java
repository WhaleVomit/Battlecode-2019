package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class MyRobot extends BCAbstractRobot {

    // DATA
    int type0 = 0, type1 = 0;
    int w, h;
    Robot[] robots;
    int[][] robotMap, seenMap, dist, pre; // note that arrays are by y and tthen x
    Robot[][] seenRobot;
    ArrayList<Integer> myCastle = new ArrayList<>(), otherCastle = new ArrayList<>();
    int numCastles=0, numPilgrims=0, numAttack=0, numChurches=0, numCrusaders=0;
    int resource = -1;

    boolean goHome;
    boolean[][] emp;
    // Random ran = new Random();

    // UNIT TYPES
    boolean isStructure(Robot r) {
        return r.unit == CASTLE || r.unit == CHURCH;
    }

    boolean isAttacker(Robot r) {
        return r.team != me.team && !isStructure(r);
    }

    // SQUARES
    boolean hsim() {
        for (int i = 0; i < h - 1 - i; ++i)
            for (int j = 0; j < w; ++j) if (map[i][j] != map[h - 1 - i][j]) return false;
        return true;
    }

    boolean wsim() {
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w - 1 - j; ++j) if (map[i][j] != map[i][w - 1 - j]) return false;
        return true;
    }

    boolean inMap(int x, int y) {
        return x >= 0 && x < w && y >= 0 && y < h;
    }

    boolean valid(int x, int y) {
        if (!inMap(x,y)) return false;
        return map[y][x];
    }

    boolean isNotEmpty(int x, int y) {
        return valid(x, y) && robotMap[y][x] > 0;
    }

    boolean isEmpty(int x, int y) {
        return valid(x, y) && robotMap[y][x] <= 0;
    }

    boolean adjacent(Robot r) {
        return Math.abs(me.x - r.x) <= 1 && Math.abs(me.y - r.y) <= 1;
    }

    int dist(Robot B) {
        return (me.x - B.x) * (me.x - B.x) + (me.y - B.y) * (me.y - B.y);
    }

    int moveDist(Robot r) {
        return MOVE_SPEED[r.unit];
    }

    boolean withinMoveRadius(Robot r, int dx, int dy) {
        return dx * dx + dy * dy <= moveDist(r) && MOVE_F_COST[r.unit] * (dx * dx + dy * dy) <= fuel;
    }

    int getDist(int x) {
        return dist[x % 64][x / 64];
    }

    int closest(ArrayList<Integer> A) {
        int bestDist = MOD, bestPos = MOD;
        for (int x : A)
            if (getDist(x) < bestDist) {
                bestDist = getDist(x);
                bestPos = x;
            }
        return bestPos;
    }

    void bfs() { // TLE?
        dist = new int[h][w];
        pre = new int[h][w];

        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) {
                dist[i][j] = MOD;
                pre[i][j] = MOD;
            }

        LinkedList<Integer> L = new LinkedList<>();

        dist[me.y][me.x] = 0;
        L.push(64 * me.x + me.y);
        while (!L.isEmpty()) {
            int x = L.poll();
            int y = x % 64;
            x = (x - y) / 64;

            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x + dx, Y = y + dy;
                    if (withinMoveRadius(me, dx, dy) && valid(X, Y) && dist[Y][X] == MOD) {
                        dist[Y][X] = dist[y][x] + 1;
                        if (pre[y][x] == MOD && isEmpty(X, Y)) pre[Y][X] = 64 * X + Y;
                        else pre[Y][X] = pre[y][x];
                        if (isEmpty(X,Y)) {
                            L.add(64 * X + Y);
                        }
                    }
                }
        }
    }

    // DEBUG
    void dumpSurroundings() {
        if (me.turn == 1) {
            log("POS: " + me.x + " " + me.y);
            for (int i = me.x - 5; i <= me.x + 5; ++i) {
                String t = "";
                for (int j = me.x - 5; j <= me.x + 5; ++j) {
                    t += (char) ('0' + (map[i][j] ? 1 : 0));
                }
                log(t);
            }
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

    /*void removeDup(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (Integer i : A) if (!B.contains(i)) B.add(i);
        A.clear();
        for (Integer i : B) A.add(i);
    }*/


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
                if (bes == null || dist(R) < dist(bes))
                    bes = R;
        return bes;
    }

    Robot closestAttacker() {
        Robot best = null;
        for (Robot R : robots)
            if(isAttacker(R))
                if(best == null || dist(R) < dist(best))
                    best = R;
        return best;
    }

    int closestUnseen() {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j)
                if (isEmpty(j, i) && seenMap[i][j] == -1 && dist[i][j] < bestDist) {
                    bestDist = dist[i][j];
                    bestPos = 64 * j + i;
                }
        return bestPos;
    }

    int closeEmpty(int x, int y) {
        int bes = MOD, pos = -1;
        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                if (isEmpty(x + i, y + j)) {
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
                if (B[i][j] && dist[i][j] < bestDist && robotMap[i][j] <= 0) {
                    bestDist = dist[i][j];
                    bestPos = 64 * j + i;
                }
        return bestPos;
    }

    int distHome() {
        return getDist(closest(myCastle));
    }

    // MOVEMENT
    boolean canMove(Robot r, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        if (!withinMoveRadius(r, dx, dy)) return false;
        int x = r.x + dx, y = r.y + dy;
        return valid(x, y) && robotMap[y][x] == 0;
    }

    Action nextMove(int x, int y) {
        if (pre[y][x] == MOD) return null;
        if (dist[y][x] == 1 && !isEmpty(x,y)) return null;
        int Y = pre[y][x] % 64;
        int X = (pre[y][x] - Y) / 64;
        return move(X - me.x, Y - me.y);
    }

    Action nextMove(int x) {
        if (x == MOD) return null;
        return nextMove((x - (x % 64)) / 64, x % 64);
    }

    Action moveToward(int x, int y) {
        int t = closeEmpty(x, y);
        if (t == -1) return null;
        return nextMove((t - (t % 64)) / 64, t % 64);
    }

    Action moveToward(Robot R) {
        if (R == null) return null;
        return moveToward(R.x, R.y);
    }

    Action moveAway(int x, int y) {
        int farthest = INF;
        Action best;
        for(int i = -3; i <= 3; i++)
            for(int j = -3; j <= 3; j++)
                if(isEmpty(me.x + i, me.y + j) && withinMoveRadius(me, i, j)) {
                    int dis = (x - me.x - i) * (x - me.x - i) + (y - me.y - j) * (y - me.y - j);
                    if(dis > farthest) {
                        farthest = dis;
                        best = move(i, j);
                    }
                }
        return best;
    }

    Action moveAway(Robot R) {
        if(R == null) return null;
        return moveAway(R.x, R.y);
    }

    Action moveTowardCastle() {
        while (otherCastle.size() > 0) {
            int y = otherCastle.get(0) % 64;
            int x = (otherCastle.get(0) - y) / 64;
            if (robotMap[y][x] == 0) {
                otherCastle.remove(0);
                continue;
            }
            return nextMove(x, y);
        }
        return null;
    }

    boolean enoughResources() {
        return me.fuel > 25 || (me.fuel > 0 && !fuelMap[me.y][me.x]) || me.karbonite > 5 || (me.karbonite > 0 && !karboniteMap[me.y][me.y]);
    }

    Action moveHome() {
        for (Robot R: robots)
            if ((R.unit == CASTLE || R.unit == CHURCH) && R.team == me.team && adjacent(R) && enoughResources())
                return give(R.x-me.x,R.y-me.y,me.karbonite,me.fuel);
        int x = getClosestStruct(true);
        return moveToward((x-(x%64))/64,x%64);
    }

    int getClosestStruct (boolean ourteam) {
        int bestDist = MOD; int bestPos = MOD;
        ArrayList<Integer> A;
        if (ourteam) A = myCastle;
        else A = otherCastle;
        for (int i: A) {
            int y = i%64; int x = (i-y)/64;
            if (dist[y][x] < bestDist) {
                bestDist = dist[y][x];
                bestPos = i;
            }
        }
        return bestPos;
    }

    // ATTACK
    boolean canAttack(int dx, int dy) {
        if(ATTACK_F_COST[me.unit] > fuel) return false;
        int x = me.x + dx, y = me.y + dy;
        if (!inMap(x,y)) return false;
        if (!isNotEmpty(x, y)) return false;
        if (getRobot(robotMap[y][x]).team == me.team) return false;

        int dist = dx * dx + dy * dy;
        if (me.unit == CRUSADER) {
            if (dist < 1 || dist > 16) return false;
            return true;
        } else if (me.unit == PROPHET) {
            if (dist < 16 || dist > 64) return false;
            return true;
        } else if (me.unit == PREACHER) {
            if (dist < 3 || dist > 16) return false;
            return true;
        }
        return false;
    }

    Action tryAttack() {
        for (int dx = -8; dx <= 8; ++dx)
            for (int dy = -8; dy <= 8; ++dy)
                if (canAttack(dx, dy)) return attack(dx, dy);
        return null;
    }

    // BUILD
    Action tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                if (isEmpty(me.x + dx, me.y + dy))
                    return buildUnit(type, dx, dy);
        return null;
    }

    boolean canBuild(int t) {
        if (fuel < 50) return false;
        if (t == SPECS.PILGRIM) return karbonite >= 10;
        if (t == SPECS.CRUSADER) return karbonite >= 20;
        if (t == SPECS.PROPHET) return karbonite >= 25;
        if (t == SPECS.PREACHER) return karbonite >= 30;
        return false;
    }

    public Action makePilgrim() {
        if (!canBuild(PILGRIM)) return null;
        // log("???"); signal(10,2); -> this works
        Action A = tryBuild(PILGRIM);
        if (A == null) return A;
        numPilgrims ++;
        log("Built pilgrim");
        int t = 0;
        if (2*type0 < type1 || (5*karbonite < fuel && 2*type1 >= type0)) {
            type0 ++; t = 1;
            log("KARBONITE");
        } else {
            type1 ++; t = 2;
            log("FUEL");
        }
        // log("NEW?");
        signal(t,2); // -> this doesn't work
        return A;
    }

    Map<Integer,Integer> castleX = new HashMap<>();
    Map<Integer,Integer> castleY = new HashMap<>();

    void addStruct(Robot R) {
        if (R.team == me.team) {
            int t = 64*R.x+R.y;
            if (!myCastle.contains(t)) {
                myCastle.add(t);
                if (wsim() && R.unit == 0 && !emp[R.y][w-1-R.x]) otherCastle.add(64*(w-1-R.x)+R.y);
                if (hsim() && R.unit == 0 && !emp[h-1-R.y][R.x]) otherCastle.add(64*R.x+(h-1-R.y));
            }
        } else {
            int t = 64*R.x+R.y;
            if (!otherCastle.contains(t)) {
                otherCastle.add(t);
                if (wsim() && R.unit == 0 && !emp[R.y][w-1-R.x]) myCastle.add(64*(w-1-R.x)+R.y);
                if (hsim() && R.unit == 0 && !emp[h-1-R.y][R.x]) myCastle.add(64*R.x+(h-1-R.y));
            }
        }
    }

    void updateData() {
        w = map[0].length; h = map.length;
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();

        if (seenMap == null) {
            seenMap = new int[h][w];
            seenRobot = new Robot[h][w];
            for (int i = 0; i < h; ++i)
                for (int j = 0; j < w; ++j)
                    seenMap[i][j] = -1;
        }

        if (emp == null) emp = new boolean[h][w];

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

        for (Robot R: robots) if (isStructure(R)) addStruct(R);
        rem(myCastle); rem(otherCastle);
    }

    public Action turn() {
        updateData();
        bfs();
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
