package rostyslav.sharuiev.expenses.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rostyslav.sharuiev.expenses.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
}
