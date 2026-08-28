package in.myfarm.api.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Keycloak puts realm roles under the non-standard realm_access.roles
 * claim rather than the "scope" claim Spring Security's default JWT
 * authorities converter expects -- this reads that claim and maps each
 * role to a ROLE_-prefixed authority so .hasRole(...) works.
 */
class KeycloakJwtAuthoritiesConverter
		implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	@SuppressWarnings("unchecked")
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess == null
				|| !(realmAccess.get("roles") instanceof List<?> roles)) {
			return List.of();
		}
		return roles.stream()
				.map(String.class::cast)
				.map(role -> "ROLE_" + role)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toUnmodifiableList());
	}
}
