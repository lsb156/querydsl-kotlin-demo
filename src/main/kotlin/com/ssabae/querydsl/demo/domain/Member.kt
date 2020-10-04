package com.ssabae.querydsl.demo.domain

import javax.persistence.*

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-04
 */
@Entity
class Member(
    @Id
    @GeneratedValue
    var id: Long? = null,

    var name: String? = null,

    var age: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team: Team? = null
) {

    fun changeTeam(team: Team) {
        this.team = team
        team.members.toMutableList().add(this)
    }

    override fun toString(): String {
        return "Member(id=$id, name=$name, age=$age)"
    }
}