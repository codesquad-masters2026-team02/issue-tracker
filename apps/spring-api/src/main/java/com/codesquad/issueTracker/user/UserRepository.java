package com.codesquad.issueTracker.user;

import com.codesquad.issueTracker.auth.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ListCrudRepository<User, Long> {

    boolean existsUserByUsername(String username);

    User findUserByUsername(String username);

    @Modifying
    @Query("UPDATE users SET refresh_token = :token WHERE id = :id")
    void updateRefreshToken(@Param("id") long id, @Param("token") String token);

    @Modifying
    @Query("UPDATE users SET refresh_token = null WHERE id = :id")
    void wipeRefreshToken(@Param("id") Long id);

    Optional<User> findUserByOauthProviderAndOauthId(OAuthProvider oauthProvider, Long oauthId);
}
