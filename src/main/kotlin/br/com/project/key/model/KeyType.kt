package br.com.project.key.model

import br.com.project.KeyRequest
import br.com.project.bcb.pix.KeyTypeBCB
import br.com.project.cellphone.CellphoneValidator
import br.com.project.document.Document
import br.com.project.email.EmailValidator

enum class KeyType {

    CPF {
        override fun valid(keyValue : String): Boolean = Document
            .de( keyValue )
            .match()
            .validateFirstDigit()
            .validateSecondDigit()
            .isValid()
    },
    NUMERO_CELULAR {
        override fun valid(keyValue : String): Boolean = CellphoneValidator.match( keyValue )
    },
    EMAIL {
        override fun valid(keyValue : String): Boolean = EmailValidator.match( keyValue )
    },
    CHAVE_ALEATORIA {
        override fun valid(keyValue : String): Boolean = keyValue.isBlank()
    };

    abstract fun valid( keyValue : String ) : Boolean

    fun toBCBKeyType(): KeyTypeBCB {
        return when( this ){
            CPF -> KeyTypeBCB.CPF
            NUMERO_CELULAR -> KeyTypeBCB.PHONE
            EMAIL -> KeyTypeBCB.EMAIL
            CHAVE_ALEATORIA -> KeyTypeBCB.RANDOM
        }
    }

    fun toKeyTypeResponse() : br.com.project.KeyType {
        return when( this ){
            CPF -> br.com.project.KeyType.CPF
            NUMERO_CELULAR -> br.com.project.KeyType.NUMERO_CELULAR
            EMAIL -> br.com.project.KeyType.EMAIL
            CHAVE_ALEATORIA -> br.com.project.KeyType.CHAVE_ALEATORIA
        }
    }

    companion object{
        fun from( keyType : br.com.project.KeyType ) : KeyType? {
            return when( keyType ){
                br.com.project.KeyType.CPF -> CPF
                br.com.project.KeyType.NUMERO_CELULAR -> NUMERO_CELULAR
                br.com.project.KeyType.EMAIL -> EMAIL
                br.com.project.KeyType.CHAVE_ALEATORIA -> CHAVE_ALEATORIA
                else -> null
            }
        }
    }

}