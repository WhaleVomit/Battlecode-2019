package bc19;

import static bc19.Consts.*;

public class Movable {
    public MyRobot Z;
    public Movable (MyRobot z) { Z = z; }

    public boolean enoughResources() {
        return Z.ME.fuel > 25 || (Z.ME.fuel > 0 && !Z.fuelMap[Z.ME.y][Z.ME.x]) 
        	|| Z.ME.karbonite > 5 || (Z.ME.karbonite > 0 && !Z.karboniteMap[Z.ME.y][Z.ME.y]);
    }
    public boolean canMove(Robot2 r, int dx, int dy) {
        if (dx == 0 && dy == 0) return false;
        return Z.withinMoveRadius(r, dx, dy) && Z.passable(r.x+dx,r.y+dy);
    }
    public Action nextMove(int x, int y) {
        if (Z.nextMove[y][x] == MOD) return null;
        if (Z.bfsDist[y][x] == 1 && !Z.passable(x,y)) return null;
        int Y = Z.nextMove[y][x] % 64, X = Z.fdiv(Z.nextMove[y][x],64);
        return Z.move(X-Z.ME.x, Y-Z.ME.y);
    }
    public Action nextMove(int x) { return x == MOD ? null : nextMove(Z.fdiv(x,64), x % 64); }
    public Action moveToward(int x, int y) { return nextMove(Z.closeEmpty(x, y)); }
    public Action moveToward(int x) { return moveToward(x%64,Z.fdiv(x,64)); }
    public Action moveToward(Robot2 R) { return R == null ? null : moveToward(R.x, R.y); }
    public Action moveAway(int x, int y) {
        int farthest = -MOD; Action best = null;
        for (int i = -3; i <= 3; i++) for (int j = -3; j <= 3; j++)
            if (Z.passable(Z.ME.x + i, Z.ME.y + j) && Z.withinMoveRadius(Z.ME, i, j)) {
                int dis = Z.sq(x - Z.ME.x - i) + Z.sq(y - Z.ME.y - j);
                if (dis > farthest) { farthest = dis; best = Z.move(i, j); }
            }
        return best;
    }
    public Action moveAway(Robot2 R) { return R == null ?  null : moveAway(R.x, R.y); }
    public Action moveHome() {
        for (Robot2 R: Z.robots)
            if (R.isStructure() && R.team == Z.ME.team && Z.adjacent(R) && enoughResources()) {
                Z.resource = -1;
                return Z.give(R.x-Z.ME.x,R.y-Z.ME.y,Z.ME.karbonite,Z.ME.fuel);
            }
        return moveToward(Z.closestStruct(true));
    }
    public Action moveEnemy() { return moveToward(Z.closestStruct(false)); }
}