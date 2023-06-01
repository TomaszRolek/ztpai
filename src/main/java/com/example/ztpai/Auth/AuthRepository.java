package com.example.ztpai.Auth;

import com.example.ztpai.AppUser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<AppUser, Long> {

}
