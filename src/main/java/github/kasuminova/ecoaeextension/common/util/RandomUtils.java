package github.kasuminova.ecoaeextension.common.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static boolean nextBool() {
        return current().nextBoolean();
    }

    public static int nextInt(int bound) {
        return current().nextInt(bound);
    }

    public static double nextFloat(float bound) {
        return current().nextFloat() * bound;
    }

    public static float nextFloat() {
        return current().nextFloat();
    }

    public static double nextDouble(double bound) {
        return current().nextDouble(bound);
    }

    public static double nextDouble() {
        return current().nextDouble();
    }

    public static ThreadLocalRandom current() {
        return ThreadLocalRandom.current();
    }

}
