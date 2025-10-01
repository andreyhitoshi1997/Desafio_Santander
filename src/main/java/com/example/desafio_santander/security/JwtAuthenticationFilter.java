package com.example.desafio_santander.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");

        logger.info("JwtFilter: Processando request para {}", requestURI);

        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            logger.info("JwtFilter: Token encontrado, extraindo username...");
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                logger.info("JwtFilter: Username extraído: {}", username);
            } catch (Exception e) {
                logger.error("JwtFilter: Erro ao extrair username do token: {}", e.getMessage());
            }
        } else {
            logger.warn("JwtFilter: Nenhum token Bearer encontrado no header Authorization");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("JwtFilter: Carregando UserDetails para username: {}", username);
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.info("JwtFilter: UserDetails carregado: {}", userDetails.getUsername());

                if (jwtUtil.validateToken(jwt)) {
                    logger.info("JwtFilter: Token válido, criando authentication");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("JwtFilter: Authentication configurado com sucesso");
                } else {
                    logger.warn("JwtFilter: Token inválido ou expirado");
                }
            } catch (Exception e) {
                logger.error("JwtFilter: Erro ao carregar UserDetails: {}", e.getMessage());
            }
        } else if (username != null) {
            logger.info("JwtFilter: Usuário já autenticado no contexto");
        }

        logger.info("JwtFilter: Continuando para próximo filtro...");
        filterChain.doFilter(request, response);
    }
}
