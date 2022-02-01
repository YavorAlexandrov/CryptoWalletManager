package bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }
}
