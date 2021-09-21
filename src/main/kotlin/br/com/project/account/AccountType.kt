package br.com.project.account

import br.com.project.KeyRequest

enum class AccountType {

    CONTA_CORRENTE,
    CONTA_POUPANCA;

    companion object{
        fun from( accountType : KeyRequest.AccountType ) : AccountType? {
            return when( accountType ){
                KeyRequest.AccountType.CONTA_CORRENTE -> CONTA_CORRENTE
                KeyRequest.AccountType.CONTA_POUPANCA -> CONTA_POUPANCA
                else -> null
            }
        }
    }

}