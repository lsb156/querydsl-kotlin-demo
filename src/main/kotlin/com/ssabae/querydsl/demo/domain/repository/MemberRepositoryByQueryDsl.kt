package com.ssabae.querydsl.demo.domain.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.ssabae.querydsl.demo.domain.QMember.*
import com.ssabae.querydsl.demo.domain.QTeam.*
import com.ssabae.querydsl.demo.domain.dto.MemberSearchCondition
import com.ssabae.querydsl.demo.domain.dto.MemberTeamDto
import com.ssabae.querydsl.demo.domain.dto.QMemberTeamDto
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-08
 */
@Repository
class MemberRepositoryByQueryDsl(
    private val em: EntityManager
) {

    private val queryFactory = JPAQueryFactory(em)

    fun searchByBuilder(condition: MemberSearchCondition): List<MemberTeamDto> {

        val builder = BooleanBuilder()

        condition.run {
            if (username.isNullOrEmpty().not())
                builder.and(member.name.eq(condition.username))
            if (teamName.isNullOrEmpty().not())
                builder.and(team.name.eq(condition.teamName))
            ageGoe?.let { builder.and(member.age.goe(condition.ageGoe)) }
            ageLoe?.let { builder.and(member.age.loe(condition.ageLoe)) }
            null
        }

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
            .where(builder)
            .fetch()
    }


    fun searchByWhere(condition: MemberSearchCondition): List<MemberTeamDto> {
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
                nameEquals(condition.username),
                teamNameEquals(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )
            .fetch()
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

}