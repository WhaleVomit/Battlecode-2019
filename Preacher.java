package bc19;

public class Preacher extends BCAbstractRobot {
	MyRobot myRobot;
	Globals glo;
	
	public Preacher(MyRobot k) {
		this.myRobot = k;
		glo = new Globals(myRobot);
	}

	public Action run() {
        return null;
    }
	
}
