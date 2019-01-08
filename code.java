package bc19;

public class MyRobot extends BCAbstractRobot {
    public int turn;
    public Robot[] robots;
    int[][] robotMap;

    public boolean valid (int x, int y) {
        if (!(0 <= x && x < robotMap.length && 0 <= y && y < robotMap[0].length)) return false;
        return map[x][y];
    }

    public boolean isNotEmpty (int x, int y) {
        return valid(x,y) && robotMap[x][y] != 0;
    }

    public boolean isEmpty(int x, int y) {
        return valid(x,y) && robotMap[x][y] == 0;
    }

    public int moveDist(Robot r) {
        if (r.unit == SPECS.CRUSADER) return 9;
        return 4;
    }

    public boolean canMove(Robot r, int dx, int dy) {
        if (moveDist(r) < dx*dx+dy*dy) return false;
        int x = r.x+dx, y = r.y+dy;
        return isEmpty(x,y);
    }

    public boolean canAttack(int dx, int dy) {
        int x = me.x+dx, y = me.y+dy;
        if (!isNotEmpty(x,y)) return false;

        int dist = dx*dx+dy*dy;
        if (me.unit == SPECS.CRUSADER) {
            if (getRobot(robotMap[x][y]).team == me.team) return false;
            if (dist < 1 || dist > 4) return false;
        } else if (me.unit == SPECS.PROPHET) {
            if (getRobot(robotMap[x][y]).team == me.team) return false;
            if (dist < 4 || dist > 8) return false;
            return true;
        } else if (me.unit == SPECS.PREACHER) {
            if (dist < 1 || dist > 4) return false;
            return true;
        }
        return false;
    }

    public Action tryAttack() {
        for (int dx = -2; dx <= 2; ++dx)
            for (int dy = -2; dy <= 2; ++dy) 
                if (canAttack(dx,dy)) 
                    return attack(dx,dy);
        return null;
    }

    public Action someMove() {
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy) 
                if (canMove(me,dx,dy)) 
                    return move(dx,dy);
        return null;
    }
    
    public Action tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (isEmpty(me.x+dx,me.y+dy))
                        return buildUnit(type,dx,dy);
        return null;
    }

    public Action runCastle() {
        /*if (turn == 1) {
            log("Building a pilgrim.");
            return buildUnit(SPECS.PILGRIM,1,0);
        }*/

        if (turn % 10 == 0) {
            log("HI");
            Action A = tryBuild(SPECS.CRUSADER);
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

    public Action runChurch() {
        return null;

    }
    
    public Action runPilgrim() {
        return null;
        /*if (turn == 1) {
            log("I am a pilgrim.");
             
            //log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
        }*/

    }

    public Action runCrusader() {
        Action A = tryAttack();
        if (A != null) return A;
        return someMove();
        /*const choices = [[0,-1], [1, -1], [1, 0], [1, 1], [0, 1], [-1, 1], [-1, 0], [-1, -1]];
        const choice = choices[Math.floor(Math.random()*choices.length)];
        return this.move(...choice);*/

    }

    public Action runProphet() {
        return null;

    }

    public Action runPreacher() {
        return null;
    }

    public Action turn() {
        turn++;
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();

        switch(me.unit) {
            case 0:
                return runCastle();
            case 1:
                return runChurch();
            case 2:
                return runPilgrim();
            case 3:
                return runCrusader();
            case 4:
                return runProphet();
            case 5:
                return runPreacher();
        }

    }
}