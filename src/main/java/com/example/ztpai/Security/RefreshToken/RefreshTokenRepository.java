package com.example.ztpai.Security.RefreshToken;

import com.example.ztpai.AppUser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByOwnerUsernameAndToken(String username, String token);
    void deleteByToken(String token);
    void deleteAllByOwner(AppUser appUser);
}

