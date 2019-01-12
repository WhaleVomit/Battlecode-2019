package bc19;

import static bc19.Consts.*;

public class Preacher extends Attackable {
    public Preacher(MyRobot z) { super(z); }
    Action run() { 
        Z.sendToCastle();
    	return aggressive(); 
   	}
}
