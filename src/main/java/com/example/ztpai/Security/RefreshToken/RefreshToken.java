package com.example.ztpai.Security.RefreshToken;

import com.example.ztpai.AppUser.AppUser;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String token;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_app_user_id")
    @ToString.Exclude
    private AppUser owner;

    private Instant created;
}
