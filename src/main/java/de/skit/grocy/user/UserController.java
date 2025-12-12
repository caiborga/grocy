package de.skit.grocy.user;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.skit.grocy.user.dto.UserCreate;
import de.skit.grocy.user.dto.UserResponse;
import de.skit.grocy.user.dto.UserUpdate;
import jakarta.validation.Valid;

@RestController
class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users")
    List<UserResponse> all() {
        return service.getAllUsers();
    }

    @PostMapping("/users")
    public UserResponse create(@RequestBody @Valid UserCreate dto) {
        return service.createUser(dto);
    }

    @GetMapping("/users/{id}")
    public UserResponse one(@PathVariable UUID id) {
        return service.findUser(id);
    }

    @PatchMapping("/users/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody UserUpdate dto) {
         return service.updateUser(id, dto);
    }

    @DeleteMapping("/users/{id}")
    void delete(@PathVariable UUID id) {
        service.deleteUser(id);
    }

}
