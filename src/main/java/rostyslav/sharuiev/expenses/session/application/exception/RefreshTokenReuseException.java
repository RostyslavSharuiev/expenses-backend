package rostyslav.sharuiev.expenses.session.application.exception;

public class RefreshTokenReuseException extends RuntimeException {

    public RefreshTokenReuseException() {
        super("Refresh token reuse detected");
    }
}
