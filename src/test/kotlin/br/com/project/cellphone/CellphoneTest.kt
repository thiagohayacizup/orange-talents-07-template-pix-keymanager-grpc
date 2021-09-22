package br.com.project.cellphone

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CellphoneTest {

    @Test
    fun `phone match regex`(){
        Assertions.assertTrue(
            CellphoneValidator
                .match("+551112345678")
        )
    }

    @Test
    fun `phone don't match regex`(){
        Assertions.assertFalse(
            CellphoneValidator
                .match("456-1111")
        )
    }

    @Test
    fun `class instanciation`(){
        CellphoneValidator()
    }

}