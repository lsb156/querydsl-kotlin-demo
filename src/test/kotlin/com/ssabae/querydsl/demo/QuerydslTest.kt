package com.ssabae.querydsl.demo

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.Tuple
import com.querydsl.core.types.Expression
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.ssabae.querydsl.demo.domain.Member
import com.ssabae.querydsl.demo.domain.QMember
import com.ssabae.querydsl.demo.domain.QMember.member
import com.ssabae.querydsl.demo.domain.QTeam.team
import com.ssabae.querydsl.demo.domain.Team
import com.ssabae.querydsl.demo.domain.dto.MemberDto
import com.ssabae.querydsl.demo.domain.dto.QMemberDto
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
    private lateinit var emf: EntityManagerFactory

    @Autowired
    private lateinit var em: EntityManager

    lateinit var queryFactory: JPAQueryFactory
//    private val queryFactory by lazy { JPAQueryFactory(em) }

    @BeforeEach
    fun before() {

        queryFactory = JPAQueryFactory(em)

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
    }

    @Test
    fun jpqlTest() {
        val member = em.createQuery("select m from Member m where m.name = :name", Member::class.java)
            .setParameter("name", "member1")
            .singleResult
        assertThat("member1").isEqualTo(member.name)
        println("member = ${member}")
    }

    @Test
    fun querydslTest() {
        val m = member
        val member = queryFactory
            .select(m)
            .from(m)
            .where(m.name.eq("member1"))
            .fetchOne()
        assertThat("member1").isEqualTo(member?.name)
        println("member = ${member}")
    }

    @Test
    fun ordering() {
        em.persist(Member(name = "member5", age = 100))
        em.persist(Member(name = "member6", age = 100))
        em.persist(Member(age = 100))

        val fetch = queryFactory
            .select(member)
            .from(member)
            .where(member.age.eq(100))
            .orderBy(member.age.asc(), member.name.asc().nullsLast())
            .fetch()

        val member5 = fetch[0]
        val member6 = fetch[1]
        val memberNull = fetch[2]

        assertThat(member5.name).isEqualTo("member5")
        assertThat(member6.name).isEqualTo("member6")
        assertThat(memberNull.name).isNull()
    }

    @Test
    fun paging() {

        val members = queryFactory.selectFrom(member)
            .offset(1)
            .limit(2)
            .fetch()

        assertThat(members.size).isEqualTo(2)
    }

    @Test
    fun aggregation() {
        val result: List<Tuple> = queryFactory
            .select(
                member.age.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max( ),
                member.age.min()
            )
            .from(member)
            .fetch()

        val tuple = result[0]
        assertThat(tuple.get(member.age.count())).isEqualTo(4)
        assertThat(tuple.get(member.age.sum())).isEqualTo(100)
        assertThat(tuple.get(member.age.avg())).isEqualTo(25.0)
        assertThat(tuple.get(member.age.max())).isEqualTo(40)
        assertThat(tuple.get(member.age.min())).isEqualTo(10)
    }

    @Test
    fun group() {
        val result: List<Tuple> = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .having(member.age.avg().gt(10))
            .fetch()

        val teamA = result[0]
        val teamB = result[1]

        assertThat(teamA.get(team.name)).isEqualTo("A")
        assertThat(teamA.get(member.age.avg())).isEqualTo(15.0)

        assertThat(teamB.get(team.name)).isEqualTo("B")
        assertThat(teamB.get(member.age.avg())).isEqualTo(35.0)
    }

    @Test
    fun join() {
        val members = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("A"))
            .fetch()

        assertThat(members).extracting("name")
            .containsExactly("member1", "member2")

    }

    @Test
    fun theta_join() {
        em.persist(Member(name = "A"))
        em.persist(Member(name = "B"))
        em.persist(Member(name = "C"))

        val members = queryFactory
            .select(member)
            .from(member, team)
            .where(member.name.eq(team.name))
            .fetch()

        assertThat(members).extracting("name")
            .containsExactly("A", "B")
    }

    @Test
    fun join_on_filtering() {
        val members = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("A"))
            .fetch()

        val members1 = queryFactory
            .select(member, team)
            .from(member)
            .join(member.team, team).on(team.name.eq("A"))
            .fetch()

        val members2 = queryFactory
            .select(member, team)
            .from(member)
            .join(member.team, team)
            .where(team.name.eq("A"))
            .fetch()

        members1.forEach { println("it = ${it}") }
        members2.forEach { println("it = ${it}") }

    }

    @Test
    fun join_on_no_relation() {
        em.persist(Member(name = "A"))
        em.persist(Member(name = "B"))
        em.persist(Member(name = "C"))

        val members = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team) // leftJoin(member.team, team) 으로 들어가게되면 ID 끼리 매칭이 되어버려 theta join 형태로 되지 않는다.
            .on(member.name.eq(team.name))
            .fetch()

        members.forEach { println("it = ${it}") }
    }

    @Test
    fun fetchJoin_notUse() {
        em.flush()
        em.clear()

        val selectedMember = queryFactory
            .selectFrom(member)
            .where(member.name.eq("member1"))
            .fetchOne()

        val loaded = emf.persistenceUnitUtil.isLoaded(selectedMember?.team)

        assertThat(loaded).isFalse()
    }

    @Test
    fun fetchJoin_Use() {
        em.flush()
        em.clear()

        val selectedMember = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.name.eq("member1"))
            .fetchOne()

        val loaded = emf.persistenceUnitUtil.isLoaded(selectedMember?.team)

        assertThat(loaded).isTrue()
    }

    @Test
    fun subQuery() {
        val subMember = QMember("subMember")
        val fetch = queryFactory
            .selectFrom(member)
            .where(
                member.age.eq(
                    JPAExpressions
                        .select(subMember.age.max())
                        .from(subMember)
                )
            )
            .fetch()

        val olderMember = fetch[0]
        assertThat(olderMember.name).isEqualTo("member4")
        assertThat(olderMember.age).isEqualTo(40)
    }

    @Test
    fun subQueryInCase() {
        val subMember = QMember("subMember")
        val fetch = queryFactory
            .selectFrom(member)
            .where(
                member.age.`in`(
                    JPAExpressions
                        .select(subMember.age)
                        .from(subMember)
                        .where(subMember.age.gt(10))
                )
            )
            .fetch()

        assertThat(fetch).extracting("age")
            .containsExactly(20, 30, 40)
    }

    @Test
    fun selectSubQuery() {
        val subMember = QMember("subMember")
        val fetch = queryFactory
            .select(
                member.name,
                JPAExpressions
                    .select(subMember.age.avg())
                    .from(subMember)
            )
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }

    @Test
    fun simpleCase() {
        val fetch = queryFactory
            .select(
                member.age
                    .`when`(10).then("열살")
                    .`when`(20).then("스무살")
                    .otherwise("기타")
            )
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }

    @Test
    fun complexCase() {
        val fetch = queryFactory
            .select(CaseBuilder()
                .`when`(member.age.between(0, 20)).then("0~20")
                .`when`(member.age.between(21, 30)).then("21~30")
                .otherwise("기타")
            )
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }

    @Test
    fun concat() {
        val fetch = queryFactory
            .select(member.name.concat("_").concat(member.age.stringValue()))
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }

    @Test
    fun projectionDto_Constructor() {
        val fetch = queryFactory
            .select(
                Projections.constructor(
                    MemberDto::class.java,
                    member.name,
                    member.age
                )
            )
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }

    @Test
    fun projectionDto_Setter() {
        val fetch = queryFactory
            .select(
                Projections.bean(
                    MemberDto::class.java,
                    member.name,
                    member.age
                )
            )
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }

    @Test
    fun projectionDto_Field() {
        val subMember = QMember("subMember")
        val fetch = queryFactory
            .select(
                Projections.fields(
                    MemberDto::class.java,
                    member.name.`as`("username"),
                    ExpressionUtils.`as`(
                        JPAExpressions
                            .select(subMember.age.max())
                            .from(subMember)
                        ,"age"
                    )
                )
            )
            .from(member)
            .fetch()

        fetch.forEach { println(it) }
    }


    @Test
    fun projectionDto_QDto() {
        val members = queryFactory
            .select(QMemberDto(member.name, member.age))
            .from(member)
            .fetch()
        members.forEach { println("it = $it") }
    }

    @Test
    fun distinct() {
        val members = queryFactory
            .select(member).distinct()
            .from(member)
            .fetch()
    }


    @Test
    fun dynamicQuery_BooleanBuilder() {
        val username = "member1"
        val age = 10
        val members = searchMember1(username, age)
        members.forEach { println(it) }

        assertThat(members[0].name).isEqualTo(username)
        assertThat(members[0].age).isEqualTo(age)

    }

    private fun searchMember1(username: String?, age: Int?): List<Member> {
        val booleanBuilder = BooleanBuilder()
        username?.let {
            booleanBuilder.and(member.name.eq(username))
        }
        age?.let {
            booleanBuilder.and(member.age.eq(age))
        }
        return queryFactory
            .selectFrom(member)
            .where(booleanBuilder)
            .fetch()
    }



    @Test
    fun dynamicQuery_WhereParam() {
        val username = "member1"
        val age = 10
        val members = searchMember2(username, age)
        members.forEach { println(it) }

        assertThat(members[0].name).isEqualTo(username)
        assertThat(members[0].age).isEqualTo(age)

    }

    private fun searchMember2(username: String?, age: Int?): List<Member> {
        return queryFactory
            .selectFrom(member)
            .where(usernameEq(username), ageEq(age))
            .fetch()
    }

    private fun usernameEq(username: String?): BooleanExpression? {
        return if (username == null) null
        else member.name.eq(username)
    }

    private fun ageEq(age: Int?): BooleanExpression? {
        return if (age == null) null
        else member.age.eq(age)
    }

    private fun allEq(username: String?, age: Int?): BooleanExpression? {
        return usernameEq(username)?.and(ageEq(age))
    }


    @Test
    fun bulkUpdate() {
        val count = queryFactory
            .update(member)
            .set(member.name, "비회원")
            .where(member.age.lt(29))
            .execute()

        println("count = ${count}")
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun bulkAdd() {
        val count = queryFactory
            .update(member)
            // member.age.multiply, member.age.divide
            .set(member.age, member.age.add(1))
            .where(member.age.lt(29))
            .execute()

        println("count = ${count}")
        assertThat(count).isEqualTo(2)
    }


    @Test
    fun bulkDelete() {
        val count = queryFactory
            .delete(member)
            .where(member.age.lt(29))
            .execute()

        println("count = ${count}")
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun functionCall() {
        val members = queryFactory
            .select(
                Expressions.stringTemplate(
                    "function('replace', {0}, {1}, {2})",
                    member.name,
                    "member",
                    "M"
                )
            )
            .from(member)
            .fetch()

        members.forEach { println(it) }
        assertThat(members).containsExactly("M1", "M2", "M3", "M4")
    }

    @Test
    fun selectLowerMember() {
        val members = queryFactory
            .selectFrom(member)
            .where(member.name.eq(member.name.lower()))
            .fetch()
    }
}