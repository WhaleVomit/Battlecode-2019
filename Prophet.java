package bc19;
import java.util.*;
import java.math.*;

public class Prophet extends BCAbstractRobot {
	MyRobot myRobot;
	Global g;
	
	public Prophet(MyRobot k) {
		this.myRobot = k;
		g = new Global(myRobot);
	}
	
	public Action run() {
        return null;
    }
}