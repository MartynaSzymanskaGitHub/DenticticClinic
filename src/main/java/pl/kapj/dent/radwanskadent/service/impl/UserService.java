package pl.kapj.dent.radwanskadent.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kapj.dent.radwanskadent.constants.ValidationConstants;
import pl.kapj.dent.radwanskadent.dto.UserProfileDto;
import pl.kapj.dent.radwanskadent.dto.UserRegistrationDto;
import pl.kapj.dent.radwanskadent.dto.UserTableDto;
import pl.kapj.dent.radwanskadent.mapper.UserMapper;
import pl.kapj.dent.radwanskadent.model.Role;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.repository.AppointmentRepository;
import pl.kapj.dent.radwanskadent.repository.DoctorScheduleRepository;
import pl.kapj.dent.radwanskadent.repository.UserRepository;
import pl.kapj.dent.radwanskadent.service.IEmailService;
import pl.kapj.dent.radwanskadent.service.IJwtService;
import pl.kapj.dent.radwanskadent.service.IUserService;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final UserDetailsService userDetailsService;
    private final IJwtService jwtService;

    public UserService(UserRepository userRepository,
                       AppointmentRepository appointmentRepository,
                       DoctorScheduleRepository doctorScheduleRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       IEmailService emailService,
                       UserDetailsService userDetailsService,
                       IJwtService jwtService) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByPhone(String phone) {
        String normalized = userMapper.normalizePhone(phone);
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        return userRepository.existsByPhone(normalized) || userRepository.existsByPhone("+48" + normalized);
    }

    @Override
    public Optional<User> findUserByPhone(String phone) {
        String normalized = userMapper.normalizePhone(phone);
        if (normalized == null || normalized.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> user = userRepository.findFirstByPhone(normalized);
        if (user.isEmpty()) {
            user = userRepository.findFirstByPhone("+48" + normalized);
        }
        return user;
    }

    @Override
    public void registerUser(UserRegistrationDto dto) {
        if (dto.getRole() != Role.CLIENT && dto.getRole() != Role.DENTIST) {
            throw new IllegalArgumentException("label.accessDenied");
        }

        String normalizedEmail = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = userMapper.normalizePhone(dto.getPhone());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("registerForm.email.exists");
        }
        if (existsByPhone(normalizedPhone)) {
            throw new IllegalArgumentException("registerForm.phone.exists");
        }

        User newUser = userMapper.registerDtoToEntity(dto);
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        String messageContent;
        if (Role.CLIENT.equals(newUser.getRole())) {
            String activationLink = "http://localhost:8080/user/activate?token=" + newUser.getActivationToken();
            log.info("Wysłano link aktywacyjny na adres e-mail: {}", newUser.getEmail());
            messageContent = "Dzień dobry " + newUser.getFirstName() + "!\n\n"
                    + "Dziękujemy za rejestrację w Radwańska-Dent.\n"
                    + "Aby aktywować swoje konto, kliknij poniższy link:\n"
                    + activationLink + "\n\n"
                    + "Pozdrawiamy,\nZespół Radwańska-Dent";
        } else {
            newUser.setActivationToken(null);
            messageContent = "Dzień dobry " + newUser.getFirstName() + "!\n\n"
                    + "Twoje konto lekarskie zostało utworzone w Radwańska-Dent.\n"
                    + "Konto oczekuje na potwierdzenie i aktywację przez administratora.\n\n"
                    + "Pozdrawiamy,\nZespół Radwańska-Dent";
        }

        userRepository.save(newUser);
        emailService.sendMail(newUser.getEmail(), "Radwańska-Dent - Rejestracja konta", messageContent);
    }

    @Override
    public boolean activateUser(UUID token) {
        if (token == null) {
            return false;
        }
        Optional<User> userOptional = userRepository.findByActivationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(true);
            user.setActivationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public List<UserTableDto> getDentists() {
        return userRepository.findAllByRoleAndActiveTrueOrderByIdAsc(Role.DENTIST).stream()
                .map(userMapper::toTableDto)
                .toList();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user.error.notFound"));
    }

    @Override
    public List<UserTableDto> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(userMapper::toTableDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public void changeUserActiveStatus(Long userId, Principal principal) {
        User user = getUserById(userId);
        User currentUser = getCurrentUser(principal);
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            throw new IllegalStateException("admin.user.error.selfStatus");
        }
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId, Principal principal) {
        User user = getUserById(userId);
        User currentUser = getCurrentUser(principal);
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            throw new IllegalStateException("admin.user.error.selfDelete");
        }
        if (appointmentRepository.existsByPatientIdOrDentistId(userId, userId)
                || doctorScheduleRepository.existsByUserId(userId)) {
            throw new IllegalStateException("admin.user.error.hasRelations");
        }
        userRepository.delete(user);
    }

    @Override
    public User getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(principal.getName().trim()).orElse(null);
    }

    @Override
    public String updateUserProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user.error.notFound"));

        String newEmail = profileDto.getEmail().trim().toLowerCase(Locale.ROOT);
        String normalizedNewPhone = userMapper.normalizePhone(profileDto.getPhone());
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(newEmail);

        if (emailChanged) {
            if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new IllegalStateException("registerForm.email.exists");
            }
            user.setEmail(newEmail);
        }

        if (!Objects.equals(user.getPhone(), normalizedNewPhone)) {
            if (userRepository.existsByPhone(normalizedNewPhone)) {
                throw new IllegalStateException("registerForm.phone.exists");
            }
            user.setPhone(normalizedNewPhone);
        }
        user.setFirstName(profileDto.getFirstName().trim());
        user.setLastName(profileDto.getLastName().trim());
        if (profileDto.getGender() != null) {
            user.setGender(profileDto.getGender());
        }

        if (profileDto.getNewPassword() != null && !profileDto.getNewPassword().trim().isEmpty()) {
            if (profileDto.getNewPassword().length() < ValidationConstants.PASSWORD_MIN
                    || profileDto.getNewPassword().length() > ValidationConstants.PASSWORD_MAX) {
                throw new IllegalArgumentException("registerForm.password.size");
            }
            user.setPassword(passwordEncoder.encode(profileDto.getNewPassword()));
        }

        userRepository.save(user);

        if (emailChanged) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(newEmail);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        auth.getCredentials(),
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                return jwtService.generateToken(userDetails);
            }
        }
        return null;
    }

}
