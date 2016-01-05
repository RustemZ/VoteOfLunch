package com.largecode.interview.rustem.service.validator;

        import com.largecode.interview.rustem.domain.User;
        import com.largecode.interview.rustem.domain.UserDto;
        import com.largecode.interview.rustem.service.UsersService;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Component;
        import org.springframework.validation.Errors;
        import org.springframework.validation.Validator;

        import java.util.Optional;

@Component
public class UserDtoValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDtoValidator.class);
    private final UsersService usersService;

    @Autowired
    public UserDtoValidator(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(UserDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LOGGER.debug("Validating {}", target);
        UserDto userDto = (UserDto) target;
        validatePasswords(errors, userDto);
    }

    private void validatePasswords(Errors errors, UserDto userDto) {
        if ( userDto.getPassword().equals( UserDto.NO_PASSWORD)) {
            return;
        }
        if (!userDto.getPassword().equals(userDto.getPasswordRepeated())) {
            errors.reject("password.no_match", "Passwords do not match.");
        }
        if ( (userDto.getPassword().length() < 6) || (userDto.getPassword().length() > 40)) {
            errors.reject("password.length", "Length of password must be between 6 and 40 chars.");
        }
    }

}