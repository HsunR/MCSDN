package com.blog.service;

import com.blog.entity.User;

public interface UserService {
    User findByUsername(String username);
    User createUser(String username, String rawPassword);
}
