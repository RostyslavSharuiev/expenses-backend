package rostyslav.sharuiev.expenses.auth.exception;

public class RefreshTokenReuseException extends RuntimeException {

    public RefreshTokenReuseException(String message) {
        super(message);
    }
}
