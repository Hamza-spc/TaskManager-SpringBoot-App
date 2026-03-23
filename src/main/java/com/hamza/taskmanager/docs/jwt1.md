Dependency	What it REALLY does
jjwt-api	gives you classes/methods to use
jjwt-impl	actually generates & validates tokens
jjwt-jackson	converts Java ↔ JSON inside token

🔁 Full flow
Client sends JWT
↓
Server extracts it
↓
Check expiration
↓
Verify signature using secret key
↓
If valid → user authenticated

🧠 So JwtService answers 3 questions:
1. “How do I create a token?”
   generateToken()
2. “Who is inside this token?”
   extractUsername()
3. “Is this token still valid?”
   isTokenValid()

How does extractUsername(String token) work?
   In your JwtService, you have:
   public String extractUsername(String token) {
   return extractClaim(token, Claims::getSubject);
   }
   Let’s walk through it.
   Step 1
   extractUsername(token) calls:
   extractClaim(token, Claims::getSubject)
   This means:
   •
   “from this token, extract one claim”
   •
   the claim I want is the subject
   Step 2
   extractClaim(...) does:
   final Claims claims = extractAllClaims(token);
   return claimsResolver.apply(claims);
   So:
1.
parse the token
2.
get all claims from the payload
3.
apply the function Claims::getSubject
Step 3
extractAllClaims(token) does:
return Jwts.parser()
.verifyWith(getSigningKey())
.build()
.parseSignedClaims(token)
.getPayload();
This is the important part.
What happens here:
Jwts.parser()
Creates a JWT parser
.verifyWith(getSigningKey())
Tells the parser:
•
use this secret key to verify the token’s signature
.parseSignedClaims(token)
Parses the JWT and verifies:
•
is the signature valid?
•
is the token format valid?
If signature is wrong or token is malformed:
•
parsing fails
•
exception is thrown
.getPayload()
Once parsing succeeds, it returns the payload claims
That gives you a Claims object, which is basically the payload data.
Step 4
Now back in extractClaim(...), this happens:
claimsResolver.apply(claims)
Since the resolver is:
Claims::getSubject
this becomes conceptually:
claims.getSubject()
And since the payload contains:
{
"sub": "persist@email.com"
}
it returns:
persist@email.com
So extractUsername(token) means:
•
verify token signature
•
parse payload
•
read sub
•
return it as username/email
. How does isTokenValid(String token, UserDetails userDetails) work?
Your method:
public boolean isTokenValid(String token, UserDetails userDetails) {
final String username = extractUsername(token);
return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
}
Let’s walk slowly.
Step 1
final String username = extractUsername(token);
This gets the username/email stored inside the token.
Example result:
persist@email.com
Step 2
userDetails.getUsername()
This gets the username/email from the actual user object loaded from the database.
Remember:
•
your User implements UserDetails
•
in User, getUsername() returns email
So if the loaded user is the real database user, this also returns:
persist@email.com
Step 3
username.equals(userDetails.getUsername())
This checks:
•
does the username inside the token match the username of the loaded user?
Why is this important? Because you want to ensure:
•
the token belongs to this user
•
not some other user
If token says:
persist@email.com
but loaded user is:
hamza@email.com
then the token is not valid for that user.
So this comparison is basically: “Does this token claim to belong to the same user I loaded?”
Step 4
!isTokenExpired(token)
This checks whether the expiration time has passed.
Inside isTokenExpired(token):
return extractExpiration(token).before(new Date());
So:
•
get token expiration from exp
•
compare it with current time
•
if expiration is before now -> token is expired
Then !isTokenExpired(token) means:
•
token is still usable
Final result
Both conditions must be true:
1.
token username matches loaded user username
2.
token is not expired
So the method returns true only if:
token belongs to this user
•
token is still valid in time
Very short summary of your 3 questions
generateToken(...)
Creates:
•
header
•
payload (sub, iat, exp)
•
signature and packs them into the JWT string
extractUsername(...)
Parses token -> verifies signature -> reads payload -> returns sub
isTokenValid(...)
Checks:
•
token’s sub matches the DB user’s email
•
token is not expired