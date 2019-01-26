package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class secretMap extends moveMap {
  public secretMap(MyRobot Z) { super(Z,2); }
	boolean ok(int x, int y) { return Z.passable(x,y); }
}
