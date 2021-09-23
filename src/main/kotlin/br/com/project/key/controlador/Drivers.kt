package br.com.project.key.controlador

import br.com.project.account.AccountRepository
import br.com.project.bcb.pix.BCBPix
import br.com.project.erp.itau.ERPItau
import br.com.project.key.model.KeyRepository

class Drivers private constructor( builder : Builder ) {

    companion object{
        fun builder() : Builder = Builder()
    }

    val keyRepository: KeyRepository
    val accountRepository: AccountRepository
    val erpItau: ERPItau
    val bcbPix: BCBPix

    init{
        keyRepository = builder.keyRepository
        accountRepository = builder.accountRepository
        erpItau = builder.erpItau
        bcbPix = builder.bcbPix
    }

    class Builder{
        lateinit var keyRepository: KeyRepository private set
        lateinit var accountRepository: AccountRepository private set
        lateinit var erpItau: ERPItau private set
        lateinit var bcbPix: BCBPix private set

        fun withKeyRepository( keyRepository: KeyRepository) : Builder{
            this.keyRepository = keyRepository
            return this
        }

        fun withAccountRepository( accountRepository: AccountRepository) : Builder{
            this.accountRepository = accountRepository
            return this
        }

        fun withERPItau( erpItau: ERPItau) : Builder{
            this.erpItau = erpItau
            return this
        }

        fun withBCBPix( bcbPix: BCBPix) : Builder{
            this.bcbPix = bcbPix
            return this
        }

        fun build() : Drivers = Drivers( this )

    }

}