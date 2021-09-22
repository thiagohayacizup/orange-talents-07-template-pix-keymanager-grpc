package br.com.project.key.service

import br.com.project.account.AccountRepository
import br.com.project.erp.itau.ERPItau
import br.com.project.key.Errors
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import io.micronaut.http.HttpStatus

class KeyServiceImplementation (
    private val keyRepository: KeyRepository,
    private val accountRepository: AccountRepository,
    private val itauErp : ERPItau
) : KeyService {

    override fun register( keyDto: KeyTransferObject): KeyResponseData {

        val buscarConta = itauErp.buscarConta(keyDto.clientId, keyDto.accountType.toString())

        if( buscarConta.status != HttpStatus.OK )
            return Errors.errorResponseERPItau

        val body = buscarConta.body() ?: return Errors.bodyNotFoundERPItau

        return KeyResponseData( key = keyDto.register( keyRepository, accountRepository, body ) )

    }

}