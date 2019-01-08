package bc19;

public class Crusader extends Globals {
	boolean canAttack(int dx, int dy) {
        int x = me.x+dx, y = me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        int dist = dx*dx+dy*dy;
        if (getRobot(robotMap[x][y]).team == me.team) return false;
        if (dist < 1 || dist > 4) return false;
        return false;
    }
	
	Action tryAttack() {
        for (int dx = -2; dx <= 2; ++dx)
            for (int dy = -2; dy <= 2; ++dy) 
                if (canAttack(dx,dy)) 
                    return attack(dx,dy);
        return null;
    }
	
	Action run() {
        Action A = tryAttack();
        if (A != null) return A;
        return someMove();
        /*const choices = [[0,-1], [1, -1], [1, 0], [1, 1], [0, 1], [-1, 1], [-1, 0], [-1, -1]];
        const choice = choices[Math.floor(Math.random()*choices.length)];
        return this.move(...choice);*/

    }
}