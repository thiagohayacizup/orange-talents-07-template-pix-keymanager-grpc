package br.com.project.document

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DocumentTest {

    @Test
    fun `valid cpf`(){
        Assertions.assertTrue(
            Document
                .de("14550522054")
                .match()
                .validateFirstDigit()
                .validateSecondDigit()
                .isValid()
        )
    }

    @Test
    fun `invalid cpf`(){
        Assertions.assertFalse(
            Document
                .de("14550522058")
                .match()
                .validateFirstDigit()
                .validateSecondDigit()
                .isValid()
        )
    }

    @Test
    fun `invalid cpf size less than 11`(){
        Assertions.assertFalse(
            Document
                .de("9999")
                .match()
                .validateFirstDigit()
                .validateSecondDigit()
                .isValid()
        )
    }

    @Test
    fun `invalid cpf size more than 11`(){
        Assertions.assertFalse(
            Document
                .de("99364564646456456456456499")
                .match()
                .validateFirstDigit()
                .validateSecondDigit()
                .isValid()
        )
    }

    @Test
    fun `invalid cpf same numbers`(){
        Assertions.assertFalse(
            Document
                .de("11111111111")
                .match()
                .validateFirstDigit()
                .validateSecondDigit()
                .isValid()
        )
    }

}