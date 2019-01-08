package bc19;

public class Prophet extends BCAbstractRobot {
	MyRobot myRobot;
	Globals glo;
	
	public Prophet(MyRobot k) {
		this.myRobot = k;
		glo = new Globals(myRobot);
	}
	
	public Action run() {
        return null;
    }
}
