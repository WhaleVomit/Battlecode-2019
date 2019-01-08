package bc19;
import java.util.*;

public class MyRobot extends BCAbstractRobot {
    int w,h, MOD = 1000000007;
    int turn;
    Robot[] robots;
    int[][] robotMap, dist, pre;
    ArrayList<Integer> myCastle = new ArrayList<>();
    ArrayList<Integer> otherCastle = new ArrayList<>();

    // UTILITY

    boolean valid (int x, int y) {
        if (!(0 <= y && y < w && 0 <= x && x < h)) return false;
        return map[y][x];
    }

    boolean isNotEmpty (int x, int y) {
        return valid(x,y) && robotMap[y][x] > 0;
    }

    boolean isEmpty(int x, int y) {
        return valid(x,y) && robotMap[y][x] <= 0;
    }

    int moveDist(Robot r) {
        if (r.unit == SPECS.CRUSADER) return 5;
        return 4;
    }

    boolean withinMoveRadius (Robot r, int dx, int dy) {
        return dx*dx+dy*dy <= moveDist(r);
    }

    boolean canMove(Robot r, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        if (!withinMoveRadius(r,dx,dy)) return false;
        int x = r.x+dx, y = r.y+dy;
        return valid(x,y) && robotMap[y][x] == 0;
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

    int dist(Robot A, Robot B) {
        return (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
    }

    Robot closestEnemy() {
        Robot bes = null;
        for (Robot R: robots) if (R.team != me.team) 
            if (bes == null || dist(R,me) < dist(bes,me)) 
                bes = R;
        return bes;
    }

    Action nextMove(int x, int y) {
        if (pre[y][x] == MOD) return null;
        int X = pre[y][x]; int Y = X%64; X = (X-Y)/64;
        return move(X-me.x,Y-me.y);
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

    Action moveTowardEnemy() {
        Robot R = closestEnemy(); if (R == null) return null;
        int t = closeEmpty(R.x,R.y); if (t == -1) return null;
        return nextMove((t-(t%64))/64,t%64);
    }

    boolean canAttack(int dx, int dy) {
        int x = me.x+dx, y = me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        // assert (getRobot(robotMap[y][x]) != null);

        int dist = dx*dx+dy*dy;
        if (me.unit == SPECS.CRUSADER) {
            if (getRobot(robotMap[y][x]).team == me.team) return false;
            if (dist < 1 || dist > 4) return false;
            return true;
        } else if (me.unit == SPECS.PROPHET) {
            if (getRobot(robotMap[y][x]).team == me.team) return false;
            if (dist < 4 || dist > 8) return false;
            return true;
        } else if (me.unit == SPECS.PREACHER) {
            if (dist < 1 || dist > 4) return false;
            return true;
        }
        return false;
    }

    Action tryAttack() {
        for (int dx = -2; dx <= 2; ++dx)
            for (int dy = -2; dy <= 2; ++dy) 
                if (canAttack(dx,dy)) {
                    // log("ATTACK "+dx+" "+dy);
                    return attack(dx,dy);
                }
        return null;
    }

    Action someMove() {
        Action A = nextMove(0,0);
        if (A != null) return A;
        // log("ZZ "+map[me.x+1][me.y]+" "+robotMap[me.x][me.y]+" "+robotMap[me.x+1][me.y]);
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy) 
                if (canMove(me,dx,dy)) {
                    // log("HA "+robotMap[me.x+dx][me.y+dy]);
                    return move(dx,dy);
                }
        return null;
    }
    
    Action tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (isEmpty(me.x+dx,me.y+dy))
                        return buildUnit(type,dx,dy);
        return null;
    }

    Action runCastle() {
        /*if (turn == 1) {
            log("Building a pilgrim.");
            return buildUnit(SPECS.PILGRIM,1,0);
        }*/

        if (turn % 10 == 0 && karbonite >= 50) {
            // log("HI");
            Action A = tryBuild(SPECS.CRUSADER);
            if (A != null) {
                log("Built crusader");
                return A;
            }
        } 

        // this.log("Building a crusader at " + (this.me.x+1) + ", " + (this.me.y+1));
        // return this.buildUnit(SPECS.CRUSADER, 1, 1);
        //return this.log("Castle health: " + this.me.health);

        return null;

    }

    Action moveTowardCastle() {
        int x = otherCastle.get(0);
        int y = x % 64; x = (x-y)/64;
        return nextMove(x,y);
    }

    Action runChurch() {
        return null;

    }
    
    Action runPilgrim() {
        return null;
        /*if (turn == 1) {
            log("I am a pilgrim.");
             
            //log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
        }*/

    }

    Action runCrusader() {
        /*if (turn == 1) {
            log("POS: "+me.x+" "+me.y);
            for (int i = me.x-5; i <= me.x+5; ++i) {
                String t;
                for (int j = me.x-5; j <= me.x+5; ++j) {
                    t += (char)('0'+(map[i][j] ? 1 : 0));
                }
                log(t);
            }
        }*/
        Action A = tryAttack();
        if (A != null) return A;
        A = moveTowardEnemy();
        if (A != null) return A;
        A = moveTowardCastle();
        if (A != null) return A;
        return someMove();
    }

    Action runProphet() {
        return null;

    }

    Action runPreacher() {
        return null;
    }

    boolean wsim() {
        for (int i = 0; i < w-1-i; ++i) for (int j = 0; j < h; ++j) if (map[i][j] != map[w-1-i][j]) return false;
        return true;
    }

    boolean hsim() {
        for (int i = 0; i < w; ++i) for (int j = 0; j < h-1-j; ++j) if (map[i][j] != map[i][h-1-j]) return false;
        return true;
    }

    public Action turn() {
        turn ++;
        w = map.length; h = map[0].length;
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();
        if (turn == 1) {
            for (Robot R: robots) if (R.unit == SPECS.CASTLE && R.team == me.team) myCastle.add(64*R.x+R.y);
            if (wsim()) {
                for (Integer R: myCastle) {
                    int y = R%64; int x = (R-y)/64;
                    otherCastle.add(64*(w-1-x)+y);
                }
            } 
            if (hsim()) {
                for (Integer R: myCastle) {
                    int y = R%64; int x = (R-y)/64;
                    otherCastle.add(64*x+(h-1-y)); // doesn't seem to work
                }
            }
            // Collections.shuffle(otherCastle);
        }
        /*for (int i = 0; i < w; ++i) for (int j = 0; j < h; ++j) if (robotMap[i][j] != -1) {
            log(me.x+" "+me.y+" "+i+" "+j+" "+robotMap[i][j]);
        }*/
        bfs();

        switch(me.unit) {
            case 0:
                return runCastle();
            case 1:
                return runChurch();
            case 2:
                return runPilgrim();
            case 3:
                return runCrusader();
            case 4:
                return runProphet();
            case 5:
                return runPreacher();
        }

    }
}