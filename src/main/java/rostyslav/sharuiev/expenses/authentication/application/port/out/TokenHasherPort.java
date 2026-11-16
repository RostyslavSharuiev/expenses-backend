package rostyslav.sharuiev.expenses.authentication.application.port.out;

public interface TokenHasherPort {

    String hash(String rawToken);
}
