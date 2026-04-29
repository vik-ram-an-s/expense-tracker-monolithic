package com.myfin.expensetracker.user;

import com.myfin.expensetracker.constants.AppConstants;
import com.myfin.expensetracker.exception.InvalidCredentialsException;
import com.myfin.expensetracker.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterResponse mapToResponse(User user){

        RegisterResponse response  = new RegisterResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setMessage(AppConstants.USER_REGISTERED_SUCCESS);

        return response;
    }

    @Transactional
    public RegisterResponse registerUser( RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("User with the email already exists");
        }

        if(userRepository.existsByMobile(request.getMobile())){
            throw new RuntimeException("Mobile No already exists");
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Password does not match");
        }

        User newUser = new User();

        newUser.setEmail(request.getEmail().trim());
        newUser.setName(request.getName().trim());
        newUser.setMobile(request.getMobile().trim());
        newUser.setCurrency(request.getCurrency());
        newUser.setRole(Role.ROLE_USER);
        newUser.setIsActive(true);

        String password = passwordEncoder.encode(request.getPassword());

        newUser.setPassword(password);

        User savedUser = userRepository.save(newUser);

        return mapToResponse(savedUser);
    }

    public User getUserEntity(Long userId){
        return userRepository.findByIdAndIsActiveTrue(userId).orElseThrow(()->new InvalidCredentialsException(AppConstants.INVALID_CREDENTIALS));
    }

    public LoginResponse userLogin(LoginRequest request) {

        User existingUser = userRepository.findByMobile(request.getMobile().trim()).orElseThrow(()->new InvalidCredentialsException(AppConstants.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(request.getPassword(),existingUser.getPassword())){
            throw new InvalidCredentialsException(AppConstants.INVALID_CREDENTIALS);
        }

        String token = "vikraman";

        LoginResponse response = new LoginResponse(token,"Bearer",existingUser.getId(),existingUser.getName(),existingUser.getRole());

        return response;

    }

}
