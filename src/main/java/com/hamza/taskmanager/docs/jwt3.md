JWT Chapter 3: JwtAuthenticationFilter.
1. Why is this class a filter?
   Because a filter runs for every incoming HTTP request before the controller is reached.
   Think of it like a checkpoint.
   Every request goes through this security checkpoint first:
   •
   GET /api/tasks
   •
   POST /api/tasks
   •
   GET /api/users
   •
   etc.
   This is exactly where authentication should happen, because the app needs to know:
   •
   is this request anonymous?
   •
   is this request from a valid logged-in user?
   So the filter is the perfect place to inspect the JWT.
2. Why extends OncePerRequestFilter?
   This means:
   •
   Spring will run this filter once per request
   That matters because you do not want the same authentication logic to accidentally run multiple times for one request.
   So this base class gives you a safe, standard way to implement a custom Spring security filter.
3. 3. Why does the filter need JwtService?
      Because the filter receives only the raw token string from the header.
      It needs help to:
      •
      extract username/email from the token
      •
      validate the token
      That is JwtService’s job.
      So:
      •
      filter = request-level security workflow
      •
      JwtService = token logic
      The filter should not itself know how to parse JWT internals. That responsibility is delegated.
4. Why does the filter need CustomUserDetailsService?
   Because reading the token is not enough.
   The filter also needs the real user from the database.
   Why? Because after extracting the username/email from the token, it must:
   •
   load the real user
   •
   compare token against real user details
   •
   get authorities/roles
   So CustomUserDetailsService answers:
   •
   “given this email, load the actual security user”
   Without that, the filter would know only:
   •
   token says email = hamza@email.com
   But it would not know:
   •
   does this user still exist?
   •
   what are their authorities?
   •
   what is their actual UserDetails object?
5. The method doFilterInternal(...)
   This is the heart of the filter.
   It runs on each request.
   Let’s go line by line.
   Step A: read the Authorization header
   final String authHeader = request.getHeader("Authorization");
   This asks the incoming HTTP request:
   “Do you have an Authorization header?”
   Example of such a header:
   Authorization: Bearer eyJhbGciOi...
   So now authHeader might be:
   •
   null
   •
   something else
   •
   a Bearer token string
   Step B: declare placeholders
   final String jwt;
   final String username;
   These are just variables that will later store:
   •
   the extracted token itself
   •
   the extracted username/email from that token
   Nothing special yet.
   Step C: if the header is missing or not Bearer, skip JWT logic
   if (authHeader == null || !authHeader.startsWith("Bearer ")) {
   filterChain.doFilter(request, response);
   return;
   }
   This is very important.
   What this checks
   It asks:
   •
   is there no Authorization header at all?
   •
   or is the header not a Bearer token?
   If yes, then:
   •
   this request is not trying to use JWT auth
   •
   so the filter should not try to parse anything
   Why filterChain.doFilter(...)?
   This means:
   •
   continue processing the request
   •
   let the next filter or Spring Security rule handle it
   So the filter is not blocking the request here. It is just saying:
   “No JWT token here, I’m not responsible for authenticating this one.”
   Why return?
   Because once it decides there is no Bearer token, the filter should stop its own logic immediately.
6. Extract the raw token
   jwt = authHeader.substring(7);
   Why 7?
   Because the header starts with:
   Bearer
   That string has 7 characters:
   •
   B e a r e r = 6
   •
   plus the space = 1
   •
   total = 7
   So this removes "Bearer " and leaves only the token.
   Example:
   From:
   Bearer eyJhbGciOi...
   To:
   eyJhbGciOi...
   Now jwt contains the actual token string.
7. 7. Extract username/email from token
      username = jwtService.extractUsername(jwt);
      This uses the JwtService you already studied.
      What happens:
      •
      parse the token
      •
      verify the signature
      •
      extract the sub claim
      •
      return it
      In your app:
      •
      sub = email
      So now username becomes something like:
      persist@email.com
      This is the identity claimed by the token.
8. 8. Check whether authentication is needed
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      Two conditions are checked.
      Condition 1: username != null
      Why? Because if token parsing somehow gave no username, authentication cannot proceed.
      No identity means:
      •
      no user to load
      •
      no authentication to set
      Condition 2:
      SecurityContextHolder.getContext().getAuthentication() == null
      This is very important.
      What is SecurityContextHolder?
      It is where Spring Security stores the current authenticated user for this request.
      Think of it as:
      This is very important.
      What is SecurityContextHolder?
      It is where Spring Security stores the current authenticated user for this request.
      Think of it as:
      •
      “security memory for the current request”
      If authentication is already there, it means:
      •
      someone already authenticated this request
      So the filter avoids re-authenticating unnecessarily.
      Why check this?
      Because if authentication already exists:
      •
      do not overwrite it
      •
      do not redo the work
      •
      avoid inconsistent behavior
      So this condition means:
      “Only authenticate if the token gave us a username and nobody is authenticated yet.”
