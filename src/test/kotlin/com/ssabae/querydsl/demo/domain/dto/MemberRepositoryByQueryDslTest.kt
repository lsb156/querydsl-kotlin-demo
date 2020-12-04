package com.ssabae.querydsl.demo.domain.dto

import com.ssabae.querydsl.demo.domain.Member
import com.ssabae.querydsl.demo.domain.QMember.member
import com.ssabae.querydsl.demo.domain.Team
import com.ssabae.querydsl.demo.domain.repository.MemberRepository
import com.ssabae.querydsl.demo.domain.repository.MemberRepositoryByQueryDsl
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-08
 */
@SpringBootTest
@Transactional
internal class MemberRepositoryByQueryDslTest {

    @Autowired
    private lateinit var memberRepositoryByQueryDsl: MemberRepositoryByQueryDsl


    @Autowired
    private lateinit var memberRepository: MemberRepository


    @Autowired
    private lateinit var em: EntityManager

    @Test
    fun searchByBuilder() {

        val teamA = Team(name = "A")
        val teamB = Team(name = "B")
        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member(name = "member1", age = 10, team = teamA)
        val member2 = Member(name = "member2", age = 20, team = teamA)

        val member3 = Member(name = "member3", age = 30, team = teamB)
        val member4 = Member(name = "member4", age = 40, team = teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        val searchCondition = MemberSearchCondition(
            ageGoe = 35,
            ageLoe = 40,
            teamName = "B"
        )
        val searchByBuilder = memberRepositoryByQueryDsl.searchByBuilder(searchCondition)

        searchByBuilder.forEach { println(it) }
        assertThat(searchByBuilder).extracting("username")
            .containsExactly("member4")
    }



    @Test
    fun searchByWhere() {

        val teamA = Team(name = "A")
        val teamB = Team(name = "B")
        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member(name = "member1", age = 10, team = teamA)
        val member2 = Member(name = "member2", age = 20, team = teamA)

        val member3 = Member(name = "member3", age = 30, team = teamB)
        val member4 = Member(name = "member4", age = 40, team = teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        val searchCondition = MemberSearchCondition(
            ageGoe = 35,
            ageLoe = 40,
            teamName = "B"
        )
        val searchByBuilder = memberRepositoryByQueryDsl.searchByWhere(searchCondition)

        searchByBuilder.forEach { println(it) }
        assertThat(searchByBuilder).extracting("username")
            .containsExactly("member4")
    }

    @Test
    fun querydslPredicateExecutorTest() {
        val members = memberRepository.findAll(
            member.age.between(20, 40)
                .and(member.name.eq("member1"))
        )
    }


}