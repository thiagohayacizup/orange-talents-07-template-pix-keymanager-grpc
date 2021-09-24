package br.com.project.bcb.pix

import br.com.project.account.AccountType
import br.com.project.key.model.KeyType

data class CreatePixKeyRequest(
    val keyType : KeyTypeBCB,
    val key : String,
    val bankAccount : BankAccount,
    val owner : OwnerBCB
)

data class BankAccount(
    val participant : String,
    val branch : String,
    val accountNumber : String,
    val accountType : AccountTypeBCB
)

data class OwnerBCB(
    val type : OwnerType,
    val name : String,
    val taxIdNumber : String
)

enum class KeyTypeBCB { CPF, PHONE, EMAIL, RANDOM;

    fun toKeyType() : KeyType{
        return when(this){
            CPF -> KeyType.CPF
            PHONE -> KeyType.NUMERO_CELULAR
            EMAIL -> KeyType.EMAIL
            RANDOM -> KeyType.CHAVE_ALEATORIA
        }
    }

}

enum class AccountTypeBCB{ CACC, SVGS;

    fun toAccountType() : AccountType{
        return when(this){
            CACC -> AccountType.CONTA_CORRENTE
            SVGS -> AccountType.CONTA_POUPANCA
        }
    }

}

enum class OwnerType{ NATURAL_PERSON }

data class DeletePixKeyRequest(
    val key : String,
    val participant: String
)