package br.com.project.key.validator

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@ReportAsSingleViolation
@Constraint( validatedBy = [] )
@Pattern(
    regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
    flags = [Pattern.Flag.CASE_INSENSITIVE]
)
@Retention( AnnotationRetention.RUNTIME )
@Target( AnnotationTarget.FIELD )
annotation class UUIDValid(
    val message : String = "UUID invalid",
    val groups : Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)
