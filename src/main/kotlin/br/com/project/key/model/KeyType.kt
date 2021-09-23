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

    companion object{
        fun from( keyType : KeyRequest.KeyType ) : KeyType? {
            return when( keyType ){
                KeyRequest.KeyType.CPF -> CPF
                KeyRequest.KeyType.NUMERO_CELULAR -> NUMERO_CELULAR
                KeyRequest.KeyType.EMAIL -> EMAIL
                KeyRequest.KeyType.CHAVE_ALEATORIA -> CHAVE_ALEATORIA
                else -> null
            }
        }
    }

}