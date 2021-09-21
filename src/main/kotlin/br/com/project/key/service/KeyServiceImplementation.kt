package br.com.project.key.service

import br.com.project.account.AccountRepository
import br.com.project.erp.itau.AccountInfo
import br.com.project.erp.itau.ERPItau
import br.com.project.key.KeyRepository
import br.com.project.key.controlador.KeyResponseData
import br.com.project.key.controlador.KeyTransferObject
import io.grpc.Status
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class KeyServiceImplementation (
    private val keyRepository: KeyRepository,
    private val accountRepository: AccountRepository,
    private val itauErp : ERPItau
) : KeyService {

    override fun register(@Valid keyDto: KeyTransferObject): KeyResponseData {
        val buscarConta = itauErp.buscarConta(keyDto.clientId, keyDto.accountType.toString())
        if( buscarConta.status != HttpStatus.OK )
            return KeyResponseData( error = Status
                .fromCodeValue( buscarConta.code() )
                .withDescription( buscarConta.status.reason)
                .asRuntimeException()
            )
        val body = buscarConta.body() ?: return KeyResponseData(error = Status
                .NOT_FOUND
                .withDescription("Response not found.")
                .asRuntimeException()
            )
        return KeyResponseData( key = keyDto.register( keyRepository, accountRepository, body ) )
    }

}