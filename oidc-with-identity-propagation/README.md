## Identity Propagation with OpenID Connect (OIDC)

When securing an application with OpenID Connect (OIDC), the `elytron-oidc-client` subsystem will automatically create a
virtual security domain for you. If your application invokes an EJB, additional configuration might be required to propagate
the security identity from the virtual security domain depending on how the EJB is being secured.

If your application secured with OIDC invokes an EJB and you'd like to secure the EJB using a different security domain,
additional configuration will be needed as shown in this example.

