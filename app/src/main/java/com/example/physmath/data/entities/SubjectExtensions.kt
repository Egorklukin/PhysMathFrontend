package com.example.physmath.data.entities

fun Subject.toEntity(): SubjectEntity {
    return SubjectEntity(
        id = this.id,
        title = this.title,
        icon = this.icon,
        color = this.color
    )
}

fun SubjectEntity.toDomain(): Subject {
    return Subject(
        id = this.id,
        title = this.title,
        icon = this.icon,
        color = this.color
    )
}

fun List<Subject>.toEntities(): List<SubjectEntity> = this.map { it.toEntity() }
fun List<SubjectEntity>.toDomains(): List<Subject> = this.map { it.toDomain() }