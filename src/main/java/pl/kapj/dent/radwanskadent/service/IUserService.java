package pl.kapj.dent.radwanskadent.service;

import pl.kapj.dent.radwanskadent.dto.UserProfileDto;
import pl.kapj.dent.radwanskadent.dto.UserRegistrationDto;
import pl.kapj.dent.radwanskadent.dto.UserTableDto;
import pl.kapj.dent.radwanskadent.model.User;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void registerUser(UserRegistrationDto user);

    boolean activateUser(UUID token);

    List<UserTableDto> getDentists();

    User getUserById(Long id);

    List<UserTableDto> getAllUsers();

    long countUsers();

    void changeUserActiveStatus(Long userId, Principal principal);

    void deleteUser(Long userId, Principal principal);

    User getCurrentUser(Principal principal);

    String updateUserProfile(Long userId, UserProfileDto profileDto);
}
