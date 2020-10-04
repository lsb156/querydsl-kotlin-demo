package com.ssabae.querydsl.demo.domain;

import javax.persistence.*;

/**
 * @author : leesangbae
 * @project : demo
 * @date : 2020-10-04
 */
@Entity
public class JavaMember {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private JavaTeam team;

    public JavaMember(String username) {
        this(username, 0);
    }

    public JavaMember(String username, int age) {
        this(username, age, null);
    }

    public JavaMember(String username, int age, JavaTeam team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    public JavaMember() {

    }

    public void changeTeam(JavaTeam team) {
        this.team = team;
        team.getMembers().add(this);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public JavaTeam getTeam() {
        return team;
    }

    public void setTeam(JavaTeam team) {
        this.team = team;
    }
}
