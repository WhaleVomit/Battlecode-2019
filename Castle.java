package bc19;
import java.util.*;
import java.math.*;

public class Castle extends BCAbstractRobot {
	MyRobot myRobot;
	Global g;
	
	public Castle(MyRobot k) {
		this.myRobot = k;
		g = new Global(myRobot);
	}
	
	public Action run() {
        if (g.numCastles == 0) g.numCastles = Math.min(3,g.robots.length);

        /*if (turn == 1) {
            castleTalk(me.x);
            for (Robot R: robots) M.put(,);
        } 
        if (turn == 2) {
            castleTalk(me.y);

        } else {

        }*/
        /*
        String S = ""; S += getInfo(me);
        for (Robot R: robots) S += getInfo(R);;

        log(S);*/
        if (g.numPilgrims < 6/g.numCastles) {
            if (g.canBuild(myRobot.SPECS.PILGRIM)) {
            		Action A = g.tryBuild(myRobot.SPECS.PILGRIM);
                if (A != null) {
                		g.numPilgrims ++;
                    myRobot.log("Built pilgrim");
                    return A;
                }
            }
        } else {
            if (g.canBuild(myRobot.SPECS.CRUSADER)) {
                Action A = g.tryBuild(myRobot.SPECS.CRUSADER);
                if (A != null) {
                    myRobot.log("Built crusader");
                    return A;
                }
            }
        }
        return null;

    }
}