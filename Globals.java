package bc19;
import java.util.*;

public class Globals extends BCAbstractRobot {
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
	
    MyRobot myRobot;
    public int w, h;
    public Robot[] robots;
    public int[][] robotMap, dist, pre;
    public ArrayList<Integer> myCastle = new ArrayList<>();
    public ArrayList<Integer> otherCastle = new ArrayList<>();
    public int turn = 0;
    
    public Globals(MyRobot k) {
    		this.myRobot = k;
    }

    public boolean valid(int x, int y) {
        return x >= 0 && x < w && y >= 0 && y < h && myRobot.map[y][x];
    }
    
    public boolean isEmpty(int x, int y) {
    	    return valid(x,y) && robotMap[y][x] <= 0;
    	}

    public boolean unavailable(int x, int y) {
        return valid(x,y) && robotMap[y][x] != 0;
    }

    public boolean available(int x, int y) {
        return valid(x,y) && robotMap[y][x] == 0;
    }

    public int moveDist() {
        return MOVE_SPEED[myRobot.me.unit];
    }

    public boolean withinMoveRadius(int dx, int dy) {
        return dx * dx + dy * dy <= moveDist();
    }

    public boolean canMove(int dx, int dy) {
        return withinMoveRadius(dx, dy) && available(myRobot.me.x + dx, myRobot.me.y + dy);
    }

    public Action someMove() {
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy)
                if (canMove(dx, dy))
                    return myRobot.move(dx, dy);
        return null;
    }

    public boolean canAttack(int dx, int dy) {
        if(!CAN_ATTACK[myRobot.me.unit]) return false;

        int x = myRobot.me.x + dx, y = myRobot.me.y + dy;
        if (unavailable(x, y)) return false;
        if (myRobot.getRobot(robotMap[y][x]).team == myRobot.me.team) return false;

        int dist = dx * dx + dy * dy;
        return dist >= MIN_ATTACK_R[myRobot.me.unit] && dist <= MAX_ATTACK_R[myRobot.me.unit];
    }

    public Action nextMove(int x, int y) {
        if (pre[y][x] == MOD) return null;
        int X = pre[y][x] / 64; int Y = pre[y][x] % 64;
        return move(X - myRobot.me.x, Y - myRobot.me.y);
    }
    
    public Robot closestEnemy() {
    	    Robot bes = null;
    	    for (Robot R: robots) if (R.team != myRobot.me.team) 
    	        if (bes == null || dist(R,myRobot.me) < dist(bes,myRobot.me)) 
    	            bes = R;
    	    return bes;
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

	public Action moveTowardCastle() {
		int x = otherCastle.get(0) / 64;
        int y = otherCastle.get(0) % 64;
        return nextMove(x, y);
    }
	
	public int dist(Robot A, Robot B) {
		return (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
	}
}
