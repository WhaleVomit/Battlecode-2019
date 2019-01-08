package bc19;

public class Prophet extends Globals {
	boolean canAttack(int dx, int dy) {
        int x = me.x+dx, y = me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        int dist = dx*dx+dy*dy;
        if (getRobot(robotMap[x][y]).team == me.team) return false;
        if (dist < 4 || dist > 8) return false;
        return false;
    }
	
	Action run() {
        return null;

    }
}