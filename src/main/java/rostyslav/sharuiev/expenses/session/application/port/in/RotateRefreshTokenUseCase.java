package rostyslav.sharuiev.expenses.session.application.port.in;

public interface RotateRefreshTokenUseCase {

    String rotateRefreshToken(String rawRefreshToken);
}
