package com.example.ztpai.AppUser;

import com.example.ztpai.Auth.AuthService;
import com.example.ztpai.Chat.ChatService;
import com.example.ztpai.Security.RefreshToken.RefreshTokenService;
import com.example.ztpai.Utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final ChatService chatService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    @Autowired
    @Lazy
    public AppUserService(AppUserRepository appUserRepository, ChatService chatService, RefreshTokenService refreshTokenService, AuthService authService) {
        this.appUserRepository = appUserRepository;
        this.chatService = chatService;
        this.refreshTokenService = refreshTokenService;
        this.authService = authService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User with username %s not found", username)));
    }

    @Transactional(readOnly = true)
    public List<String> getAllUsers() {
        return appUserRepository.findAll().stream().map(AppUser::getUsername).toList();
    }

    @Transactional(readOnly = true)
    public AppUserResponse getUser(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User " + username + " not found"));

        return new AppUserResponse(
                user.getAppUserId(),
                user.getUsername(),
                new String(user.getAvatar()),
                Time.timeAgo(user.getCreated())
        );
    }

    @Transactional
    public ResponseEntity<String> deleteUser(String username) {
        Optional<AppUser> userOptional = appUserRepository.findByUsername(username);
        if (userOptional.isEmpty())
            return new ResponseEntity<>("User " + username + " not found", HttpStatus.NOT_FOUND);

        AppUser user = userOptional.get();

        if (!user.equals(authService.getUserFromAuthentication()))
            return new ResponseEntity<>("You are not the owner of this account", HttpStatus.FORBIDDEN);

        refreshTokenService.deleteRefreshTokensByUser(user);

        chatService.deleteAllChatsByUser(user);

        appUserRepository.delete(user);

        return new ResponseEntity<>("User deleted", HttpStatus.OK);
    }
}