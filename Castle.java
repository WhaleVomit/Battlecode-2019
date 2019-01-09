package bc19;

import static bc19.Consts.*;

public class Castle {
    MyRobot Z;

    public Castle(MyRobot z) {
        this.Z = z;
    }

    void determineLoc() {
        for (Robot R: Z.robots) if (R.castle_talk > 0 && R.castle_talk <= 64) 
            Z.castleX.put(R.id,R.castle_talk-1);

        for (Robot R: Z.robots) if (R.castle_talk > 64 && R.castle_talk <= 128) {
            if (Z.castleY.get(R.id) == null) {
                Z.castleY.put(R.id,R.castle_talk-65);
                Z.myCastle.add(64*Z.castleX.get(R.id)+Z.castleY.get(R.id));
            }
        }

        if (Z.me.turn == 1) Z.castleTalk(Z.me.x+1);
        else if (Z.me.turn == 2) Z.castleTalk(64+Z.me.y+1);
        else if (Z.me.turn == 3) Z.castleTalk(0);

        /*if (Z.turn <= 3) {
            log("HA: "+Z.turn);
            for (Robot R: Z.robots) log(Z.getInfo(R));
            for (int R: Z.myCastle) log("HUH "+R);
        }*/
    }

    Action run() {
        determineLoc();
        /*
        String S = ""; S += getInfo(me);
        for (Robot R: robots) S += getInfo(R);

        log(S);*/
        if (2 * Z.numPilgrims <= Z.numAttack) {
            if (Z.canBuild(PILGRIM)) {
                Action A = Z.tryBuild(PILGRIM);
                if (A != null) {
                    Z.numPilgrims++;
                    Z.log("Built pilgrim");
                    return A;
                }
            }
        } else {
            if (Z.canBuild(PROPHET)) {
                Action A = Z.tryBuild(PROPHET);
                if (A != null) {
                    Z.numAttack++;
                    Z.log("Built prophet");
                    return A;
                }
            }
            /*if (Z.canBuild(CRUSADER)) {
                Action A = Z.tryBuild(CRUSADER);
                if (A != null) {
                    Z.numAttack ++;
                    Z.log("Built crusader");
                    return A;
                }
            }*/
        }
        return null;

    }
}
