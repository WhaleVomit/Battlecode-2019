package bc19;
import java.util.*;
import java.math.*;

public class Global extends BCAbstractRobot {
	MyRobot myRobot;
	
	public Global(MyRobot k) {
		this.myRobot = k;
	}
	
	
	
	
	public int w,h, MOD = 1000000007;
    public int turn;
    public Robot[] robots;
    public int[][] robotMap, seenMap, dist, pre;
    public Robot[][] seenRobot;
    public ArrayList<Integer> myCastle = new ArrayList<>();
    public ArrayList<Integer> otherCastle = new ArrayList<>();
    public int numCastles, numPilgrims;
    public int resource = -1;
    // Random ran = new Random();

    // UTILITY

    public boolean valid (int x, int y) {
        if (!(0 <= y && y < w && 0 <= x && x < h)) return false;
        return myRobot.map[y][x];
    }

    public boolean isNotEmpty (int x, int y) {
        return valid(x,y) && robotMap[y][x] > 0;
    }

    public boolean isEmpty(int x, int y) {
        return valid(x,y) && robotMap[y][x] <= 0;
    }

    public boolean adjacent(Robot r) {
        return Math.abs(me.x-r.x) <= 1 && Math.abs(me.y-r.y) <= 1; 
    }

    public int moveDist(Robot r) {
        if (r.unit == myRobot.SPECS.CRUSADER) return 9;
        return 4;
    }

    public int getDist(int x) {
        return dist[(x-(x%64))/64][x%64];
    }

    public int closest(ArrayList<Integer> A) {
        int bestDist = MOD, bestPos = MOD;
        for (int x: A) if (getDist(x) < bestDist) {
            bestDist = getDist(x);
            bestPos = x;
        }
        return bestPos;
    }
    
    public Action returnHome() {
        for (Robot R: robots) if (R.unit == myRobot.SPECS.CASTLE && R.team == myRobot.me.team && adjacent(R)) 
            return give(R.x-myRobot.me.x,R.y-myRobot.me.y,myRobot.me.karbonite,myRobot.me.fuel);
        return nextMove(closest(myCastle));
    }

    public int getClosest(boolean[][] B) {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < w; ++i) for (int j = 0; j < h; ++j) if (B[i][j] && dist[i][j] < bestDist) {
            bestDist = dist[i][j];
            bestPos = 64*j+i;
        }
        return bestPos;
    }
    
    public Action moveTowardCastle() {
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
    
    boolean canBuild(int t) {
        if (myRobot.fuel < 50) return false;
        if (t == myRobot.SPECS.PILGRIM) return myRobot.karbonite >= 10;
        if (t == myRobot.SPECS.CRUSADER) return myRobot.karbonite >= 20;
        if (t == myRobot.SPECS.PROPHET) return myRobot.karbonite >= 25;
        if (t == myRobot.SPECS.PREACHER) return myRobot.karbonite >= 30;
        return false;
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

    int closestUnseen() {
        int bestDist = MOD, bestPos = MOD;
        for (int i = 0; i < w; ++i) for (int j = 0; j < h; ++j) 
            if (isEmpty(j,i) && seenMap[i][j] == -1 && dist[i][j] < bestDist) {
                bestDist = dist[i][j];
                bestPos = 64*j+i;
            }
        return bestPos;
    }

    public Action nextMove(int x, int y) {
        if (pre[y][x] == MOD) return null;
        int X = pre[y][x]; int Y = X%64; X = (X-Y)/64;
        return myRobot.move(X-myRobot.me.x,Y-myRobot.me.y);
    }

    public Action nextMove(int x) {
        if (x == MOD) return null;
        return nextMove((x-(x%64))/64,x%64);
    }

    public int closeEmpty(int x, int y) {
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

    public Action moveTowardEnemy() {
        Robot R = closestEnemy(); if (R == null) return null;
        int t = closeEmpty(R.x,R.y); if (t == -1) return null;
        return nextMove((t-(t%64))/64,t%64);
    }

    public boolean canAttack(int dx, int dy) {
        int x = myRobot.me.x+dx, y = myRobot.me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        // assert (getRobot(robotMap[y][x]) != null);

        int dist = dx*dx+dy*dy;
        if (myRobot.me.unit == myRobot.SPECS.CRUSADER) {
            if (getRobot(robotMap[y][x]).team == myRobot.me.team) return false;
            if (dist < 1 || dist > 4) return false;
            return true;
        } else if (myRobot.me.unit == myRobot.SPECS.PROPHET) {
            if (getRobot(robotMap[y][x]).team == myRobot.me.team) return false;
            if (dist < 4 || dist > 8) return false;
            return true;
        } else if (myRobot.me.unit == myRobot.SPECS.PREACHER) {
            if (dist < 1 || dist > 4) return false;
            return true;
        }
        return false;
    }

    public Action tryAttack() {
        for (int dx = -2; dx <= 2; ++dx)
            for (int dy = -2; dy <= 2; ++dy) 
                if (canAttack(dx,dy)) {
                    // log("ATTACK "+dx+" "+dy);
                    return myRobot.attack(dx,dy);
                }
        return null;
    }

    public Action tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (isEmpty(me.x+dx,me.y+dy))
                        return myRobot.buildUnit(type,dx,dy);
        return null;
    }
    
    public boolean withinMoveRadius (Robot r, int dx, int dy) {
        return dx*dx+dy*dy <= moveDist(r);
    }

    public boolean canMove(Robot r, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        if (!withinMoveRadius(r,dx,dy)) return false;
        int x = r.x+dx, y = r.y+dy;
        return valid(x,y) && robotMap[y][x] == 0;
    }
}