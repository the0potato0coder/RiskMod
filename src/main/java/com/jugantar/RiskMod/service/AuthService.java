package com.jugantar.RiskMod.service;
import com.jugantar.RiskMod.model.User;
import com.jugantar.RiskMod.repository.UserRepository;
import com.jugantar.RiskMod.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new user in the system.
     *
     * @param username The username for the new user.
     * @param password The plain-text password for the new user.
     * @throws RuntimeException if the username is already taken.
     */
    public void registerUser(String username, String password) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(username);

        // Hash the password BEFORE saving
        user.setPassword(passwordEncoder.encode(password));

        // Save the new user to the database
        userRepository.save(user);
    }

    /**
     * Authenticates a user and returns a JWT.
     * @param username The user's username.
     * @param password The user's plain-text password.
     * @return A valid JWT string.
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid.
     */
    public String loginUser(String username, String password) {
        // 1. Authenticate the user with Spring Security's AuthenticationManager
        // This will internally use our UserDetailsServiceImpl and PasswordEncoder.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // 2. If successful, set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Get the UserDetails principal from the authentication object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 4. Generate a JWT using our token provider
        return tokenProvider.generateToken(userDetails);
    }
}