package bc19;
import java.util.*;
import java.math.*;

public class Preacher extends BCAbstractRobot {
	MyRobot myRobot;
	Global g;
	
	public Preacher(MyRobot k) {
		this.myRobot = k;
		g = new Global(myRobot);
	}
	
	public Action run() {
        return null;
    }
}