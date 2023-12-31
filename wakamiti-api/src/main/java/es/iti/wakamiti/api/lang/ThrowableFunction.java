package es.iti.wakamiti.api.lang;

@FunctionalInterface
public interface ThrowableFunction<T,U> {

    U applyThrowing(T value) throws Exception;

    default U apply (T value) {
        try {
            return applyThrowing(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
