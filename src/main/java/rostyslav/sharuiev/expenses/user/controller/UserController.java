package rostyslav.sharuiev.expenses.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rostyslav.sharuiev.expenses.user.dto.CreateUserRequest;
import rostyslav.sharuiev.expenses.user.dto.UserResponse;
import rostyslav.sharuiev.expenses.user.entity.UserEntity;
import rostyslav.sharuiev.expenses.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // TODO: remove after tests
    @GetMapping("/all")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest user) {
        log.info("Called createUser with email={}", user.email());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("Called getUserById with id={}", id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserById(id));
    }
}
