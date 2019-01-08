package bc19;

public class Preacher extends Globals {
	boolean canAttack(int dx, int dy) {
        int x = me.x+dx, y = me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        int dist = dx*dx+dy*dy;
        if (dist < 1 || dist > 4) return false;
        return true;
    }
	
	Action run() {
        return null;
    }
}