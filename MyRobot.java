package bc19;

public class MyRobot extends Globals {
    public Action turn() {
        turn++;
        robots = getVisibleRobots();
        robotMap = getVisibleRobotMap();

        switch(me.unit) {
            case 0:
                return Castle.run();
            case 1:
                return Church.run();
            case 2:
                return Pilgrim.run();
            case 3:
                return Crusader.run();
            case 4:
                return Prophet.run();
            case 5:
                return Preacher.run();
        }

    }
}