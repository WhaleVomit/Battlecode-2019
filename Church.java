package bc19;

import static bc19.Consts.*;

public class Church extends Building {
    public Church(MyRobot z) { super(z); }
    public Action2 run() {
		return panicBuild();
    }
}
