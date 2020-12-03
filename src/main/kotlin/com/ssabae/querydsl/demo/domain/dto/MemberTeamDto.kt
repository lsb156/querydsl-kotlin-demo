package com.ssabae.querydsl.demo.domain.dto

import com.querydsl.core.annotations.QueryProjection

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-08
 */
class MemberTeamDto @QueryProjection constructor(
    var memberId: Long? = null,
    var username: String? = null,
    var age: Int? = null,
    var teamId: Long? = null,
    var teamName: String? = null
) {

    override fun toString(): String {
        return "MemberTeamDto(memberId=$memberId, username=$username, age=$age, teamId=$teamId, teamName=$teamName)"
    }
}