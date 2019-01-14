package bc19;

import static bc19.Consts.*;

public class Building {
    public MyRobot Z;
	public Building(MyRobot z) { Z = z; }
    int decideUnit() {
		int[] cnt = new int[6];
		for(int dx = -10; dx <= 10; dx++) {
			for(int dy = -10; dy <= 10; dy++) if(dx*dx + dy*dy <= VISION_R[Z.CUR.unit]) {
				int x = Z.CUR.x + dx; int y = Z.CUR.y + dy;
				if(!Z.valid(x,y)) continue;
				if(Z.robotMapID[y][x] > 0) {
					Robot2 R = Z.robotMap[y][x];
					if(R.team != Z.CUR.team) cnt[R.unit]++;
				}
			}
		}
		int crus = 2*cnt[PROPHET]; if(!Z.canBuild(CRUSADER)) crus = 0;
		int proph = 2*cnt[PREACHER] + 2*cnt[CRUSADER]; if(!Z.canBuild(PROPHET)) proph = 0;
		int preach = cnt[CASTLE] + cnt[CHURCH] + cnt[PILGRIM] + 2*cnt[CRUSADER]; if(!Z.canBuild(PREACHER)) preach = 0;
		
		if(crus + proph + preach == 0) return MOD;
		if(cnt[CRUSADER]+cnt[PROPHET]+cnt[PREACHER] == 0 && Z.euclidDist(Z.closestAttacker(Z.CUR,Z.CUR.team)) <= 2) return MOD;
		if(crus >= proph && crus >= preach) return CRUSADER;
		if(proph >= crus && proph >= preach) return PROPHET;
		return PREACHER;
	}
    
    Action2 panicBuild() {
		int w = decideUnit();
		if(w == MOD) return null;
		if(Z.me.unit == CASTLE) return Z.tryBuild(w);
		else return Z.tryBuildNoSignal(w);
	}
}
