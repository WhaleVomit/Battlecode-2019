package bc19;

import static bc19.Consts.*;

public class Castle extends Building {
    public Castle(MyRobot z) { super(z); }

    /*boolean canSee(Robot2 A, Robot2 B) {
        int dist = (A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);
    }*/

    int getSignal(Robot2 R) {
        return 1001+21*(R.x-Z.me.x+10)+(R.y-Z.me.y+10);
    }

    boolean clearVision(Robot2 R) {
        if (Z.fdiv(R.castle_talk,6) % 6 == 1) return false;
        for (int i = -10; i <= 10; ++i)
            for (int j = -10; j <= 10; ++j) {
                if (i*i+j*j > VISION_R[R.unit]) continue;
                int x = R.x+i, y = R.y+j;
                if (Z.containsRobot(x,y) && Z.robotMap[y][x] > 0 && Z.seenRobot[y][x].team != Z.me.team) return false;
            }
        return true;
    }

    void warnDefenders() {
        Robot2 R = Z.closestEnemy(); if (R == null) return;
        int needDist = 0;
        for (int i = -4; i <= 4; ++i) for (int j = -4; j <= 4; ++j) if (i*i+j*j <= 16) {
            int x = Z.me.x+i, y = Z.me.y+j;
            if (!Z.containsRobot(x,y)) continue;
            Robot2 A = Z.seenRobot[y][x];
            if (A.unit > 2 && A.team == Z.me.team && clearVision(A)) needDist = Math.max(needDist,i*i+j*j);
        }
        if (needDist > 0) {
            Z.log("SIGNAL ENEMY: "+Z.me.turn+" "+Z.me.x+" "+Z.me.y+" "+R.x+" "+R.y+" "+getSignal(R));
            Z.signal(getSignal(R),needDist);
        }
    }

    void determineLoc() {
        if (Z.me.turn > 3) return;
        
        for (Robot2 R: Z.robots) if (R.castle_talk > 0 && R.castle_talk <= 64)
            Z.castleX.put(R.id,R.castle_talk-1);

        for (Robot2 R: Z.robots) if (R.castle_talk > 64 && R.castle_talk <= 128) {
            if (Z.castleY.get(R.id) == null) {
                Z.castleY.put(R.id,R.castle_talk-65);
                int t = 64*Z.castleX.get(R.id)+Z.castleY.get(R.id);
                if (!Z.myCastle.contains(t)) {
                    Z.myCastle.add(t);
                    if (Z.wsim()) Z.otherCastle.add(64*(Z.w-1-Z.castleX.get(R.id))+Z.castleY.get(R.id));
                    if (Z.hsim()) Z.otherCastle.add(64*Z.castleX.get(R.id)+(Z.h-1-Z.castleY.get(R.id)));
                }
            }
        }

        if (Z.me.turn == 1) Z.castleTalk(Z.me.x+1);
        else if (Z.me.turn == 2) Z.castleTalk(64+Z.me.y+1);
        else if (Z.me.turn == 3) Z.castleTalk(0);
    }

    boolean shouldMakePilgrim() {
        return 2 * Z.numPilgrims <= Z.numAttack;
    }
    
    Action build() {
        if (shouldMakePilgrim()) {
            Action A = makePilgrim();
            if (A != null) return A;
        } else if (Z.turn <= 20) {
            if (Z.canBuild(PREACHER)) {
                Action A = Z.tryBuild(PREACHER);
                if (A != null) {
                    Z.numAttack ++;
                    Z.log("Built preacher");
                    return A;
                }
            }
            if (Z.canBuild(CRUSADER)) {
                Action A = Z.tryBuild(CRUSADER);
                if (A != null) {
                    Z.numAttack ++;
                    Z.log("Built crusader");
                    return A;
                }
            }
        } else {
            boolean canTake = Z.fuel >= CONSTRUCTION_F[CHURCH] + CONSTRUCTION_F[PROPHET] && Z.karbonite >= CONSTRUCTION_K[CHURCH] + CONSTRUCTION_K[PROPHET];
            if((Z.numAttack < 6 && Z.canBuild(PROPHET)) || canTake) {
                Action A = Z.tryBuild(PROPHET);
                if (A != null) {
                    Z.numAttack++;
                    Z.log("Built prophet");
                    return A;
                }
            }
        }
        return null;
    }

    Action testPreacherDefense() {
        if (shouldMakePilgrim()) {
            Action A = makePilgrim();
            if (A != null) return A;
        } else {
            if (Z.me.team == 0) {
                if (Z.canBuild(PREACHER)) {
                    Action A = Z.tryBuild(PREACHER);
                    if (A != null) {
                        Z.numAttack ++;
                        Z.log("Built preacher");
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
            }
        }
        return null;
    }

    Action run() {
        determineLoc();
        warnDefenders();
        return testPreacherDefense();
        // return build();

    }
}
