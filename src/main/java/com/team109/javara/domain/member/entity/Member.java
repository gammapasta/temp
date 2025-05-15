package com.team109.javara.domain.member.entity;


import com.team109.javara.domain.location.entity.PoliceLocation;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.entity.enums.Gender;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.task.entity.Task;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


//도메인 주도 설계 사용
//자신의 상태, 상태와 직접적으로 관련된 로직은 여기서 구현
@Slf4j
@Entity
@Table(name = "member")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자를 통해서 값 변경 목적으로 접근하는 메시지들 차단
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username; // 스프링에서 로그인 할떄 사용하는 id

    @Column(nullable = false, length = 200)
    private String password; //암호화된 비번 저장

    @Column(name = "name", nullable = false, length = 50)
    private String name; //사람 이름

    
    @Enumerated(EnumType.STRING) // ENUM 타입 매핑
    @Column(name = "gender", nullable = true)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private Role role;

    @Column(unique = true, nullable = true, length = 20)
    private String policeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status",nullable = true)
    private MemberStatus memberStatus; //순찰상태

    @Column(precision = 5, scale = 2)
    private BigDecimal penaltyPoints = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "edge_device_id", nullable = true, length = 100) // Added length
    private String edgeDeviceId;


    //=================================================================


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true) //맘버가 탈퇴하면 위치는 다 사라짐
    private List<PoliceLocation> locations = new ArrayList<>();


    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();


    @OneToMany(mappedBy = "assignedMember", fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();





    //--- ---
    // Member 삭제 전에 Task 는 놔둠
    @PreRemove
    private void preRemove() {
        for (Task task : new ArrayList<>(assignedTasks)) {
            task.setAssignedMember(null);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.getRoleName()));
    }


    // Spring Security가 비밀번호를 가져갈 때 필요한 매소드
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;  //나중에 토큰 줄때 이걸로 이용, JWT의 Subject 저장 값에도 이거 사용함
    }

    public String getReporterName(){
        return this.id.toString() + "_" + this.name;
    }

    //---나중에 계정 관련 상태를 지정 가능---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }




    public void changeRole(Role newRole) {
        this.role = newRole;
    }
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    public void setMemberStatus(MemberStatus newMemberStatus){
        this.memberStatus = newMemberStatus;
    }
    public void setEdgeDeviceId(String newEdgeDeviceId) {
        this.edgeDeviceId = newEdgeDeviceId;
    }
    public void setAdmin(Role role){this.role = role;} //TODO 지워야함

    //로직 부분
    //핼퍼 매소드
    public void addImage(Image image) {
        images.add(image);
        image.setMember(this);
    }
    public void removeImage(Image image) {
        images.remove(image);
        image.setMember(null);
    }
    public void addTask(Task task) {
        assignedTasks.add(task);
        task.setAssignedMember(this);
    }
    public void removeTask(Task task) {
        assignedTasks.remove(task);
        task.setAssignedMember(null);
    }





//    //맵버 생성
//    @Builder
//    public Member(String username, String encodedPassword, String email, Role role) {
//        log.info("맴버 빌더 로그");
//        this.username = username;
//        this.password = encodedPassword; //PasswordEncoder로 서비스에서 인코딩 된 비번!!!!!!
//        this.role = role;
//        //        this.status = status; 추가
//    }



    //경찰 상태 병경???
//    public void updateStatus(String name, Gender gender, MemberStatus status) {
//        this.name = name;
//        this.gender = gender;
//        this.status = status;
//    }

    //나중에 추가
    public void updateMember() {

    }


}
