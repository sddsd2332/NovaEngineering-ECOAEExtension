package github.kasuminova.ecoaeextension.common.util;

@FunctionalInterface
public interface BiFunction2Bool<T, U> {

    boolean apply(T t, U u);

}
