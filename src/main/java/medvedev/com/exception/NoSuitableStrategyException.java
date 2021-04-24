package medvedev.com.exception;

public class NoSuitableStrategyException extends RuntimeException {

    public NoSuitableStrategyException() {
        super("There is no suitable strategy");
    }
}
