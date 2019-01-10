package bc19;

import static bc19.Consts.*;

public class Movable {
    public MyRobot Z;
    public Movable (MyRobot z) { Z = z; }

    public boolean enoughResources() {
        return Z.me.fuel > 25 || (Z.me.fuel > 0 && !Z.fuelMap[Z.me.y][Z.me.x]) 
        	|| Z.me.karbonite > 5 || (Z.me.karbonite > 0 && !Z.karboniteMap[Z.me.y][Z.me.y]);
    }

    public boolean canMove(Robot r, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        if (!Z.withinMoveRadius(r, dx, dy)) return false;
        return Z.passable(r.x+dx,r.y+dy);
    }

    public Action nextMove(int x, int y) {
        if (Z.pre[y][x] == MOD) return null;
        if (Z.dist[y][x] == 1 && Z.passable(x,y)) return null;
        int Y = Z.pre[y][x] % 64, X = Z.fdiv(Z.pre[y][x],64);
        return Z.move(X - Z.me.x, Y - Z.me.y);
    }

    public Action nextMove(int x) {
        if (x == MOD) return null;
        return nextMove(Z.fdiv(x,64), x % 64);
    }

    public Action moveToward(int x, int y) {
        return nextMove(Z.closeEmpty(x, y));
    }

    public Action moveToward(Robot R) {
        if (R == null) return null;
        return moveToward(R.x, R.y);
    }

    public Action moveAway(int x, int y) {
        int farthest = -MOD; Action best = null;
        for (int i = -3; i <= 3; i++)
            for (int j = -3; j <= 3; j++)
                if (Z.passable(Z.me.x + i, Z.me.y + j) && Z.withinMoveRadius(Z.me, i, j)) {
                    int dis = Z.sq(x - Z.me.x - i) + Z.sq(y - Z.me.y - j);
                    if(dis > farthest) {
                        farthest = dis;
                        best = Z.move(i, j);
                    }
                }
        return best;
    }

    public Action moveAway(Robot R) {
        if(R == null) return null;
        return moveAway(R.x, R.y);
    }

    public Action moveHome() {
        for (Robot R: Z.robots)
            if (Z.isStructure(R) && R.team == Z.me.team && Z.adjacent(R) && enoughResources())
                return Z.give(R.x-Z.me.x,R.y-Z.me.y,Z.me.karbonite,Z.me.fuel);
        int x = Z.getClosestStruct(true);
        return moveToward(Z.fdiv(x,64),x%64);
    }

    public Action moveTowardCastle() {
        while (Z.otherCastle.size() > 0) {
            int y = Z.otherCastle.get(0) % 64;
            int x = (Z.otherCastle.get(0) - y) / 64;
            if (Z.robotMap[y][x] == 0) {
                Z.otherCastle.remove(0);
                continue;
            }
            return nextMove(x, y);
        }
        while (Z.otherChurch.size() > 0) {
            int y = Z.otherChurch.get(0) % 64;
            int x = (Z.otherChurch.get(0) - y) / 64;
            if (Z.robotMap[y][x] == 0) {
                Z.otherChurch.remove(0);
                continue;
            }
            return nextMove(x, y);
        }
        return null;
    }
}