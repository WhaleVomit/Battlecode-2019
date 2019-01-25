package bc19;

import static bc19.Consts.*;

public class Movable {
    public MyRobot Z;
    public Movable (MyRobot z) { Z = z; }

    public boolean containsConfident(int x, int y) {
        for (int i = x-1; i <= x+1; ++i) for (int j = y-1; j <= y+1; ++j)
            if (Z.enemyRobot(i,j) && Z.lastTurn[j][i] >= Z.CUR.turn-1) return true;
        return false;
    }

    Action2 tryGive() {
        for (Robot2 R: Z.robots)
            if (R.isStructure() && R.team == Z.CUR.team && Z.adjacent(Z.CUR,R) && enoughResources()) {
                Z.resource = -1;
                return Z.giveAction(R.x-Z.CUR.x,R.y-Z.CUR.y,Z.CUR.karbonite,Z.CUR.fuel);
            }
        return null;
    }
    boolean enoughResources() {
      if (Z.CUR.unit != PILGRIM || Z.danger[Z.CUR.y][Z.CUR.x] == 0)
        return Z.CUR.fuel > 25 || (Z.CUR.fuel > 0 && !Z.fuelMap[Z.CUR.y][Z.CUR.x])
        	|| Z.CUR.karbonite > 5 || (Z.CUR.karbonite > 0 && !Z.karboniteMap[Z.CUR.y][Z.CUR.x]);
      return Z.CUR.fuel > 25 || Z.CUR.karbonite > 5;
    }

    Action2 moveClose(int x, int y) {
        int bestDist = Z.euclidDist(Z.CUR,x,y); Action2 bestMove = null;
        for (int i = -3; i <= 3; ++i) for (int j = -3; j <= 3; ++j)
            if (Z.canMove(Z.CUR,i,j)) {
                int dist = Z.sq(Z.CUR.x+i-x)+Z.sq(Z.CUR.y+j-y);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestMove = Z.moveAction(i,j);
                }
            }
        return bestMove;
    }
    Action2 moveAway(int x, int y) {
        int farthest = Z.euclidDist(Z.CUR,x,y); Action2 best = null;
        for (int i = -3; i <= 3; i++) for (int j = -3; j <= 3; j++)
            if (Z.canMove(Z.CUR,i,j)) {
                int dis = Z.sq(x - Z.CUR.x - i) + Z.sq(y - Z.CUR.y - j);
                if (dis > farthest) {
                  farthest = dis;
                  best = Z.moveAction(i, j);
                }
            }
        return best;
    }
    Action2 moveAway(Robot2 R) { return R == null ?  null : moveAway(R.x, R.y); }

    Action2 goHome() {
        Action2 A = tryGive(); if (A != null) return A;
        if (Z.CUR.unit == PILGRIM) return Z.safe.moveYourStruct();
        return Z.bfs.moveYourStruct();
    }
}
