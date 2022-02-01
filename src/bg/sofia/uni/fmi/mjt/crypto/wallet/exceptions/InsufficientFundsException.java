package bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable e) {
        super(message, e);
    }
}
