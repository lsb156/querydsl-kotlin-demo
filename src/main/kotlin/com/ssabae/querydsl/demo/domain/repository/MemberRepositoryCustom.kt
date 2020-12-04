package com.ssabae.querydsl.demo.domain.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.ssabae.querydsl.demo.domain.Member
import com.ssabae.querydsl.demo.domain.QMember.*
import com.ssabae.querydsl.demo.domain.QTeam.*
import com.ssabae.querydsl.demo.domain.dto.MemberSearchCondition
import com.ssabae.querydsl.demo.domain.dto.MemberTeamDto
import com.ssabae.querydsl.demo.domain.dto.QMemberTeamDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.support.PageableExecutionUtils
import javax.persistence.EntityManager

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-09
 */
interface MemberRepositoryCustom {
    fun search(searchCondition: MemberSearchCondition): List<MemberTeamDto>
    fun searchSimple(searchCondition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
    fun searchComplex(searchCondition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto>
}

class MemberRepositoryImpl(
    private val em: EntityManager
): MemberRepositoryCustom {

    private val queryFactory = JPAQueryFactory(em)

    override fun search(searchCondition: MemberSearchCondition): List<MemberTeamDto> {
        return queryFactory.select(
            QMemberTeamDto(
                member.id.`as`("memberId"),
                member.name.`as`("username"),
                member.age,
                team.id.`as`("teamId"),
                team.name.`as`("teamName")
            )
        )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                nameEquals(searchCondition.username),
                teamNameEquals(searchCondition.teamName),
                ageGoe(searchCondition.ageGoe),
                ageLoe(searchCondition.ageLoe)
            )
            .fetch()
    }

    override fun searchSimple(searchCondition: MemberSearchCondition, pageable: Pageable): PageImpl<MemberTeamDto> {
        val results = queryFactory.select(
            QMemberTeamDto(
                member.id.`as`("memberId"),
                member.name.`as`("username"),
                member.age,
                team.id.`as`("teamId"),
                team.name.`as`("teamName"))
        )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                nameEquals(searchCondition.username),
                teamNameEquals(searchCondition.teamName),
                ageGoe(searchCondition.ageGoe),
                ageLoe(searchCondition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()

        val content = results.results
        return PageImpl(content, pageable, results.total)
    }

    override fun searchComplex(searchCondition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        val content = queryFactory
            .select(
                QMemberTeamDto(
                member.id.`as`("memberId"),
                member.name.`as`("username"),
                member.age,
                team.id.`as`("teamId"),
                team.name.`as`("teamName"))
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                nameEquals(searchCondition.username),
                teamNameEquals(searchCondition.teamName),
                ageGoe(searchCondition.ageGoe),
                ageLoe(searchCondition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val count = queryFactory
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                nameEquals(searchCondition.username),
                teamNameEquals(searchCondition.teamName),
                ageGoe(searchCondition.ageGoe),
                ageLoe(searchCondition.ageLoe)
            )
            .fetchCount()

        return PageImpl(content, pageable, count)
    }


    fun searchByUseExecutionUtils(searchCondition: MemberSearchCondition, pageable: Pageable): Page<MemberTeamDto> {
        val content = queryFactory
            .select(
                QMemberTeamDto(
                member.id.`as`("memberId"),
                member.name.`as`("username"),
                member.age,
                team.id.`as`("teamId"),
                team.name.`as`("teamName"))
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                nameEquals(searchCondition.username),
                teamNameEquals(searchCondition.teamName),
                ageGoe(searchCondition.ageGoe),
                ageLoe(searchCondition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = queryFactory
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                nameEquals(searchCondition.username),
                teamNameEquals(searchCondition.teamName),
                ageGoe(searchCondition.ageGoe),
                ageLoe(searchCondition.ageLoe)
            )

        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.fetchCount()
        }
    }



    private fun nameEquals(username: String?): BooleanExpression? {
        return username?.let { member.name.eq(username) }
    }

    private fun teamNameEquals(teamName: String?): BooleanExpression? {
        return teamName?.let { team.name.eq(teamName) }
    }

    private fun ageGoe(age: Int?): BooleanExpression? {
        return age?.let { member.age.goe(age) }
    }

    private fun ageLoe(age: Int?): BooleanExpression? {
        return age?.let { member.age.loe(age) }
    }


    fun orderTest(pageable: Pageable): List<Member> {
        val query = queryFactory.selectFrom(member)
        pageable.sort.forEach {
            query.orderBy(OrderSpecifier(
                if (it.isAscending) com.querydsl.core.types.Order.ASC
                else com.querydsl.core.types.Order.DESC,
                PathBuilder(member.type, member.metadata).get(it.property) as PathBuilder<Nothing>
            ))
        }
        return query.fetch()
    }



}

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member>