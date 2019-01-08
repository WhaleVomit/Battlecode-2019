package bc19;

public class Castle extends BCAbstractRobot {
	MyRobot myRobot;
	Globals glo;
	
	public Castle(MyRobot k) {
		this.myRobot = k;
		glo = new Globals(myRobot);
	}

	public Action tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (glo.available(myRobot.me.x + dx, myRobot.me.y + dy))
                        return myRobot.buildUnit(type, dx, dy);
        return null;
    }

	public Action run() {
        /*if (turn == 1) {
            log("Building a pilgrim.");
            return buildUnit(SPECS.PILGRIM,1,0);
        }*/

        if (glo.turn % 10 == 0) {
            Action A = tryBuild(glo.CRUSADER);
            if (A != null) {
                myRobot.log("Built crusader");
                return A;
            }
        }

        // this.log("Building a crusader at " + (this.me.x+1) + ", " + (this.me.y+1));
        // return this.buildUnit(SPECS.CRUSADER, 1, 1);
        //return this.log("Castle health: " + this.me.health);

        return null;

    }
}
