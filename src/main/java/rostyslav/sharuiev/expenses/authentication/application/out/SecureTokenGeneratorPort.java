package rostyslav.sharuiev.expenses.authentication.application.out;

public interface SecureTokenGeneratorPort {

    String generateToken();

    // TODO: check later
//    String hashToken(String rawToken);
}
