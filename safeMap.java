package bc19;

import java.util.*;
import java.math.*;

import static bc19.Consts.*;

public class safeMap extends moveMap {
  public safeMap(MyRobot Z, int mx) { super(Z,mx); }
	boolean ok(int x, int y) {
    return Z.passable(x,y) && !Z.dangerous[y][x];
    // return true;
  }
}
