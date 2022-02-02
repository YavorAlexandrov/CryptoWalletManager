package bg.sofia.uni.fmi.mjt.crypto.wallet.exceptions;

public class CryptoHttpClientException extends Exception {
    public CryptoHttpClientException(String message) {
        super(message);
    }

    public CryptoHttpClientException(String message, Throwable e) {
        super(message, e);
    }
}
