package bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions;

public class ApiKeyException extends CryptoHttpClientException {
    public ApiKeyException(String message) {
        super(message);
    }

    public ApiKeyException(String message, Throwable e) {
        super(message, e);
    }
}
