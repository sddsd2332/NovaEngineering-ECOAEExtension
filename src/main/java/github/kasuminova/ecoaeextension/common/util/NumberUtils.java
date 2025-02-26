package github.kasuminova.ecoaeextension.common.util;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class NumberUtils {

    public static OptionalInt tryParseInt(String str) {
        try {
            return OptionalInt.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static OptionalLong tryParseLong(String str) {
        try {
            return OptionalLong.of(Long.parseLong(str));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    public static OptionalDouble tryParseDouble(String str) {
        try {
            return OptionalDouble.of(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    public static boolean canParse(String str) {
        return tryParseInt(str).isPresent() ||
                tryParseLong(str).isPresent() ||
                tryParseDouble(str).isPresent();
    }

}
