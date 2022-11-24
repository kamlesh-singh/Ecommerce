package org.ttn.ecommerce.services;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ttn.ecommerce.dto.AuthResponseDto;
import org.ttn.ecommerce.dto.LoginDto;
import org.ttn.ecommerce.dto.register.CustomerRegisterDto;
import org.ttn.ecommerce.dto.register.SellerRegisterDto;
import org.ttn.ecommerce.entities.*;
import org.ttn.ecommerce.repository.CustomerRepository;
import org.ttn.ecommerce.repository.RoleRepository;
import org.ttn.ecommerce.repository.SellerRepository;
import org.ttn.ecommerce.repository.TokenRepository.AccessTokenRepository;
import org.ttn.ecommerce.repository.TokenRepository.RefreshTokenRepository;
import org.ttn.ecommerce.repository.UserRepository;
import org.ttn.ecommerce.security.JWTGenerator;
import org.ttn.ecommerce.security.SecurityConstants;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Transactional
@NoArgsConstructor
public class UserDaoService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncode;
    private JWTGenerator jwtGenerator;
    private CustomerRepository customerRepository;
    private EmailService emailService;
    private SellerRepository sellerRepository;
    private TokenService tokenService;
    private AccessTokenRepository accessTokenRepository;
    private RefreshTokenRepository refreshTokenRepository;


    @Autowired
    public UserDaoService(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncode, JWTGenerator jwtGenerator, CustomerRepository customerRepository, EmailService emailService, SellerRepository sellerRepository, TokenService tokenService, AccessTokenRepository accessTokenRepository, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncode = passwordEncode;
        this.jwtGenerator = jwtGenerator;
        this.customerRepository = customerRepository;
        this.emailService = emailService;
        this.sellerRepository = sellerRepository;
        this.tokenService = tokenService;
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }



    public ResponseEntity<String> registerCustomer(CustomerRegisterDto registerDto){
        if(userRepository.existsByEmail(registerDto.getEmail())){
            return new ResponseEntity<>("Email is already taken", HttpStatus.BAD_REQUEST);
        }
        Customer customer =new Customer();
        customer.setFirstName(registerDto.getFirstName());
        customer.setMiddleName(registerDto.getMiddleName());
        customer.setLastName(registerDto.getLastName());

        customer.setActive(false);
        customer.setDeleted(false);
        customer.setExpired(false);
        customer.setLocked(false);
        customer.setInvalidAttemptCount(0);


        customer.setEmail(registerDto.getEmail());
        customer.setPassword(passwordEncode.encode(registerDto.getPassword()));
        customer.setContact(registerDto.getContact());


        Role roles = roleRepository.findByAuthority("ROLE_CUSTOMER").get();
        System.out.println(roles.getAuthority());
        customer.setRoles(Collections.singletonList(roles));

        customerRepository.save(customer);

        String token = tokenService.generateRegisterToken(customer);

        emailService.setSubject("Your Account || "+ customer.getFirstName() + " finish setting up your new  Account " );

        emailService.setToEmail(customer.getEmail());
        emailService.setMessage("Click on the link to Activate Your Account \n"
                + "127.0.0.1:8080/api/auth/activate_account/"+customer.getEmail() +"/"+token);
        emailService.sendEmail();


        return new ResponseEntity<>("Customer Registered Successfully!Activate Your Account within 3 hours",HttpStatus.CREATED);

    }


    public ResponseEntity<String> registerSeller(SellerRegisterDto sellerRegisterDto){
        if(userRepository.existsByEmail(sellerRegisterDto.getEmail())){
            return new ResponseEntity<>("Email is already taken", HttpStatus.BAD_REQUEST);
        }
        Seller seller =new Seller();
        seller.setFirstName(sellerRegisterDto.getFirstName());
        seller.setMiddleName(sellerRegisterDto.getMiddleName());
        seller.setLastName(sellerRegisterDto.getLastName());

        seller.setCompanyContact(sellerRegisterDto.getCompanyContact());
        seller.setCompanyName(sellerRegisterDto.getCompanyName());
        seller.setGst(sellerRegisterDto.getGstNumber());

        seller.setActive(true);
        seller.setDeleted(false);
        seller.setExpired(false);
        seller.setLocked(false);
        seller.setInvalidAttemptCount(0);

        seller.setEmail(sellerRegisterDto.getEmail());
        seller.setPassword(passwordEncode.encode(sellerRegisterDto.getPassword()));

        Role roles = roleRepository.findByAuthority("ROLE_SELLER").get();
        System.out.println(roles.getAuthority());
        seller.setRoles(Collections.singletonList(roles));

        sellerRepository.save(seller);

     /* Send Mail to Seller */
        emailService.setSubject( seller.getFirstName()+" Your Account || " + " has been created! " );
        emailService.setToEmail(seller.getEmail());
        emailService.setMessage("Please Kindly wait for admin to approve your account");
        emailService.sendEmail();

        /* Sending mail to Admin user to activate Sellers Account*/
        emailService.setSubject("New Seller Registered || Activate Sellers Account");
        emailService.setMessage("Seller Id : " + seller.getId()+
                "\nSeller Name : " + seller.getFirstName()+
                "\nSeller Gst Number : " + seller.getGst()+
                "\nReview and Activate Seller's Account");
        emailService.setToEmail(SecurityConstants.ADMIN_EMAIL_ADDRESS);
        emailService.sendEmail();

        return new ResponseEntity<>("Seller Registered Successfully !! kindly wait for admin to approve your seller account",HttpStatus.OK);


    }


    public ResponseEntity<?> loginCustomer(LoginDto loginDto,UserEntity user){


        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);


        /* Access Token */
        Token accessToken = new Token();
        accessToken.setUserEntity(user);
        accessToken.setToken(token);
        accessToken.setCreatedAt(LocalDateTime.now());
        accessToken.setExpiredAt(LocalDateTime.now().plusMinutes(SecurityConstants.JWT_EXPIRATION));
        accessTokenRepository.save(accessToken);


        /* Refresh Token */
        RefreshToken refreshToken = tokenService.generateRefreshToken(user);
        refreshTokenRepository.save(refreshToken);


        return new ResponseEntity<>(new AuthResponseDto(accessToken.getToken(),refreshToken.getToken()),HttpStatus.OK);
    }

    public ResponseEntity<String> confirmAccount(UserEntity userEntity, String token) {
       String out =  tokenService.confirmAccount(userEntity.getId(),token);
       return new ResponseEntity<>(out,HttpStatus.OK);
    }
}
