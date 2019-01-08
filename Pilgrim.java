package bc19;

public class Pilgrim extends BCAbstractRobot {
	MyRobot myRobot;
	Globals glo;
	
	public Pilgrim(MyRobot k) {
		this.myRobot = k;
		glo = new Globals(myRobot);
	}

	public Action run() {
        return null;
        /*if (turn == 1) {
            log("I am a pilgrim.");

            //log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
        }*/
    }

}
