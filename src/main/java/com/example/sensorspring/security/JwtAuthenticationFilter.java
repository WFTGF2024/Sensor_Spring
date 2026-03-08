package com.example.sensorspring.security;

import com.example.sensorspring.entity.User;
import com.example.sensorspring.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, StringRedisTemplate redisTemplate) {
        this.jwtService = jwtService; this.userRepository = userRepository; this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var jws = jwtService.parse(token);
                String jti = jws.getBody().getId();
                Boolean blacklisted = Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jti));
                if (!blacklisted) {
                    Long userId = Long.valueOf(jws.getBody().getSubject());
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        var authToken = new UsernamePasswordAuthenticationToken(
                                user, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        MDC.put("user", user.getUsername());
                    }
                }
            } catch (Exception ignore) {}
        }
        filterChain.doFilter(request, response);
    }
}
