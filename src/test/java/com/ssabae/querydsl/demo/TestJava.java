package com.ssabae.querydsl.demo;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssabae.querydsl.demo.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;

import static com.ssabae.querydsl.demo.domain.QJavaMember.*;
import static com.ssabae.querydsl.demo.domain.QMember.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : leesangbae
 * @project : demo
 * @date : 2020-10-04
 */
@SpringBootTest
@Transactional
public class TestJava {

    @PersistenceUnit
    EntityManagerFactory emf;

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory = null;

    @BeforeEach
    void before() {

        queryFactory = new JPAQueryFactory(em);

        JavaTeam teamA = new JavaTeam("A");
        JavaTeam teamB = new JavaTeam("B");
        em.persist(teamA);
        em.persist(teamB);

        JavaMember member1 = new JavaMember("member1", 10, teamA);
        JavaMember member2 = new JavaMember("member2", 20, teamA);

        JavaMember member3 = new JavaMember("member3", 30, teamB);
        JavaMember member4 = new JavaMember("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }


    @Test
    void test() {
        em.flush();
        em.clear();

        JavaMember member1 = queryFactory
                .selectFrom(javaMember)
                .where(javaMember.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).isFalse();

    }


//    @PersistenceUnit
//    lateinit var emf: EntityManagerFactory
//    @Autowired
//    private lateinit var em: EntityManager
//
//    lateinit var queryFactory: JPAQueryFactory



}
