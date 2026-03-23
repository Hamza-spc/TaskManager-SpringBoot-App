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