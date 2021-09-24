package br.com.project.bcb.pix

data class CreatePixKeyResponse(
    val key : String
)

data class DeletePixKeyResponse(
    val key : String
)

data class PixKeyDetailsResponse(
    val keyType : KeyTypeBCB,
    val key : String,
    val bankAccount: BankAccount,
    val owner: OwnerBCB,
    val createdAt : String
)
