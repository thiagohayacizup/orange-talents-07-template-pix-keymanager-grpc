package br.com.project.account

import br.com.project.bcb.pix.AccountTypeBCB

enum class AccountType {

    CONTA_CORRENTE,
    CONTA_POUPANCA;

    fun toBCBAccountType(): AccountTypeBCB {
        return when( this ){
            CONTA_CORRENTE -> AccountTypeBCB.CACC
            CONTA_POUPANCA -> AccountTypeBCB.SVGS
        }
    }

    fun toAccountTypeResponse() : br.com.project.AccountType{
        return when( this ){
            CONTA_CORRENTE -> br.com.project.AccountType.CONTA_CORRENTE
            CONTA_POUPANCA -> br.com.project.AccountType.CONTA_POUPANCA
        }
    }

    companion object{
        fun from( accountType : br.com.project.AccountType ) : AccountType? {
            return when( accountType ){
                br.com.project.AccountType.CONTA_CORRENTE -> CONTA_CORRENTE
                br.com.project.AccountType.CONTA_POUPANCA -> CONTA_POUPANCA
                else -> null
            }
        }
    }

}