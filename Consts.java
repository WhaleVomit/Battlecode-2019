package bc19;

public class Consts {
    public static final int INF = Integer.MAX_VALUE;
    public static final int MOD = 1000000007;

    public static final int CASTLE = 0;
    public static final int CHURCH = 1;
    public static final int PILGRIM = 2;
    public static final int CRUSADER = 3;
    public static final int PROPHET = 4;
    public static final int PREACHER = 5;

    public static final int RED = 0;
    public static final int BLUE = 1;

    public static final int[] CONTRUCTION_K = {-1, -1, 10, 20, 25, 30};
    public static final int CONTRUCTION_F = 50;

    public static final int K_CARRY_CAP = 20;
    public static final int F_CARRY_CAP = 100;

    public static final boolean[] CAN_MOVE = {false, false, true, true, true};
    public static final int[] MOVE_SPEED = {-1, -1, 4, 9, 4, 4};
    public static final int[] MOVE_F_COST = {-1, -1, 1, 1, 2, 3};

    public static final int[] START_HEALTH = {-1, -1, 10, 40, 20, 60};

    public static final int[] VISION_R = {-1, -1, 10, 6, 8, 4};

    public static final int[] DAMAGE = {-1, -1, 10, 10, 20};

    public static final boolean[] CAN_ATTACK = {false, false, false, true, true, true};
    public static final int[] MIN_ATTACK_R = {-1, -1, -1, 1, 4, 1};
    public static final int[] MAX_ATTACK_R = {-1, -1, -1, 4, 64, 4};
    public static final int[] ATTACK_F_COST = {-1, -1, -1, 10, 25, 15};
}
