package br.com.project.key.service

import br.com.project.account.AccountRepository
import br.com.project.bcb.pix.BCBPix
import br.com.project.erp.itau.ERPItau
import br.com.project.key.model.KeyRepository
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyResponseData

interface KeyService {

    fun register( keyDto : KeyTransferObject) : KeyResponseData

    companion object{
        class Builder{
            private lateinit var keyRepository: KeyRepository
            private lateinit var accountRepository: AccountRepository
            private lateinit var erpItau: ERPItau
            private lateinit var bcbPix: BCBPix

            fun withKeyRepository( keyRepository: KeyRepository ) : Builder{
                this.keyRepository = keyRepository
                return this
            }

            fun withAccountRepository( accountRepository: AccountRepository ) : Builder{
                this.accountRepository = accountRepository
                return this
            }

            fun withERPItau( erpItau: ERPItau ) : Builder{
                this.erpItau = erpItau
                return this
            }

            fun withBCBPix( bcbPix: BCBPix ) : Builder{
                this.bcbPix = bcbPix
                return this
            }

            fun build() : KeyService{
                return KeyServiceValidator(
                    KeyServiceImplementation
                        .builder()
                        .withKeyRepository( keyRepository )
                        .withAccountRepository( accountRepository )
                        .withERPItau( erpItau )
                        .withBCBPix( bcbPix )
                        .build(),
                    keyRepository
                )
            }

        }
    }

}