package top.wyhao.system.auth.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import top.wyhao.system.auth.domain.gateway.PasswordEncodeGateway;

@Component
@RequiredArgsConstructor
public class PasswordEncodeGatewayImpl implements PasswordEncodeGateway {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String input) {
        return passwordEncoder.encode(input);
    }

    @Override
    public boolean matches(String input, String encoded) {
        if (input == null || encoded == null) {
            return false;
        }
        return passwordEncoder.matches(input, encoded);
    }
}
