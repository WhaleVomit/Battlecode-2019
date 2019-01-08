package bc19;
import java.util.*;

public class MyRobot extends BCAbstractRobot {
	
	Globals glo;
	boolean first = true;

    private void bfs() {
        glo.dist = new int[glo.h][glo.w];
        glo.pre = new int[glo.h][glo.w];

        for (int i = 0; i < glo.h; ++i)
            for (int j = 0; j < glo.w; ++j) {
                glo.dist[i][j] = glo.INF;
                glo.pre[i][j] = glo.INF;
            }

        Queue<Integer> L = new LinkedList<Integer>();

        glo.dist[me.y][me.x] = 0;
        L.add(64 * me.x + me.y);

        while (!L.isEmpty()) {
            int x = L.poll() / 64;
            int y = L.poll() % 64;

            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x + dx, Y = y + dy;
                    if (glo.available(X, Y) && glo.withinMoveRadius(dx, dy) && glo.dist[Y][X] == glo.INF) {
                        glo.dist[Y][X] = glo.dist[y][x] + 1;
                        glo.pre[Y][X] = 64 * X + Y;
                        L.add(64 * X + Y);
                    }
                }
        }
    }

    private boolean wsim() {
        for (int i = 0; i < glo.w - 1 - i; ++i) for (int j = 0; j < glo.h; ++j) if (map[i][j] != map[glo.w-1-i][j]) return false;
        return true;
    }

    public Action turn() {
    		if(first) {
    			first = false;
    			glo = new Globals(this);
    		}
    		glo.turn++;
        glo.robots = getVisibleRobots();
        glo.robotMap = getVisibleRobotMap();

        if (glo.turn == 1) {
            glo.w = map[0].length;
            glo.h = map.length;
            for (Robot r: glo.robots) if (r.unit == glo.CASTLE && r.team == me.team) glo.myCastle.add(64 * r.x + r.y);
            if (wsim()) {
                for (Integer pos : glo.myCastle) {
                    int x = pos / 64;
                    int y = pos % 64;
                    glo.otherCastle.add(64 * (glo.w - 1 - x) + y);
                }
            } else {
                for (Integer R : glo.myCastle) {
                    int y = R % 64; int x = (R-y)/64;
                    glo.otherCastle.add(64 * x + (glo.h - 1 - y));
                }
            }
        }
        bfs();

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
