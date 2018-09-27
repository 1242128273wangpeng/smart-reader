package com.ding.basic.db.migration

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
annotation class FieldMigration(val oldName: String, val converter: KClass<out DBFieldConverter<*,*>> = DefaultConverter::class)
