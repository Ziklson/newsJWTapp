package nicetu.kuvarin.news.newsJwtApp.controller;


import nicetu.kuvarin.news.newsJwtApp.model.ERole;
import nicetu.kuvarin.news.newsJwtApp.model.Role;
import nicetu.kuvarin.news.newsJwtApp.model.User;
import nicetu.kuvarin.news.newsJwtApp.payload.request.LoginRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.request.SignupRequest;
import nicetu.kuvarin.news.newsJwtApp.payload.response.JwtResponse;
import nicetu.kuvarin.news.newsJwtApp.payload.response.MessageResponse;
import nicetu.kuvarin.news.newsJwtApp.repository.RoleRepository;
import nicetu.kuvarin.news.newsJwtApp.repository.UserRepository;
import nicetu.kuvarin.news.newsJwtApp.security.jwt.JwtUtils;
import nicetu.kuvarin.news.newsJwtApp.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getFirst_name(),
                userDetails.getLast_name(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: email is already taken!"));
        }


        User user = new User(signupRequest.getEmail(), signupRequest.getFirst_name(), signupRequest.getLast_name(),
                encoder.encode(signupRequest.getPassword()));

        Set<Role> roles = new HashSet<>();


        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);



        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
