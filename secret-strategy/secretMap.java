package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class secretMap extends moveMap {
  public secretMap(MyRobot Z, int mx) { super(Z,mx); }
	boolean ok(int x, int y) {
    return Z.passable(x,y) && Z.danger[y][x] == 0;
    // return true;
  }
}
