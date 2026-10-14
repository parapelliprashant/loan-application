package com.loanapplication.service.impl;

import com.loanapplication.dto.LoginRequest;
import com.loanapplication.dto.RegisterRequest;
import com.loanapplication.dto.TotpSetupResponse;
import com.loanapplication.entities.Customer;
import com.loanapplication.entities.Role;
import com.loanapplication.entities.User;
import com.loanapplication.repo.CustomerRepository;
import com.loanapplication.repo.RoleRepository;
import com.loanapplication.repo.UserRepository;
import com.loanapplication.security.JwtService;
import com.loanapplication.security.TotpService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.loanapplication.email.EmailService;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    public AuthService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService,
            TotpService totpService,EmailService emailService) {

        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.totpService = totpService;
        this.emailService = emailService;
    }
    @Transactional
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() ->
                        new RuntimeException("CUSTOMER role not found"));

        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setAge(request.getAge());
        customer.setEmail(request.getEmail());
        customer.setPassword(null);
        customer.setMobileNo(request.getMobileNo());
        customer.setPan(request.getPan());
        customer.setAadhaarNo(request.getAadhaarNo());
        customer.setEmploymentType(request.getEmploymentType());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setMonthlyExpense(request.getMonthlyExpense());
        customer.setEmailVerified(false);
        customer.setCreatedAt(LocalDateTime.now());

        customerRepository.save(customer);

        User user = new User();

        user.setRole(customerRole);
        user.setCustomer(customer);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobileNo());

        // Authentication password is stored in Users.Password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setEnabled(true);
        user.setTotpConfigured(false);
        user.setEmailOtpAttempts(0);

        userRepository.save(user);

        return "Customer registered successfully";
    }

    @Transactional
    public String sendEmailOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Customer customer = user.getCustomer();

        if (customer == null) {
            throw new RuntimeException("Customer profile not found");
        }

        if (Boolean.TRUE.equals(customer.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }

        LocalDateTime now = LocalDateTime.now();

        if (user.getEmailOtpSentAt() != null
                && user.getEmailOtpSentAt()
                .plusSeconds(RESEND_COOLDOWN_SECONDS)
                .isAfter(now)) {
            throw new RuntimeException(
                    "Please wait before requesting another OTP");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        user.setEmailOtpHash(hashOtp(otp));
        user.setEmailOtpExpiry(now.plusMinutes(OTP_EXPIRY_MINUTES));
        user.setEmailOtpAttempts(0);
        user.setEmailOtpSentAt(now);

        userRepository.save(user);

        // Send after the OTP data has been prepared.
        emailService.sendOtp(email, otp);

        return "Email verification OTP sent successfully";
    }


    public String login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        return jwtService.generatePreAuthToken(
                user.getEmail()
        );
    }
    public TotpSetupResponse setupTotp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getTotpConfigured()) {
            throw new RuntimeException(
                    "TOTP is already configured"
            );
        }

        String secret = totpService.generateSecret();

        user.setPendingSecret(secret);

        userRepository.save(user);

        String otpAuthUri =
                totpService.otpAuthUri(
                        user.getEmail(),
                        secret
                );

        String qrCode =
                totpService.qrDataUri(otpAuthUri);

        return new TotpSetupResponse(
                totpService.formatSecret(secret),
                qrCode
        );
    }
    public String verifyTotp(String email, String code) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String secret;

        // First-time TOTP setup
        if (!user.getTotpConfigured()) {

            if (user.getPendingSecret() == null) {
                throw new RuntimeException(
                        "TOTP setup has not been started"
                );
            }

            secret = user.getPendingSecret();

        } else {

            // TOTP already configured
            if (user.getSecretKey() == null) {
                throw new RuntimeException(
                        "TOTP secret not found"
                );
            }

            secret = user.getSecretKey();
        }

        boolean valid = totpService.verify(
                secret,
                code
        );

        if (!valid) {
            throw new RuntimeException(
                    "Invalid authenticator code"
            );
        }

        // Only during first-time setup,
        // move pending secret to permanent secret
        if (!user.getTotpConfigured()) {

            user.setSecretKey(user.getPendingSecret());
            user.setPendingSecret(null);
            user.setTotpConfigured(true);

            userRepository.save(user);
        }

        return jwtService.generateAuthToken(
                user.getEmail()
        );
    }

    @Transactional
    public String verifyEmailOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Customer customer = user.getCustomer();

        if (customer == null) {
            throw new RuntimeException("Customer profile not found");
        }

        if (Boolean.TRUE.equals(customer.getEmailVerified())) {
            return "Email is already verified";
        }

        if (user.getEmailOtpHash() == null
                || user.getEmailOtpExpiry() == null) {
            throw new RuntimeException("Request an OTP first");
        }

        if (user.getEmailOtpExpiry().isBefore(LocalDateTime.now())) {
            clearEmailOtp(user);
            userRepository.save(user);
            throw new RuntimeException("OTP has expired");
        }

        int attempts = user.getEmailOtpAttempts() == null
                ? 0 : user.getEmailOtpAttempts();

        if (attempts >= MAX_OTP_ATTEMPTS) {
            clearEmailOtp(user);
            userRepository.save(user);
            throw new RuntimeException(
                    "Too many incorrect attempts. Request a new OTP");
        }

        if (!MessageDigest.isEqual(
                user.getEmailOtpHash().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                hashOtp(otp).getBytes(java.nio.charset.StandardCharsets.UTF_8))) {

            user.setEmailOtpAttempts(attempts + 1);
            userRepository.save(user);

            throw new RuntimeException("Invalid email OTP");
        }

        customer.setEmailVerified(true);
        customerRepository.save(customer);

        clearEmailOtp(user);
        userRepository.save(user);

        return "Email verified successfully";
    }
    private String hashOtp(String otp) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    otp.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash OTP", e);
        }
    }

    private void clearEmailOtp(User user) {
        user.setEmailOtpHash(null);
        user.setEmailOtpExpiry(null);
        user.setEmailOtpAttempts(0);
        user.setEmailOtpSentAt(null);
    }
}