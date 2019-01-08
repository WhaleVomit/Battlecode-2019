package bc19;

public class Church extends BCAbstractRobot {
	MyRobot myRobot;
	Globals glo;
	
	public Church(MyRobot k) {
		this.myRobot = k;
		glo = new Globals(myRobot);
	}

	public Action run() {
        return null;
	}

}
