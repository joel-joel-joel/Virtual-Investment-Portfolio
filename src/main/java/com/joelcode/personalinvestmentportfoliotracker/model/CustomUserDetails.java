package com.joelcode.personalinvestmentportfoliotracker.model;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    // This class wraps the user entity for spring security to check and generate a jwt (takes the loaded username
    // from customeruserdetailsservice
    // Might want to modify these functions in the future?

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRoles().name()));
    }



    public User getUser() {return user;}

    @Override
    public String getUsername() {return user.getUsername();}

    @Override
    public String getPassword() {return user.getPassword();}

    @Override
    public boolean isAccountNonExpired() {return true;}

    @Override
    public boolean isAccountNonLocked() {return true;}

    @Override
    public boolean isCredentialsNonExpired() {return true;}

    @Override
    public boolean isEnabled() {return true;}
}
