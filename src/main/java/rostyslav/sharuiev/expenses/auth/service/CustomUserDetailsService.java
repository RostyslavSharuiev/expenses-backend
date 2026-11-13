package rostyslav.sharuiev.expenses.auth.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rostyslav.sharuiev.expenses.auth.repository.UserCredentialsRepository;
import rostyslav.sharuiev.expenses.user.entity.UserCredentialsEntity;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialsRepository credentialsRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        UserCredentialsEntity credentials = credentialsRepository
                .findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User  with email=%s not found", email)
                ));

        return User
                .withUsername(credentials.getUser().getEmail())
                .password(credentials.getPasswordHash())
                .roles("USER")
                .build();
    }
}
