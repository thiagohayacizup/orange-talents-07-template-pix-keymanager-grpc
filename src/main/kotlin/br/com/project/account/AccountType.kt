package br.com.project.account

import br.com.project.KeyRequest
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