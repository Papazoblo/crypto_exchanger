package medvedev.com.service.security;

import lombok.RequiredArgsConstructor;
import medvedev.com.repository.UserRepository;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isUserAuthenticated(String login, String password) {
        return userRepository.existsByLoginAndPassword(login, getHash256(password));
    }

    private static String getHash256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException ex) {
            //TODO логи
            return "";
        }
    }
}
