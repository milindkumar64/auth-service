package com.arth.auth.persist;

import com.arth.auth.model.AuthProviderType.AuthProviderType;
import com.arth.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT U from User U left join fetch U.roles where U.username = :username")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.providerId = :providerId AND u.providerType = :providerType")
    Optional<User> findByProviderIdAndProviderType(@Param("providerId") String providerId,
                                                     @Param("providerType") AuthProviderType providerType);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    Optional<User> findByEmail(String email);
}
