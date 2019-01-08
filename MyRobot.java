package bc19;
import java.util.*;
import java.math.*;

public class MyRobot extends BCAbstractRobot {
	Global g;
	boolean first = true;
	
    void dumpSurroundings() {
        if (g.turn == 1) {
            log("POS: "+me.x+" "+me.y);
            for (int i = me.x-5; i <= me.x+5; ++i) {
                String t;
                for (int j = me.x-5; j <= me.x+5; ++j) {
                    t += (char)('0'+(map[i][j] ? 1 : 0));
                }
                log(t);
            }
        }
    }

    void bfs() { // TLE?
    		g.dist = new int[g.w][g.h];
    		g.pre = new int[g.w][g.h];

        for (int i = 0; i < g.w; ++i)
            for (int j = 0; j < g.h; ++j) {
            		g.dist[i][j] = g.MOD;
            		g.pre[i][j] = g.MOD;
            }

        LinkedList<Integer> L = new LinkedList<>();

        g.dist[me.y][me.x] = 0; L.push(64*me.x+me.y);
        while (!L.isEmpty()) {
            int x = L.poll(); int y = x%64; x = (x-y)/64;
            // log(x+" "+y+" "+me.x+" "+me.y); break;
            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x+dx, Y = y+dy;
                    if (g.withinMoveRadius(me,dx,dy) && g.isEmpty(X,Y) && g.dist[Y][X] == g.MOD) {
                    		g.dist[Y][X] = g.dist[y][x]+1;
                        if (g.pre[y][x] == g.MOD) g.pre[Y][X] = 64*X+Y;
                        else g.pre[Y][X] = g.pre[y][x];
                        L.add(64*X+Y);
                    } 
                }
        }
    }

    

    // Map<Integer,Integer> M = new HashMap<>();
    boolean wsim() {
        for (int i = 0; i < g.w-1-i; ++i) for (int j = 0; j < g.h; ++j) if (map[i][j] != map[g.w-1-i][j]) return false;
        return true;
    }

    boolean hsim() {
        for (int i = 0; i < g.w; ++i) for (int j = 0; j < g.h-1-j; ++j) if (map[i][j] != map[i][g.h-1-j]) return false;
        return true;
    }

    String getInfo(Robot R) {
        String res = R.unit+" "+R.team+" " +R.x+" "+R.y;
        res += " "+R.castle_talk;
        res += " | ";
        return res;
    }

    void removeDup(ArrayList<Integer> A) {
        ArrayList<Integer> B = new ArrayList<>();
        for (Integer i: A) if (!B.contains(i)) B.add(i);
        A = B;
    }

    public Action turn() {
    		if(first) {
    			first = false;
    			g = new Global(this);
    		}
        g.turn ++;
        g.w = map.length; g.h = map[0].length;
        g.robots = getVisibleRobots();
        g.robotMap = getVisibleRobotMap();

        if (g.seenMap == null) {
        		g.seenMap = new int[g.w][g.h];
        		g.seenRobot = new Robot[g.w][g.h];
            for (int i = 0; i < g.w; ++i) 
                for (int j = 0; j < g.h; ++j) 
                		g.seenMap[i][j] = -1;
        }
        
        for (int i = 0; i < g.w; ++i) 
            for (int j = 0; j < g.h; ++j) 
                if (g.robotMap[i][j] != -1) {
                		g.seenMap[i][j] = g.robotMap[i][j];
                    if (g.robotMap[i][j] == 0) g.seenRobot[i][j] = null;
                    else {
                        // log(""+robotMap[i][j]);
                    		g.seenRobot[i][j] = g.getRobot(g.robotMap[i][j]);
                    }
                }

        bfs();
        for (Robot R: g.robots) if (R.unit == SPECS.CASTLE) {
            if (R.team == me.team) g.myCastle.add(64*R.x+R.y);
            else g.otherCastle.add(64*R.x+R.y);
        }
        removeDup(g.myCastle);
        if (g.turn == 2) {
            if (wsim()) {
                for (Integer R: g.myCastle) { // note: this does not include all of your team's castles
                    int y = R%64; int x = (R-y)/64;
                    g.otherCastle.add(64*x+(g.w-1-y));
                }
            } 
            if (hsim()) {
                for (Integer R: g.myCastle) {
                    int y = R%64; int x = (R-y)/64;
                    g.otherCastle.add(64*(g.h-1-x)+y);
                }
            }
        }
        removeDup(g.otherCastle);
        switch(me.unit) {
            case 0:
                Castle ooo1 = new Castle(this);
                return ooo1.run();
            case 1:
            		Church ooo2 = new Church(this);
                return ooo2.run();
            case 2:
                Pilgrim ooo3 = new Pilgrim(this);
                return ooo3.run();
            case 3:
            		Crusader ooo4 = new Crusader(this);
                return ooo4.run();
            case 4:
            		Prophet ooo5 = new Prophet(this);
                return ooo5.run();
            case 5:
            		Preacher ooo6 = new Preacher(this);
                return ooo6.run();
        }

    }
}

/*
to fix:
* pilgrims don't flee from attackers
* pilgrims don't return home
* removeDup may not be working
*/