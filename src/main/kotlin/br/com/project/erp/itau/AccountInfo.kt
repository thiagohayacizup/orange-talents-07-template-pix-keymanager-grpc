package br.com.project.erp.itau

data class AccountInfo(
    val instituicao : InstitutionResponse,
    val agencia : String,
    val numero : String,
    val titular : HolderResponse
)
