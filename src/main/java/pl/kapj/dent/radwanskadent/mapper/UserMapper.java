package pl.kapj.dent.radwanskadent.mapper;

import org.springframework.stereotype.Component;
import pl.kapj.dent.radwanskadent.dto.UserRegistrationDto;
import pl.kapj.dent.radwanskadent.dto.UserTableDto;
import pl.kapj.dent.radwanskadent.model.Role;
import pl.kapj.dent.radwanskadent.model.User;

import java.util.UUID;

@Component
public class UserMapper {

    public String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.trim();
        if (trimmed.startsWith("+48") && trimmed.length() > 9) {
            return trimmed.substring(3);
        }
        return trimmed;
    }

    public User registerDtoToEntity(UserRegistrationDto dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        user.setPhone(normalizePhone(dto.getPhone()));
        Role role = (dto.getRole() == Role.DENTIST) ? Role.DENTIST : Role.CLIENT;
        user.setRole(role);
        user.setGender(dto.getGender());
        user.setActive(false);
        user.setActivationToken(role == Role.CLIENT ? UUID.randomUUID() : null);
        return user;
    }

    public UserTableDto toTableDto(User user) {
        if (user == null) {
            return null;
        }
        UserTableDto dto = new UserTableDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setGender(user.getGender());
        dto.setActive(user.isActive());
        return dto;
    }
}
