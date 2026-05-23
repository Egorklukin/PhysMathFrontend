package com.example.physmath.data.entities

fun Topic.toEntity(): TopicEntity {
    return TopicEntity(
        id = this.id,
        title = this.title,
        subjectId = this.subjectId
    )
}

fun TopicEntity.toDomain(): Topic {
    return Topic(
        id = this.id,
        title = this.title,
        subjectId = this.subjectId
    )
}

fun List<Topic>.toEntities(): List<TopicEntity> = this.map { it.toEntity() }
fun List<TopicEntity>.toDomains(): List<Topic> = this.map { it.toDomain() }