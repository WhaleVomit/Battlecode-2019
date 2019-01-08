package bc19;

public class MyRobot extends BCAbstractRobot {
	int turn = 0;
	public Action turn() {
		if (this.me.unit == SPECS.CRUSADER) {
            //this.log("Crusader health: " + this.me.health);
            return this.move(0,1);
        }

        else if (this.me.unit == SPECS.CASTLE) {
            if (turn % 10 == 0) {
                this.log("Building a crusader at " + (this.me.x+1) + ", " + (this.me.y+1));
                return this.buildUnit(SPECS.CRUSADER, 1, 1);
            } else {
                //return this.log("Castle health: " + this.me.health);
            }
        }
		turn++;
    }
}