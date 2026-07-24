# Manual front-channel OIDC logout test

This guide walks through a **browser-based** logout test using WildFly Elytron OIDC Client and a local Keycloak install. It matches the configuration in `simple-webapp-oidc` (`WEB-INF/oidc.json`).

## Overview

| Component | URL / port |
|-----------|------------|
| Keycloak | http://localhost:8180 |
| WildFly HTTP (app) | http://localhost:8090 (`port-offset=10`) |
| WildFly management | http://localhost:9990 |
| Application context | `/simple-webapp-oidc` |

### Logout mechanisms in this test

| Mechanism | Config | Who calls it | Visible in browser address bar? |
|-----------|--------|--------------|--------------------------------|
| **RP-initiated logout** | `logout-path` → `/mylogout` | User (link) | No — redirects to Keycloak first |
| **Front-channel logout** | `logout-callback-path` → `/more/myCallback` | Keycloak (during logout) | Usually **no** (iframe/background GET) |
| **Post-logout redirect** | `post-logout-redirect-uri` → `/logged-out` | Keycloak (after logout) | **Yes** |

Keycloak ends the **SSO session**. WildFly must end the **application session**. Front-channel logout is how Keycloak tells the app to drop the local session via `logout-callback-path`.

---

## Prerequisites

- JDK and Maven
- WildFly with `elytron-oidc-client` subsystem (WildFly 27+ / EAP 8+)
- Keycloak distribution (tested with 24.x; newer versions should work similarly)
- This example built from `elytron-examples/simple-webapp-oidc`

---

## Step 1 — Start Keycloak locally

From your Keycloak install directory:

```bash
export KC_BOOTSTRAP_ADMIN_USERNAME=admin
export KC_BOOTSTRAP_ADMIN_PASSWORD=admin

bin/kc.sh start-dev --http-port=8180 --hostname-strict=false
```

- Admin console: http://localhost:8180/admin  
- Login: `admin` / `admin`

---

## Step 2 — Configure Keycloak

### 2.1 Create realm

1. Open the admin console.
2. Create realm **`myrealm`** (or use an existing test realm).

### 2.2 Create user

1. **Users** → create user (e.g. **`joe`**).
2. Set a password (disable “temporary” if you want a fixed password for testing).

### 2.3 Create client `myclient`

**Clients** → **Create client**

| Setting | Value |
|---------|--------|
| Client type | OpenID Connect |
| Client ID | `myclient` |
| Client authentication | **OFF** (public client) |
| Standard flow | **ON** |
| Direct access grants | OFF (optional) |

**Settings** tab:

| Field | Value |
|-------|--------|
| Valid redirect URIs | `http://localhost:8090/simple-webapp-oidc/*` |
| Valid post logout redirect URIs | `http://localhost:8090/simple-webapp-oidc/*` |
| Web origins | `http://localhost:8090` |
| Root URL | (optional) `http://localhost:8090/simple-webapp-oidc` |

**Advanced** tab (front-channel only):

| Field | Value |
|-------|--------|
| Front channel logout | **ON** |
| Front channel logout URL | `http://localhost:8090/simple-webapp-oidc/more/myCallback` |
| Front-channel logout session required | **ON** |
| Backchannel logout URL | *(empty)* |
| Backchannel logout session required | **OFF** |

> Do **not** enable both front-channel and back-channel logout on the same client for this test.

Save the client.

---

## Step 3 — Start WildFly

Use a port offset so HTTP is on **8090** while Keycloak uses **8180**:

```bash
cd $WILDFLY_HOME
./bin/standalone.sh -Djboss.socket.binding.port-offset=10
```

Management port: **9990**.

---

## Step 4 — Deploy the application

```bash
cd /path/to/elytron-examples/simple-webapp-oidc

export OIDC_PROVIDER_URL=http://localhost:8180
export OIDC_POST_LOGOUT_REDIRECT_URL=http://localhost:8090/simple-webapp-oidc/logged-out

mvn clean package wildfly:deploy -Dwildfly.port=9990
```

### Application `oidc.json` (reference)

| Attribute | Value |
|-----------|--------|
| `client-id` | `myclient` |
| `provider-url` | `http://localhost:8180/realms/myrealm` |
| `public-client` | `true` |
| `logout-path` | `/mylogout` |
| `logout-callback-path` | `/more/myCallback` *(path only — for Elytron)* |
| `post-logout-redirect-uri` | `http://localhost:8090/simple-webapp-oidc/logged-out` |
| `logout-session-required` | `true` |

### Application URLs

| Path | Purpose |
|------|---------|
| `/` | Home |
| `/secured` | Protected page (shows principal) |
| `/mylogout` | Starts RP-initiated logout |
| `/more/myCallback` | Front-channel callback URL (Elytron only — **no servlet**) |
| `/logged-out` | Post-logout landing page (`PostLogoutServlet`) |

---

## Step 5 — Manual browser test

Use a **fresh** browser window or incognito to avoid stale cookies.

| # | Action | Expected result |
|---|--------|-----------------|
| 1 | Open http://localhost:8090/simple-webapp-oidc/secured | Redirect to Keycloak login |
| 2 | Log in as `joe` (or your user) | Secured page shows `Current Principal 'joe'` |
| 3 | Click **Log out** (or open http://localhost:8090/simple-webapp-oidc/mylogout) | Keycloak logout UI (“logging out from following apps…”) |
| 4 | Complete logout | Browser lands on **You are logged out** at `/logged-out` |
| 5 | Open http://localhost:8090/simple-webapp-oidc/secured | Redirect to Keycloak login again (**not** still `joe`) |

### Success criteria

- You see the `/logged-out` page after logout.
- `/secured` requires login again (HTTP **302** in access log, not **200** with the old user).

---
