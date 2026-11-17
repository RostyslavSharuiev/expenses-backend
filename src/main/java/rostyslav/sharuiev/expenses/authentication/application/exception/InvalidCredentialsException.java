package rostyslav.sharuiev.expenses.authentication.application.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
