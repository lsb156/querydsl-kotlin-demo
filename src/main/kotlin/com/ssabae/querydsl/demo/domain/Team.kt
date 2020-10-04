package com.ssabae.querydsl.demo.domain

import javax.persistence.*

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-04
 */
@Entity
class Team(
    @Id
    @GeneratedValue
    var id: Long? = null,

    var name: String? = null,

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    var members: MutableList<Member> = mutableListOf()
) {
    override fun toString(): String {
        return "Team(id=$id, name=$name)"
    }
}