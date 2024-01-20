package com.example.security;

import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUser;
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUserRegistry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.Set;

@WebListener
public class Bootstrap implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // setup security
        InMemoryUserRegistry.get().clear();
        InMemoryUserRegistry.get().registerUser(new InMemoryUser("admin", "admin", Set.of("ROLE_ADMIN", "ROLE_USER")));
        InMemoryUserRegistry.get().registerUser(new InMemoryUser("user", "user", Set.of("ROLE_USER")));
    }
}
