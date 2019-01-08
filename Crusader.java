package bc19;

public class Crusader extends MyRobot {
	MyRobot Z;
	
	public Crusader(MyRobot z) {
		this.Z = z;
	}

    Action run() {
        Action A = Z.tryAttack();
        if (A != null) return A;
        A = Z.moveToward(Z.closestEnemy());
        if (A != null) return A;
        A = Z.moveTowardCastle();
        if (A != null) return A;
        return Z.nextMove(Z.closestUnseen());
    }
}
