package br.com.project.email

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EmailTest {

    @Test
    fun `email matches regex`(){
        Assertions.assertTrue(
            EmailValidator
                .match("email@email.com")
        )
    }

    @Test
    fun `email don't matches regex`(){
        Assertions.assertFalse(
            EmailValidator
                .match("@")
        )
    }

    @Test
    fun `email instantiation`(){
        EmailValidator()
    }

}