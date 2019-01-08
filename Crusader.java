package bc19;
import java.util.*;
import java.math.*;

public class Crusader extends BCAbstractRobot {
	MyRobot myRobot;
	Global g;
	
	public Crusader(MyRobot k) {
		this.myRobot = k;
		g = new Global(myRobot);
	}
	
	public Action run() {
        Action A = g.tryAttack();
        if (A != null) return A;
        A = g.moveTowardEnemy();
        if (A != null) return A;
        A = g.moveTowardCastle();
        if (A != null) return A;
        return g.nextMove(g.closestUnseen());
    }
}