package com.ssabae.querydsl.demo

import com.querydsl.core.Tuple
import com.querydsl.jpa.impl.JPAQueryFactory
import com.ssabae.querydsl.demo.domain.Member
import com.ssabae.querydsl.demo.domain.QMember
import com.ssabae.querydsl.demo.domain.QMember.member
import com.ssabae.querydsl.demo.domain.QTeam.team
import com.ssabae.querydsl.demo.domain.Team
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-04
 */
@SpringBootTest
@Transactional
internal class QuerydslTest {

    @PersistenceUnit
    lateinit var emf: EntityManagerFactory

    @Autowired
    private lateinit var em: EntityManager

    lateinit var queryFactory: JPAQueryFactory

    @BeforeEach
    fun before() {
        queryFactory = JPAQueryFactory(em)

        val teamA = Team(name = "A")
        val teamB = Team(name = "B")
        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member().apply { name = "member1"; age = 10; team = teamA }
        val member2 = Member().apply { name = "member2"; age = 20; team = teamA }

        val member3 = Member().apply { name = "member3"; age = 30; team = teamB }
        val member4 = Member().apply { name = "member4"; age = 40; team = teamB }

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)
    }


    @Test
    fun fetchJoin() {
        em.flush()
        em.clear()

        val selectedMember = queryFactory
            .selectFrom(member)
            .where(member.name.eq("member1"))
            .fetchOne()

        val loaded = emf.persistenceUnitUtil.isLoaded(selectedMember?.team)

        assertThat(loaded).isFalse()
    }



}