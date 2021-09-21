package br.com.project.key

import br.com.project.KeyRequest

enum class KeyType {

    NULL {
        override fun valid(keyValue : String): Boolean { return false }
    },
    CPF {
        override fun valid(keyValue : String): Boolean { return true }
    },
    NUMERO_CELULAR {
        override fun valid(keyValue : String): Boolean { return true }
    },
    EMAIL {
        override fun valid(keyValue : String): Boolean { return true }
    },
    CHAVE_ALEATORIA {
        override fun valid(keyValue : String): Boolean { return true }
    };

    abstract fun valid( keyValue : String ) : Boolean

    companion object{
        fun from( keyType : KeyRequest.KeyType ) : KeyType {
            return when( keyType ){
                KeyRequest.KeyType.CPF -> CPF
                KeyRequest.KeyType.NUMERO_CELULAR -> NUMERO_CELULAR
                KeyRequest.KeyType.EMAIL -> EMAIL
                KeyRequest.KeyType.CHAVE_ALEATORIA -> CHAVE_ALEATORIA
                else -> NULL
            }
        }
    }

}