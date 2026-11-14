package rostyslav.sharuiev.expenses.authentication.application.out;

public interface TokenHasherPort {

    String hash(String rawToken);
}
