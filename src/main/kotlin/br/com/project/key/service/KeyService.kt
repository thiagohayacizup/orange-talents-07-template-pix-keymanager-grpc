package br.com.project.key.service

import br.com.project.account.AccountRepository
import br.com.project.erp.itau.ERPItau
import br.com.project.key.model.KeyRepository
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyResponseData

interface KeyService {

    fun register( keyDto : KeyTransferObject) : KeyResponseData

    companion object{
        fun create(keyRepository: KeyRepository, accountRepository: AccountRepository, erpItau: ERPItau ) : KeyService {
            return KeyServiceValidator(
                KeyServiceImplementation( keyRepository, accountRepository, erpItau ),
                keyRepository
            )
        }
    }

}