package rostyslav.sharuiev.expenses.authentication.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import rostyslav.sharuiev.expenses.authentication.application.event.PasswordResetEmailRequestedEvent;
import rostyslav.sharuiev.expenses.authentication.application.port.out.EmailSenderPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEmailEventListener {

    private final EmailSenderPort emailSenderPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PasswordResetEmailRequestedEvent event) {
        try {
            emailSenderPort.sendPasswordResetEmail(
                    event.recipient(),
                    event.rawToken()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to send password reset email to {}",
                    event.recipient(),
                    ex
            );
        }
    }
}
