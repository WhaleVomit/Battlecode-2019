package bc19;

import static constants;

public class Castle {

	public static Action tryBuild(Robot r, int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (available(me.x + dx, me.y + dy))
                        return buildUnit(type, dx, dy);
        return null;
    }

	public static Action run(Robot r) {
        /*if (turn == 1) {
            log("Building a pilgrim.");
            return buildUnit(SPECS.PILGRIM,1,0);
        }*/

        if (r.turn % 10 == 0) {
            Action A = tryBuild(r, CRUSADER);
            if (A != null) {
                log("Built crusader");
                return A;
            }
        }

        // this.log("Building a crusader at " + (this.me.x+1) + ", " + (this.me.y+1));
        // return this.buildUnit(SPECS.CRUSADER, 1, 1);
        //return this.log("Castle health: " + this.me.health);

        return null;

    }
}
