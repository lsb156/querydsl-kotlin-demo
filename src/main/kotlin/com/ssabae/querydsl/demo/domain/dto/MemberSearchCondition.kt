package com.ssabae.querydsl.demo.domain.dto

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-08
 */

class MemberSearchCondition(
    val username: String? = null,
    val teamName: String? = null,
    val ageGoe: Int? = null,
    val ageLoe: Int? = null
) {
    override fun toString(): String {
        return "MemberSearchCondition(username=$username, teamName=$teamName, ageGoe=$ageGoe, ageLoe=$ageLoe)"
    }
}