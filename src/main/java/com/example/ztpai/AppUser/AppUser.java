package com.example.ztpai.AppUser;

import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long appUserId;

    @Column(unique = true)
    @NotBlank(message = "Nazwa użytkownika jest wymagany")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Nazwa użytkownika może zawierać tylko liczby i cyfry")
    @Size(min = 1, max = 128, message = "Nazwa użytkownika nie może przekraczać 128 znaków")
    @Getter(AccessLevel.NONE)
    private String username;

    @AssertFalse(message = "Username is a reserved word")
    private boolean isUsernameReservedWord() {
        List<String> reservedWords = List.of("login", "register", "logout", "profile", "chat", "message", "user", "api");
        return reservedWords.contains(username);
    }

    // password
    @NotBlank(message = "Password is required")
    @Getter(AccessLevel.NONE)
    private String password;

    // avatar
    @Column(nullable = false, length = 100000)
    @Size(max = 100000, message = "Avatar must be less than 100kB")
    private byte[] avatar;

    // appUserAuthority
    @Enumerated(EnumType.STRING)
    private AppUserAuthority appUserAuthority;

    // created
    @PastOrPresent(message = "Created date must be in the past or present")
    private Instant created;

    // methods
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(appUserAuthority.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AppUser appUser = (AppUser) o;
        return appUserId != null && Objects.equals(appUserId, appUser.appUserId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
