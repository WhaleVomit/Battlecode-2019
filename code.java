package bc19;

public class MyRobot extends BCAbstractRobot {
    public int turn;
    public Robot[] robots;
    int[][] robotMap;

    boolean valid (int x, int y) {
        if (!(0 <= x && x < robotMap.length && 0 <= y && y < robotMap[0].length)) return false;
        return map[x][y];
    }

    public boolean isEmpty(int x, int y) {
        return valid(x,y) && robotMap[x][y] != 0;
    }

    public boolean canMove(Robot r, int dx, int dy) {
        int x = r.x+dx, y = r.y+dy;
        return isEmpty(x,y);
    }

    public void tryBuild(int type) {
        for (int dx = -1; dx <= 1; ++dx)
            for (int dy = -1; dy <= 1; ++dy)
                    if (isEmpty(me.x+dx,me.y+dy))
                        return buildUnit(type,);

    }

    public Action runCastle() {
        /*if (turn == 1) {
            log("Building a pilgrim.");
            return buildUnit(SPECS.PILGRIM,1,0);
        }*/

        if (turn % 10 == 0) {
            Action A = tryBuild(SPECS.CRUSADER);
            if (A != null) return A;

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
        return move(0,1);
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