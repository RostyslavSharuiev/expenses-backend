package rostyslav.sharuiev.expenses.authentication.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.authentication.application.port.in.RefreshAuthenticationUseCase;
import rostyslav.sharuiev.expenses.authentication.application.port.out.AccessTokenProviderPort;
import rostyslav.sharuiev.expenses.session.application.exception.RefreshTokenReuseException;
import rostyslav.sharuiev.expenses.session.application.port.in.RotateRefreshTokenUseCase;

@Service
@RequiredArgsConstructor
public class RefreshAuthenticationService implements RefreshAuthenticationUseCase {

    private final RotateRefreshTokenUseCase rotateRefreshTokenUseCase;
    private final AccessTokenProviderPort accessTokenProviderPort;

    @Override
    @Transactional(noRollbackFor = RefreshTokenReuseException.class)
    public RefreshAuthenticationResult refresh(String rawRefreshToken) {
        var rotationResult = rotateRefreshTokenUseCase.rotateRefreshToken(rawRefreshToken);
        var accessToken = accessTokenProviderPort.generate(rotationResult.userId());

        return new RefreshAuthenticationResult(
                rotationResult.userId(),
                accessToken.value(),
                rotationResult.refreshToken(),
                accessToken.expiresIn()
        );
    }
}
