package bc19;

public class Globals extends BCAbstractRobot {
    Robot[] robots;
    int[][] robotMap;
    int turn;

    boolean valid (int x, int y) {
        if (!(0 <= x && x < robotMap.length && 0 <= y && y < robotMap[0].length)) return false;
        return map[x][y];
    }

    boolean isNotEmpty (int x, int y) {
        return valid(x,y) && robotMap[x][y] != 0;
    }

    boolean isEmpty(int x, int y) {
        return valid(x,y) && robotMap[x][y] == 0;
    }

    int moveDist(Robot r) {
        if (r.unit == SPECS.CRUSADER) return 9;
        return 4;
    }

    boolean canMove(Robot r, int dx, int dy) {
        if (moveDist(r) < dx*dx+dy*dy) return false;
        int x = r.x+dx, y = r.y+dy;
        return isEmpty(x,y);
    }

    Action someMove() {
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy) 
                if (canMove(me,dx,dy)) 
                    return move(dx,dy);
        return null;
    }
}