package bc19;
import java.util.*;
import java.math.*;

public class Pilgrim extends BCAbstractRobot {
	MyRobot myRobot;
	Global g;
	
	public Pilgrim(MyRobot k) {
		this.myRobot = k;
		g = new Global(myRobot);
	}
	
	public Action run() {
		if (g.resource == -1) g.resource = myRobot.me.id % 2;
        /*String T = me.karbonite+" "+me.fuel+" "+myCastle.size()+ " | ";
        T += closest(myCastle);
        log(T);*/

        if (g.resource == 0) {
            if (myRobot.me.karbonite > 15) return g.returnHome();
            if (g.karboniteMap[myRobot.me.y][myRobot.me.x]) return myRobot.mine();
            return g.nextMove(g.getClosest(g.karboniteMap));
        } else {
            if (myRobot.me.fuel > 75) return g.returnHome();
            if (g.fuelMap[me.y][me.x]) return myRobot.mine();
            return g.nextMove(g.getClosest(g.fuelMap));
        }
    }
}