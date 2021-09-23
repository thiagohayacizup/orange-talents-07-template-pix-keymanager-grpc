package br.com.project.bcb.pix

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

enum class KeyTypeBCB { CPF, PHONE, EMAIL, RANDOM }

enum class AccountTypeBCB{ CACC, SVGS }

enum class OwnerType{ NATURAL_PERSON, LEGAL_PERSON }