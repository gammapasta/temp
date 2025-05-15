package com.team109.javara.domain.member.repository;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.Gender;
import com.team109.javara.domain.member.entity.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(Long id);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEdgeDeviceId(String edgeDeviceId);

    boolean existsByPoliceId(String username);

    boolean existsByUsername(String username);

    @Query("SELECT m FROM Member m WHERE " +
            "(:username IS NULL OR m.username LIKE %:username%) AND " +
            "(:name IS NULL OR m.name LIKE %:name%) AND " +
            "(:policeId IS NULL OR m.policeId LIKE %:policeId%) AND " +
            "(:gender IS NULL OR m.gender = :gender) AND " +
            "(:role IS NULL OR m.role = :role)")
    Page<Member> searchByFilters(@Param("username") String username,
                                 @Param("name") String name,
                                 @Param("policeId") String policeId,
                                 @Param("gender") Gender gender,
                                 @Param("role") Role role,
                                 Pageable pageable);
}
