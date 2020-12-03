package com.ssabae.querydsl.demo.domain.dto

import com.querydsl.core.annotations.QueryProjection

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-05
 */

class MemberDto @QueryProjection constructor(
    val name: String? = null,
    val age: Int? = null
) {
    override fun toString(): String {
        return "MemberDto(name=$name, age=$age)"
    }
}