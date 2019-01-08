package bc19;

import static constants;

public class MyRobot extends Globals {

    private int w, h;

    private void bfs() {
        dist = new int[h][w];
        pre = new int[h][w];

        for (int i = 0; i < h; ++i)
            for (int j = 0; j < w; ++j) {
                dist[i][j] = INF;
                pre[i][j] = INF;
            }

        Queue<Integer> L = new LinkedList<Integer>();

        dist[me.y][me.x] = 0;
        L.push(64 * me.x + me.y);

        while (!L.isEmpty()) {
            int x = L.poll() / 64;
            int y = L.poll() % 64;

            for (int dx = -3; dx <= 3; ++dx)
                for (int dy = -3; dy <= 3; ++dy) {
                    int X = x + dx, Y = y + dy;
                    if (available(X, Y) && withinMoveRadius(dx, dy) && dist[Y][X] == INF) {
                        dist[Y][X] = dist[y][x] + 1;
                        pre[Y][X] = 64 * X + Y;
                        L.add(64 * X + Y);
                    }
                }
        }
    }

    private boolean wsim() {
        for (int i = 0; i < w - 1 - i; ++i) for (int j = 0; j < h; ++j) if (map[i][j] != map[w-1-i][j]) return false;
        return true;
    }

    public Action turn() {
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();

        if (turn == 1) {
            w = map[0].length;
            h = map.length;
            for (Robot r: robots) if (r.unit == CASTLE && r.team == me.team) myCastle.add(64 * r.x + r.y);
            if (wsim()) {
                for (Integer pos : myCastle) {
                    int x = pos / 64;
                    int y = pos % 64;
                    otherCastle.add(64 * (w - 1 - x) + y);
                }
            } else {
                for (Integer pos : myCastle) {
                    int y = R % 64; int x = (R-y)/64;
                    otherCastle.add(64 * x + (h - 1 - y));
                }
            }
        }
        bfs();

        switch(me.unit) {
            case 0:
                return Castle.run(me);
            case 1:
                return Church.run(me);
            case 2:
                return Pilgrim.run(me);
            case 3:
                return Crusader.run(me);
            case 4:
                return Prophet.run(me);
            case 5:
                return Preacher.run(me);
        }

    }
}
