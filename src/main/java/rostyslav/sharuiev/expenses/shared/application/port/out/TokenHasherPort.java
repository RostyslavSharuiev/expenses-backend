package rostyslav.sharuiev.expenses.shared.application.port.out;

public interface TokenHasherPort {

    String hash(String rawToken);
}
