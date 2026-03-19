# Credentials and configuration

Set these in environment variables or in `application.properties` (never commit real secrets).

## 1. MongoDB

- **`MONGODB_URI`** (optional if using defaults)  
  Example: `mongodb://localhost:27017/portfolio` or  
  `mongodb+srv://user:password@cluster.mongodb.net/portfolio`

## 2. JWT

- **`JWT_SECRET`** – Secret key for signing JWTs (min 32 characters).  
  Generate: `openssl rand -base64 32`
- **`JWT_EXPIRATION_MS`** (optional) – Token lifetime in ms. Default: 86400000 (24 hours).

## 3. Google OAuth (login with Google)

1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Create a project or select one.
3. **APIs & Services → Credentials → Create credentials → OAuth client ID**.
4. Application type: **Web application**.
5. **Authorized redirect URIs** add:
   - Dev: `http://localhost:8080/login/oauth2/code/google`
   - Prod: `https://your-api-domain.com/login/oauth2/code/google`
6. Copy **Client ID** and **Client secret**.

Set:

- **`GOOGLE_CLIENT_ID`** – OAuth client ID
- **`GOOGLE_CLIENT_SECRET`** – OAuth client secret
- **`OAUTH2_FRONTEND_REDIRECT_URL`** (optional) – Where to send the user after Google login (e.g. `http://localhost:3000/auth/callback`). The token is appended as `?token=<jwt>`.

**Google login flow:**  
User visits: `GET /oauth2/authorization/google`  
→ Redirects to Google → After login, redirects back to your app → Backend creates/finds user, issues JWT, redirects to `OAUTH2_FRONTEND_REDIRECT_URL?token=<jwt>`.

## 4. Email (OTP for email login)

For sending OTP emails, configure SMTP:

- **`MAIL_HOST`** – e.g. `smtp.gmail.com`
- **`MAIL_PORT`** – e.g. `587`
- **`MAIL_USERNAME`** – sender email / SMTP user
- **`MAIL_PASSWORD`** – SMTP password (for Gmail use an [App password](https://support.google.com/accounts/answer/185833))

Alternatively use SendGrid, Mailgun, etc. and set host/port/username/password to their SMTP values.

## 5. User roles (User, Admin, Super Admin)

- New users get role **USER** by default.
- To make someone **ADMIN** or **SUPER_ADMIN**, update the `users` collection in MongoDB, e.g.:

```javascript
db.users.updateOne(
  { email: "admin@example.com" },
  { $set: { roles: ["USER", "ADMIN"] } }
)

db.users.updateOne(
  { email: "super@example.com" },
  { $set: { roles: ["USER", "ADMIN", "SUPER_ADMIN"] } }
)
```

Roles are stored as an array of strings: `"USER"`, `"ADMIN"`, `"SUPER_ADMIN"`.

## API access summary

| Path pattern           | Who can access        |
|------------------------|------------------------|
| `/`, `/api/public/**`  | Everyone (no auth)    |
| `/api/auth/login/email/request-otp`, `verify-otp` | Everyone |
| `/oauth2/authorization/google`, `/login/oauth2/**` | Everyone (OAuth flow) |
| `/api/user/**`         | Any authenticated user |
| `/api/admin/**`        | ADMIN or SUPER_ADMIN  |
| `/api/super-admin/**`  | SUPER_ADMIN only      |

## Email login flow

1. **Request OTP:** `POST /api/auth/login/email/request-otp`  
   Body: `{ "email": "user@example.com" }`  
   → Sends 6-digit OTP to that email.

2. **Verify and get JWT:** `POST /api/auth/login/email/verify-otp`  
   Body: `{ "email": "user@example.com", "otp": "123456" }`  
   → Returns `{ "token": "<jwt>", "type": "Bearer" }`.

Use the token in the header: `Authorization: Bearer <token>`.
