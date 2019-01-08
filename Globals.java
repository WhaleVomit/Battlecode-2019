package bc19;

import static constants;

public class Globals extends Constants {

    int w, h;
    Robot[] robots;
    int[][] robotMap, dist, pre;
    ArrayList<Integer> myCastle = new ArrayList<>();
    ArrayList<Integer> otherCastle = new ArrayList<>();

    private boolean valid(int x, int y) {
        return x >= 0 && x < w && y >= 0 && y <h && map[y][x];
    }

    private boolean unavailable(int x, int y) {
        return valid(x,y) && robotMap[y][x] != 0;
    }

    private boolean available(int x, int y) {
        return valid(x,y) && robotMap[y][x] == 0;
    }

    private int moveDist() {
        return MOVE_SPEED[me.unit];
    }

    private boolean withinMoveRadius(int dx, int dy) {
        return dx * dx + dy * dy <= moveDist();
    }

    private boolean canMove(int dx, int dy) {
        return withinMoveRadius(dx, dy) && available(me.x + dx, me.y + dy);
    }

    private Action someMove() {
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy)
                if (me.canMove(dx, dy))
                    return move(dx, dy);
        return null;
    }

    private boolean canAttack(int dx, int dy) {
        if(!CAN_ATTACK[me.unit]) return false;

        int x = me.x + dx, y = me.y + dy;
        if (unavailable(x, y)) return false;
        if (getRobot(robotMap[y][x]).team == me.team) return false;

        int dist = dx * dx + dy * dy;
        return dist >= MIN_ATTACK_R[me.unit] && dist <= MAX_ATTACK_R[me.unit];
    }

    private Action nextMove(int x, int y) {
        if (pre[y][x] == MOD) return null;
        int X = pre[y][x] / 64; int Y = pre[y][x] % 64;
        return move(X - me.x, Y - me.y);
    }
}
