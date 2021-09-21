package br.com.project.key.service

import br.com.project.account.AccountRepository
import br.com.project.erp.itau.ERPItau
import br.com.project.key.KeyRepository
import br.com.project.key.controlador.KeyResponseData
import br.com.project.key.controlador.KeyTransferObject

interface KeyService {

    fun register( keyDto : KeyTransferObject ) : KeyResponseData

    companion object{
        fun create( keyRepository: KeyRepository, accountRepository: AccountRepository, erpItau: ERPItau ) : KeyService {
            return KeyServiceValidator(
                KeyServiceImplementation( keyRepository, accountRepository, erpItau )
            )
        }
    }

}