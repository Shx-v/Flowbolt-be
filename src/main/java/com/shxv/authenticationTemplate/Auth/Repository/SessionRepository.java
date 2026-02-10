package com.shxv.authenticationTemplate.Auth.Repository;

import com.shxv.authenticationTemplate.Auth.Model.Session;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface SessionRepository extends ReactiveCrudRepository<Session, UUID> {

    @Query("SELECT * FROM sessions WHERE user_id = :userId AND active = true AND expires_at > CURRENT_TIMESTAMP")
    Flux<Session> findByUserIdAndActiveIsTrue(UUID userId);

    @Query("SELECT * FROM sessions WHERE refresh_token = :refreshToken AND active = true AND expires_at > CURRENT_TIMESTAMP")
    Mono<Session> findByRefreshTokenAndActiveIsTrue(String refreshToken);

    @Query("SELECT * FROM sessions WHERE access_token = :accessToken AND active = true AND expires_at > CURRENT_TIMESTAMP")
    Mono<Session> findByAccessTokenAndActiveIsTrue(String accessToken);

    @Query("DELETE FROM sessions WHERE expires_at < :cutoff")
    Mono<Void> deleteExpiredSessions(Instant cutoff);
}
