package com.nbirl.bank.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Data
@Entity
@Table(name="role")
public class Role  implements GrantedAuthority {

    public Role(){}
    public Role(String roleName){
        this.roleName = roleName;
    }
    public Role(Long id, String roleName){
        this.id= id;
        this.roleName = roleName;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name="role", nullable = false)
    private String roleName;


    @Override
    public String getAuthority() {
        return roleName;
    }
}
