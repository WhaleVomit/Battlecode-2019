package bc19;

import static constants;

public class Crusader {

	public static Action tryAttack(Robot r) {
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy)
                if (r.canAttack(dx, dy))
                    return attack(dx, dy);
        return null;
    }

	public static Action moveTowardEnemy(Robot r) {
        Robot R = closestEnemy(); if (R == null) return null;
        int t = closeEmpty(R.x,R.y); if (t == -1) return null;
        return nextMove((t-(t%64))/64,t%64);
    }

	public static Action moveTowardCastle(Robot r) {
		int x = otherCastle.get(0) / 64;
        int y = otherCastle.get(0) % 64;
        return r.nextMove(x, y);
    }

	public static Action run(Robot r) {
		Action A = tryAttack(r);
		if (A == null) A = moveTowardEnemy(r);
		if (A == null) A = moveTowardCastle(r);
		if (A == null) A = r.someMove();
		return A;
        /*const choices = [[0,-1], [1, -1], [1, 0], [1, 1], [0, 1], [-1, 1], [-1, 0], [-1, -1]];
        const choice = choices[Math.floor(Math.random()*choices.length)];
        return this.move(...choice);*/
    }
}
