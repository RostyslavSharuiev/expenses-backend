package rostyslav.sharuiev.expenses.session.application.port.out;

public interface TokenHasherPort {

    String hash(String rawToken);
}
