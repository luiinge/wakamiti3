package es.iti.wakamiti.api.lang;

@FunctionalInterface
public interface ThrowableRunnable {

    void run(Object... arguments) throws Exception;

}
