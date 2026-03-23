JWT Chapter 2: Login Flow
Now we move to the next part:
How the app turns email/password into a JWT token
This chapter is about:
•
AuthRequest
•
AuthResponse
•
AuthService
•
AuthServiceImpl

AuthServiceImpl as a whole
This class is the place where your app does:
login logic
Its job is:
1.
receive email/password
2.
verify them
3.
if valid, create JWT
4.
return the JWT
So this class is the bridge between:
•
normal login credentials
•
token-based authentication
A simplified version of the class is conceptually:
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
1. AuthenticationManager
   What it is
   AuthenticationManager is a Spring Security component whose job is:
   take login credentials and decide whether they are valid
   It does not generate tokens. It does not return JSON. It only answers:
   •
   valid login?
   •
   invalid login?
   Why we need it
   Because you do not want to manually write password verification logic like:
   •
   load user
   •
   compare encoded password yourself
   •
   handle security rules yourself
   Spring Security already knows how to do authentication properly.
   So instead of reinventing login logic, you delegate that responsibility to AuthenticationManager.
   What it uses internally
   When you call it, it uses the auth setup you configured earlier:
   AuthenticationProvider
   •
   CustomUserDetailsService
   •
   PasswordEncoder
   So AuthenticationManager is like the orchestrator of authentication.
   Simple analogy
   Think of AuthenticationManager as:
   •
   the login checker
   You give it:
   •
   email
   •
   password
   It says:
   •
   yes, valid
   •
   or no, reject
2. 2. CustomUserDetailsService
      You wrote this already, but now here is why we need it inside login flow.
      What it is
      It is the class that tells Spring Security:
      how to load a user from the database
      Spring Security does not know your repository. It does not know your UserRepository. It only knows it needs a user by username.
      In your app:
      •
      username = email
      So CustomUserDetailsService does:
      •
      findByEmail(email)
      Why we need it here
      During authentication, Spring Security must load the user record from the DB to:
      •
      get encoded password
      •
      get authorities/role
      •
      build the authenticated user object
      So when AuthenticationManager checks credentials, it relies on CustomUserDetailsService to fetch the user.
      Then later, after authentication succeeds, you also use it again yourself to load UserDetails so you can generate a JWT from that user.
      That is why it appears in AuthServiceImpl.
      In short
      CustomUserDetailsService is needed because:
      •
      Spring Security needs a way to find users in your DB
      •
      after authentication, you need the real UserDetails object to create the token
3. . JwtService
   What it is
   This is your own utility/service class that knows:
   •
   how to create a JWT
   •
   how to parse a JWT
   •
   how to validate a JWT
   Why we need it here
   Because after login succeeds, you want to give the client a token.
   AuthenticationManager only checks credentials. It does not create JWTs.
   So after authentication succeeds, you need another component to say:
   •
   “okay, this user is valid”
   •
   “now generate a signed token for them”
   That is exactly what JwtService does.
   In short
   •
   AuthenticationManager verifies login
   •
   JwtService creates token after login
   Both are needed, but for different jobs.
4. 4. Breaking down this line
      authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
      );
      This is the most important line in the login flow.
      Let’s break it down.
      Step A: request.getEmail()
      Gets the email the client sent in the login request.
      Example:
      hamza@email.com
      Step B: request.getPassword()
      Gets the raw password the client sent.
      Example:
      secret123
      tep C: new UsernamePasswordAuthenticationToken(...)
      This creates an object representing a login attempt.
      At this stage, the object means:
      •
      here is the username/email
      •
      here is the raw password
      •
      please try to authenticate this person
      Important: At this moment, this object is not yet authenticated. It is just a credential container.
      So conceptually it is like saying:
      “Spring Security, please check if these credentials are valid.”
      Step D: authenticationManager.authenticate(...)
      Now Spring Security receives that login attempt.
      What happens internally:
1.
AuthenticationManager receives the token
2.
it passes it to the configured AuthenticationProvider
3.
the provider uses CustomUserDetailsService to load the user by email
4.
it gets the stored encoded password from DB
5.
it uses PasswordEncoder.matches(rawPassword, encodedPassword)
6.
if password matches:
◦
authentication succeeds
7.
if it does not match:
◦
exception is thrown
So this one line is really triggering the whole username/password authentication mechanism.
Why we do not assign the result here
Often people write:
Authentication authentication = authenticationManager.authenticate(...);
That is valid too.
In your version, you are only using it as a validation step:
•
if it fails, exception happens
•
if it succeeds, continue
So in your code, the main purpose is: verify credentials
5. Breaking down this line
   UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
   What it does
   After login succeeds, this line loads the user details from the database.
   Why do we do this after authentication?
   Because now we know the credentials are correct.
   We need the actual UserDetails object so that JwtService can generate a token using:
   •
   username/email
   •
   maybe later roles/claims
   What happens inside
   loadUserByUsername(email) does:
   •
   query UserRepository.findByEmail(email)
   •
   return the User
   •
   the User works as UserDetails because your entity implements UserDetails
   So this line gives you the real authenticated user representation.
6. 6. Breaking down this line
      String token = jwtService.generateToken(userDetails);
      What it does
      It creates the JWT string.
      What JwtService does with userDetails
      Inside generateToken(...), it uses:
      •
      userDetails.getUsername()
      In your app, that means:
      •
      user email
      Then it builds a token containing:
      •
      subject = email
      •
      issued time
      •
      expiration
      •
      signature
      So if user email is:
      persist@email.com
      then the token payload will contain:
      {
      "sub": "persist@email.com",
      "iat": ...,
      "exp": ...
      }
      Why we need this line
      Because successful login by itself is not enough.
      We want to give the client a token that can be used on future requests.
      This line is the moment where:
      •
      successful credentials turn into
      •
      reusable JWT token
      So this line is basically:
      “Create the proof-of-identity token for this user.”
7. 7. Breaking down this block
      return AuthResponse.builder()
      .token(token)
      .build();
      What it does
      It builds the response object that will be returned to the client.
      Since AuthResponse has:
      private String token;
      this creates:
      AuthResponse {
      token = "eyJhbGciOi..."
      }
      Then your controller wraps it inside your standardized success response.
      So the final API response becomes something like:
      {
      "status": 200,
      "message": "Login successful",
      "data": {
      "token": "eyJhbGciOi..."
      }
      }
      Why use builder here?
      Because AuthResponse is a response DTO. You are creating it yourself in code. Builder is a clean way to do that.
      So this block is basically:
      “Take the generated JWT and send it back to the client.”

Full mechanism of AuthServiceImpl from start to finish
Let’s walk through the whole login flow slowly.
Input
Client sends:
{
"email": "hamza@email.com",
"password": "secret123"
}
Inside AuthServiceImpl
Step 1: verify credentials
authenticationManager.authenticate(...)
Spring Security:
•
loads user by email
•
compares raw password to encoded password
•
throws exception if invalid
If this step fails:
•
no token is created
•
login request fails
Step 2: load user details
customUserDetailsService.loadUserByUsername(...)
Now get the real UserDetails object for the authenticated user.
Step 3: generate JWT
jwtService.generateToken(userDetails)
Create a signed token containing the user identity.
Step 4: return token
return AuthResponse.builder().token(token).build();
Send token back to the client.
Short summary of the three dependencies
AuthenticationManager
Checks if login credentials are valid
CustomUserDetailsService
Loads the user from the database by email
JwtService
Creates the JWT after login succeeds
Short summary of the method
AuthServiceImpl.authenticate(...) means:
1.
validate email/password
2.
load user
3.
create token
4.
return token