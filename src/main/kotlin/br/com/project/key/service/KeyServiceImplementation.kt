package br.com.project.key.service

import br.com.project.key.Errors
import br.com.project.key.controlador.Drivers
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyResponseData
import io.micronaut.http.HttpStatus

class KeyServiceImplementation( private val drivers: Drivers ) : KeyService {

    override fun register( keyDto: KeyTransferObject): KeyResponseData {

        val findAccount = drivers.erpItau.findAccount(keyDto.clientId, keyDto.accountType.toString())

        if( findAccount.status != HttpStatus.OK )
            return Errors.errorResponseERPItau

        val body = findAccount.body() ?: return Errors.bodyNotFoundERPItau

        val createKey = drivers.bcbPix.createKey( keyDto.toBCBRequest( body ) )

        if( createKey.status != HttpStatus.CREATED )
            return Errors.errorBcb

        val bcbResponse = createKey.body() ?: return Errors.bodyBcbNotFound

        keyDto.updateToBCBKey( bcbResponse.key )

        return KeyResponseData(
            key = keyDto.register( drivers.keyRepository, drivers.accountRepository, body )
        )

    }

}