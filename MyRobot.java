package bc19;
import java.util.*;
import java.math.*;

public class MyRobot extends BCAbstractRobot {
    // CONSTANTS
    public int INF = Integer.MAX_VALUE;
    public int MOD = 1000000007;

    public int CASTLE = 0;
    public int CHURCH = 1;
    public int PILGRIM = 2;
    public int CRUSADER = 3;
    public int PROPHET = 4;
    public int PREACHER = 5;

    public int RED = 0;
    public int BLUE = 1;

    public int[] CONTRUCTION_K = {-1, -1, 10, 20, 25, 30};
    public int CONTRUCTION_F = 50;

    public int K_CARRY_CAP = 20;
    public int F_CARRY_CAP = 100;

    public boolean[] CAN_MOVE = {false, false, true, true, true};
    public int[] MOVE_SPEED = {-1, -1, 4, 9, 4, 4};
    public int[] MOVE_F_COST = {-1, -1, 1, 1, 2, 3};

    public int[] START_HEALTH = {-1, -1, 10, 40, 20, 60};

    public int[] VISION_R = {-1, -1, 10, 6, 8, 4};

    public int[] DAMAGE = {-1, -1, 10, 10, 20};

    public boolean[] CAN_ATTACK = {false, false, false, true, true, true};
    public int[] MIN_ATTACK_R = {-1, -1, -1, 1, 4, 1};
    public int[] MAX_ATTACK_R = {-1, -1, -1, 4, 8, 4};
    public int[] ATTACK_F_COST = {-1, -1, -1, 10, 25, 15};

    // DEBUG 
    void dumpSurroundings() {
        log("POS: "+me.x+" "+me.y);
        for (int i = me.x-5; i <= me.x+5; ++i) {
            String t;
            for (int j = me.x-5; j <= me.x+5; ++j) {
                t += (char)('0'+(map[i][j] ? 1 : 0));
            }
            log(t);
        }
    }
    String getInfo(Robot R) {
        String res = "ID: "+R.id+" UNIT: "+R.unit+", TEAM: "+R.team+", X: " +R.x+", Y: "+R.y;
        res += " CT: "+R.castle_talk;
        res += " | ";
        return res;
    }
    void removeDup(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (Integer i: A) if (!B.contains(i)) B.add(i);
        A.clear();
        for (Integer i: B) A.add(i);
    }

    // DATA
    int w,h,turn;
    Robot[] robots;
    int[][] robotMap, seenMap, dist, pre; // note that arrays are by y and tthen x
    Robot[][] seenRobot;
    ArrayList<Integer> myCastle = new ArrayList<>(), otherCastle = new ArrayList<>();
    int numCastles, numPilgrims, numAttack;
    int resource = -1;
    boolean goHome = false;
    // Random ran = new Random();

    int sq(int x) { return x*x; }

    // SQUARES

    boolean wsim() {
        for (int i = 0; i < w-1-i; ++i) for (int j = 0; j < h; ++j) if (map[i][j] != map[w-1-i][j]) return false;
        return true;
    }
    boolean hsim() {
        for (int i = 0; i < w; ++i) for (int j = 0; j < h-1-j; ++j) if (map[i][j] != map[i][h-1-j]) return false;
        return true;
    }
    boolean valid(int x, int y) {
        if (!(0 <= y && y < w && 0 <= x && x < h)) return false;
        return map[y][x];
    }
    boolean isNotEmpty (int x, int y) { return valid(x,y) && robotMap[y][x] > 0; }
    boolean isEmpty(int x, int y) { return valid(x,y) && robotMap[y][x] <= 0; }
    boolean adjacent(Robot r) { return Math.abs(me.x-r.x) <= 1 && Math.abs(me.y-r.y) <= 1; }
    int closest(ArrayList<Integer> A) {
        int bestDist = MOD, bestPos = MOD;
        for (int x: A) if (getDist(x) < bestDist) {
            bestDist = getDist(x);
            bestPos = x;
        }
        return bestPos;
    }
    int dist(Robot A, Robot B) {
        if (A == null || B == null) return MOD;
        return (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
    }
    void bfs() { // TLE?
        dist = new int[w][h];
        pre = new int[w][h];

        for (int i = 0; i < w; ++i)
            for (int j = 0; j < h; ++j) {
                dist[i][j] = MOD;
                pre[i][j] = MOD;
            }

        LinkedList<Integer> L = new LinkedList<>();

        dist[me.y][me.x] = 0; L.push(64*me.x+me.y);
        while (!L.isEmpty()) {
            int x = L.poll(); int y = x%64; x = (x-y)/64;
            // log(x+" "+y+" "+me.x+" "+me.y); break;
            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x+dx, Y = y+dy;
                    if (withinMoveRadius(me,dx,dy) && isEmpty(X,Y) && dist[Y][X] == MOD) {
                        dist[Y][X] = dist[y][x]+1;
                        if (pre[y][x] == MOD) pre[Y][X] = 64*X+Y;
                        else pre[Y][X] = pre[y][x];
                        L.add(64*X+Y);
                    } 
                }
        }
    }
    int moveDist(Robot r) { if (r.unit == CRUSADER) return 9; return 4; }
    boolean withinMoveRadius (Robot r, int dx, int dy) { return dx*dx+dy*dy <= moveDist(r); }
    int getDist(int x) { return dist[x%64][(x-(x%64))/64]; }


