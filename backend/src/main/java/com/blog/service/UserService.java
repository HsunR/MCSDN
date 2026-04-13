package com.blog.service;

import com.blog.entity.User;

public interface UserService {
    String authenticate(String username, String password);
    User findByUsername(String username);
    User createUser(String username, String rawPassword);
}