9. 9. Load the user from the database
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      Now that the token claims to belong to:
      persist@email.com
      the filter loads the actual user from the database.
      Why do we do this?
      Because a token claiming an email is not enough by itself.
      We also need:
      •
      the actual user object
      •
      the current authorities/roles
      •
      confirmation that the user still exists
      So this line means:
      “Give me the real user that this token claims to represent.”
10. 10. Validate the token
        if (jwtService.isTokenValid(jwt, userDetails)) {
        Now the app checks:
        •
        does the token belong to this user?
        •
        is it not expired?
        You already studied this in JwtService.
        This is the safety check before trusting the token.
        If validation fails:
        •
        the filter does not authenticate the request
        •
        request stays anonymous
        •
        protected endpoint will eventually return 401
        So this line is the “trust or reject” decision.
11. 11. Create Spring Security authentication object
        UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
        );
        This is another very important line.
        What is this object?
        It is a Spring Security object representing an authenticated user.
        This time, unlike the login flow version, it is not being used as a login attempt.
        Here it is being used as:
        •
        authenticated principal
        •
        with authorities
        Parameters explained
    userDetails
    This is the authenticated user.
    null
    This is for credentials. We do not need to keep raw password here anymore.
    userDetails.getAuthorities()
    These are the user’s roles/permissions.
    Example:
    •
    ROLE_USER
    •
    ROLE_ADMIN
    So this object now says:
    •
    this request belongs to this user
    •
    and they have these authorities
    Why use this class?
    Because Spring Security understands this object as:
    •
    a valid authenticated security principal
    So this is how you convert your own token validation result into something Spring Security can use.
12. 🧠 The line
    authToken.setDetails(
    new WebAuthenticationDetailsSource().buildDetails(request)
    );
    🎯 What it REALLY does (simple)

👉 It just adds extra info about the request to the authenticated user.

That’s it.

🧠 Think like this

You already created:

User: hamza@example.com
Role: USER

Now this line adds:

Extra info:
- IP address
- session info
  📦 So the final object becomes:
  User: hamza@example.com
  Role: USER
  Extra:
    - IP: 192.168.1.1
    - SessionId: XYZ123
      🧠 What is WebAuthenticationDetailsSource?

👉 It’s just a helper that says:

“Take the HTTP request and extract some basic info from it”

🔍 What kind of info?

Mostly:

🌍 IP address (who is calling)
🧠 session ID (if sessions exist)
💡 Why would this be useful?

In real apps:

logging → “user logged in from IP X”
security → detect suspicious activity
auditing → track requests
⚠️ IMPORTANT

👉 This line is NOT required for JWT to work

If you remove it:

✔ authentication still works
✔ JWT still works

🧠 So why include it?

Because Spring Security standard practice is:

“When authenticating a user, attach request details too”

💬 Simple analogy

You already have a user:

Hamza (USER)

This line adds:

Hamza logged in from Morocco 🇲🇦 using browser X

🎯 One-line explanation

This line attaches extra request info (like IP) to the authenticated user, but it’s optional and not essential for JWT.

🚀 What you should focus on instead

The REAL important lines are:

extractUsername()
loadUserByUsername()
isTokenValid()
setAuthentication()

👉 This one is just “bonus info”
13. Put authentication into security context
    SecurityContextHolder.getContext().setAuthentication(authToken);
    This is the most important line in the whole filter.
    This is the moment where Spring Security is told:
    “This request is now authenticated as this user.”
    After this line, the rest of Spring Security and your app will treat the request as logged in.
    That means:
    •
    protected endpoints can be accessed
    •
    roles are available
    •
    current user information can be used later
    Without this line:
    •
    everything before it would be useless
    •
    the request would still be treated as anonymous
    So this line is the final “authentication completed” step.
14. 14. Continue the filter chain
        filterChain.doFilter(request, response);
        This means:
        •
        continue processing the request
        •
        move to the next filter / eventually the controller
        Very important: filters do not usually return the final controller response themselves. They inspect, modify, or authenticate the request, then pass it on.
        So once the user is authenticated, the request continues normally.

Full request example
Suppose the client sends:
GET /api/tasks
Authorization: Bearer eyJhbGciOi...
What happens
1.
filter reads Authorization
2.
sees it starts with Bearer
3.
extracts token
4.
extracts email from token
5.
checks nobody is authenticated yet
6.
loads user by email from DB
7.
validates token
8.
creates authenticated Spring Security token
9.
stores it in SecurityContextHolder
10.
request continues
11.
controller is allowed to run
If the token were missing or invalid:

no authentication would be set
•
protected endpoint would later fail with 401

Big summary
Why this filter exists
Because JWT login gives you a token, but requests need a way to turn that token back into an authenticated user.
What it does
•
reads Bearer token
•
extracts identity
•
loads user
•
validates token
•
authenticates request
Most important line
SecurityContextHolder.getContext().setAuthentication(authToken);
That is the line that actually makes the request “logged in”.