package com.b1.mysawit.repository;

import com.b1.mysawit.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE " +
        "(:nama IS NULL OR LOWER(u.nama) LIKE LOWER(CONCAT('%', :nama, '%'))) AND " +
        "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
        "(:role IS NULL OR u.role = :role)")
    List<User> searchAndFilterUsers(@Param("nama") String nama, 
                                    @Param("email") String email, 
                                    @Param("role") User.Role role);
}
