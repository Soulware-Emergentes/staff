/**
 * The identity provider in front of the directory, standing in for Entra ID or Keycloak: which
 * applications are registered, the API each one calls, the roles every API defines, and which groups
 * hold them. Issues the tokens.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Identity provider")
package lat.soulware.staff.identityprovider;
