package bc19;

public class MyRobot extends BCAbstractRobot {
    public int turn;

    public Action runCastle() {
        /*if (turn == 1) {
            log("Building a pilgrim.");
            return buildUnit(SPECS.PILGRIM,1,0);
        }*/

        if (turn % 10 == 0) {
            this.log("Building a crusader at " + (this.me.x+1) + ", " + (this.me.y+1));
            return this.buildUnit(SPECS.CRUSADER, 1, 1);
        } else {
            return null;
            //return this.log("Castle health: " + this.me.health);
        }

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
        return null;
        // return this.move(0,1);
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