    // LOOKING FOR DESTINATION
    Robot closestEnemy() {
        Robot bes = null;
        for (Robot R: robots) if (R.team != me.team) 
            if (dist(R,me) < dist(bes,me)) 
                bes = R;
        return bes;
    }
    Robot closestAttacker() {
        Robot bes = null;
        // for (Robot R: robots) log(getInfo(R));
        for (Robot R: robots) {
            if (R.team != me.team && R.id > 0 && isAttacker(R)) 
            if (dist(R,me) < dist(bes,me)) 
                bes = R;
        }
        // log("Z");
        return bes;
    }
    int closestUnseen() {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < w; ++i) for (int j = 0; j < h; ++j) 
            if (isEmpty(j,i) && seenMap[i][j] == -1 && dist[i][j] < bestDist) {
                bestDist = dist[i][j];
                bestPos = 64*j+i;
            }
        return bestPos;
    }
    int closeEmpty(int x, int y) {
        int bes = MOD, pos = -1;
        for (int i = -2; i <= 2; ++i) for (int j = -2; j <= 2; ++j) if (isEmpty(x+i,y+j)) {
            int BES = i*i+j*j;
            if (BES < bes) {
                bes = BES;
                pos = 64*(x+i)+(y+j);
            }
        }
        return pos;
    }
    int distClose(int x) {
        x = closeEmpty((x-(x%64))/64,x%64); if (x == MOD) return x;
        return getDist(x);
    }
    int getClosest(boolean[][] B) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < w; ++i) for (int j = 0; j < h; ++j) if (B[i][j] && dist[i][j] < bestDist) {
            bestDist = dist[i][j];
            bestPos = 64*j+i;
        }
        return bestPos;
    }

    // MOVEMENT
    boolean canMove(Robot r, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        if (!withinMoveRadius(r,dx,dy)) return false;
        int x = r.x+dx, y = r.y+dy;
        return valid(x,y) && robotMap[y][x] == 0;
    }
    Action nextMove(int x, int y) {
        if (pre[y][x] == MOD) return null;
        int X = pre[y][x]; int Y = X%64; X = (X-Y)/64;
        return move(X-me.x,Y-me.y);
    }
    Action nextMove(int x) {
        if (x == MOD) return null;
        return nextMove((x-(x%64))/64,x%64);
    }
    Action moveToward(int x, int y) {
        int t = closeEmpty(x,y); if (t == -1) return null;
        return nextMove((t-(t%64))/64,t%64);
    }
    Action moveToward(Robot R) {
        if (R == null) return null;
        return moveToward(R.x,R.y);
    }
    Action moveAway(Robot R) {
        if (R.id <= 0) return null;
        int bestDist = -MOD, DX = MOD, DY = MOD;
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy)
                if (canMove(me,dx,dy)) {
                    if (sq(me.x+dx-R.x)+sq(me.y+dy-R.y) > bestDist) {
                        bestDist = sq(me.x+dx-R.x)+sq(me.y+dy-R.y);
                        DX = dx; DY = dy;
                    }
                }
        if (bestDist == MOD) return null;
        return move(DX,DY);
    }

    int getClosest(ArrayList<Integer> A) {
        int bes = MOD, val = MOD;
        for (int i: A) if (distClose(i) < bes) {
            bes = distClose(i);
            val = i;
        }
        return val;
    }

    Action moveTowardCastle() {
        while (otherCastle.size() > 0) {
            int x = otherCastle.get(0);
            int y = x % 64; x = (x-y)/64;
            if (robotMap[y][x] == 0) {
                otherCastle.remove(0);
                continue;
            }
            return nextMove(x,y);
        }
        return null;
    }
    Action moveHome() {
        if (myCastle.size() == 0) return null;
        for (Robot R: robots) 
            if (R.id > 0 && R.unit == SPECS.CASTLE && R.team == me.team && adjacent(R) && (me.fuel > 25 || me.karbonite > 5)) 
                return give(R.x-me.x,R.y-me.y,me.karbonite,me.fuel);
        int x = getClosest(myCastle);
        return moveToward((x-(x%64))/64,x%64);
    }
    int distHome() { return distClose(getClosest(myCastle)); }


    // ATTACK
    boolean isAttacker(Robot R) {
        return R != null && R.unit >= 3;
    }
    boolean isStructure(Robot R) {
        return R != null && R.unit <= 1;
    }

    boolean canAttack(int dx, int dy) {
        int x = me.x+dx, y = me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        int dist = dx*dx+dy*dy;
        if (me.unit == SPECS.CRUSADER) {
            if (fuel < 10) return false;
            if (getRobot(robotMap[y][x]).team == me.team) return false;
            if (dist < 1 || dist > 16) return false;
            return true;
        } else if (me.unit == SPECS.PROPHET) {
            if (fuel < 25) return false;
            if (getRobot(robotMap[y][x]).team == me.team) return false;
            if (dist < 16 || dist > 64) return false;
            return true;
        } else if (me.unit == SPECS.PREACHER) {
            if (fuel < 15) return false;
            if (dist < 3 || dist > 16) return false;
            return true;
        }
        return false;
    }

    Action tryAttack() { // crusader, prophet
        Robot bes = null;
        for (int dx = -8; dx <= 8; ++dx) for (int dy = -8; dy <= 8; ++dy) 
            if (canAttack(dx,dy)) {
                Robot R = getRobot(robotMap[me.y+dy][me.x+dx]);
                if (bes == null || 
                    R.health < bes.health || 
                    (!isAttacker(bes) && isAttacker(R)))
                    bes = R;
            }

        if (bes == null) return null;
        return attack(bes.x-me.x,bes.y-me.y);
    }

    // BUILD
    Action tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (isEmpty(me.x+dx,me.y+dy))
                        return buildUnit(type,dx,dy);
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

    public Action turn() {
        turn ++;
        if (turn == 1) log("TYPE: "+me.unit);
        w = map.length; h = map[0].length;
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();

        if (seenMap == null) {
            seenMap = new int[w][h];
            seenRobot = new Robot[w][h];
            for (int i = 0; i < w; ++i) 
                for (int j = 0; j < h; ++j) 
                    seenMap[i][j] = -1;
        }
        
        for (int i = 0; i < w; ++i) 
            for (int j = 0; j < h; ++j) 
                if (robotMap[i][j] != -1) {
                    seenMap[i][j] = robotMap[i][j];
                    if (robotMap[i][j] == 0) seenRobot[i][j] = null;
                    else {
                        // log(""+robotMap[i][j]);
                        seenRobot[i][j] = getRobot(robotMap[i][j]);
                    }
                }

        bfs();
        for (Robot R: robots) if (R.unit == SPECS.CASTLE) {
            if (R.team == me.team) myCastle.add(64*R.x+R.y);
            else otherCastle.add(64*R.x+R.y);
        }
        removeDup(myCastle);
        if (turn == 2) {
            if (wsim()) {
                for (Integer R: myCastle) { // note: this does not include all of your team's castles
                    int y = R%64; int x = (R-y)/64;
                    otherCastle.add(64*x+(w-1-y));
                }
            } 
            if (hsim()) {
                for (Integer R: myCastle) {
                    int y = R%64; int x = (R-y)/64;
                    otherCastle.add(64*(h-1-x)+y);
                }
            }
        }
        removeDup(otherCastle);
        switch(me.unit) {
            case 0: {
                Castle C = new Castle(this);
                return C.run();
            } case 1: {
                Church C = new Church(this);
                return C.run();
            } case 2: {
                Pilgrim C = new Pilgrim(this);
                return C.run();
            } case 3: {
                Crusader C = new Crusader(this);
                return C.run();
            } case 4: {
                Prophet C = new Prophet(this);
                return C.run();
            } case 5: {
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