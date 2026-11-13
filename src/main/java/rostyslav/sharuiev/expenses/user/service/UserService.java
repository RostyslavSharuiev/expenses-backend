package rostyslav.sharuiev.expenses.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rostyslav.sharuiev.expenses.user.dto.CreateUserRequest;
import rostyslav.sharuiev.expenses.user.dto.UserResponse;
import rostyslav.sharuiev.expenses.user.entity.UserCredentialsEntity;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;
import rostyslav.sharuiev.expenses.user.mapper.UserMapper;
import rostyslav.sharuiev.expenses.user.repository.UserCredentialsRepository;
import rostyslav.sharuiev.expenses.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository userCredentialsRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    // TODO: delete after tests
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    String.format("User with %s already exists", request.email())
            );
        }

        var user = userMapper.toEntity(request);
        var savedUser = userRepository.save(user);

        var credentials = new UserCredentialsEntity();
        credentials.setUser(savedUser);
        credentials.setPasswordHash(passwordEncoder.encode(request.password()));

        userCredentialsRepository.save(credentials);

        return userMapper.toResponse(savedUser);
    }

    public UserResponse getUserById(UUID id) {
        var user = userRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User with id=%s not found", id))
                );

        return userMapper.toResponse(user);
    }
}
