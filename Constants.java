public class Constants extends BCAbstractRobot {

    final int INF = Integer.MAX_VALUE;

    final int CASTLE = 0;
    final int CHURCH = 1;
    final int PILGRIM = 2;
    final int CRUSADER = 3;
    final int PROPHET = 4;
    final int PREACHER = 5;

    final int RED = 0;
    final int BLUE = 1;

    final int[] CONTRUCTION_K = new int[6]{-1, -1, 10, 20, 25, 30};
    final int CONTRUCTION_F = 50;

    final int K_CARRY_CAP = 20;
    final int F_CARRY_CAP = 100;

    final boolean[] CAN_MOVE = new int[6]{false, false, true, true, true};
    final int[] MOVE_SPEED = new int[6]{-1, -1, 4, 9, 4, 4};
    final int[] MOVE_F_COST = new int [6]{-1, -1, 1, 1, 2, 3};

    final int[] START_HEALTH = new int[6]{-1, -1, 10, 40, 20, 60};

    final int[] VISION_R = new int[6]{-1, -1, 10, 6, 8, 4};

    final int[] DAMAGE = new int[6]{-1, -1, 10, 10, 20};

    final boolean[] CAN_ATTACK = new int[6]{false, false, false, true, true, true};
    final int[] MIN_ATTACK_R = new int[6]{-1, -1, -1, 1, 4, 1};
    final int[] MAX_ATTACK_R = new int[6]{-1, -1, -1, 4, 8, 4};
    final int[] ATTACK_F_COST = new int[6]{-1, -1, -1, 10, 25, 15};

}
