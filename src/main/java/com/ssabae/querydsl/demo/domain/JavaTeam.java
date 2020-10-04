package com.ssabae.querydsl.demo.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : leesangbae
 * @project : demo
 * @date : 2020-10-04
 */
@Entity
public class JavaTeam {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    List<JavaMember> members = new ArrayList<>();

    public JavaTeam(String name) {
        this.name = name;
    }

    public JavaTeam() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JavaMember> getMembers() {
        return members;
    }

    public void setMembers(List<JavaMember> members) {
        this.members = members;
    }
}
