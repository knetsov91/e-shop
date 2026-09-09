package eshop.com.eshopauthservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServerConfigTest {

    @Mock
    private JwtEncodingContext context;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtClaimsSet.Builder claimsBuilder;

    private final OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer =
            new AuthorizationServerConfig().jwtCustomizer();

    @Test
    void jwtCustomizer_whenAccessTokenWithUserDetailsPrincipal_thenAddsRolesClaim() {
        when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
        when(context.getPrincipal()).thenReturn(authentication);
        UserDetails userDetails = new User("jdoe", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(context.getClaims()).thenReturn(claimsBuilder);

        jwtCustomizer.customize(context);

        verify(claimsBuilder).claim("roles", List.of("ROLE_USER"));
    }
}
