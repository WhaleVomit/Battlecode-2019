package bc19;

public class Castle extends MyRobot {
	MyRobot Z;
	
	public Castle(MyRobot z) {
		this.Z = z;
	}

    Action run() {
        if (Z.numCastles == 0) Z.numCastles = Math.min(3,Z.robots.length);

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
        if (Z.numPilgrims < 6/Z.numCastles) {
            if (Z.canBuild(PILGRIM)) {
                Action A = Z.tryBuild(PILGRIM);
                if (A != null) {
                    Z.numPilgrims ++;
                    Z.log("Built pilgrim");
                    return A;
                }
            }
        } else {
            if (Z.canBuild(CRUSADER)) {
                Action A = Z.tryBuild(CRUSADER);
                if (A != null) {
                    Z.log("Built crusader");
                    return A;
                }
            }
        }
        return null;

    }
}
