package com.ssabae.querydsl.demo.domain.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager

/**
 * @project : demo
 * @author  : leesangbae
 * @date    : 2020-10-09
 */
@Repository
class QueryDsl4RepositorySupport(


    val entityManager: EntityManager,
) {
    val domainClass: Class
    val querydsl: Querydsl
    val queryFactory = JPAQueryFactory(entityManager)

}