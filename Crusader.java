package bc19;

public class Crusader extends BCAbstractRobot {
	MyRobot myRobot;
	Globals glo;
	
	public Crusader(MyRobot k) {
		this.myRobot = k;
		glo = new Globals(myRobot);
	}

	public Action tryAttack() {
        for (int dx = -3; dx <= 3; ++dx)
            for (int dy = -3; dy <= 3; ++dy)
                if (glo.canAttack(dx, dy))
                    return myRobot.attack(dx, dy);
        return null;
    }

	public Action run() {
		Action A = tryAttack();
		if (A == null) A = glo.moveTowardEnemy();
		if (A == null) A = glo.moveTowardCastle();
		if (A == null) A = glo.someMove();
		return A;
        /*const choices = [[0,-1], [1, -1], [1, 0], [1, 1], [0, 1], [-1, 1], [-1, 0], [-1, -1]];
        const choice = choices[Math.floor(Math.random()*choices.length)];
        return this.move(...choice);*/
    }
}
