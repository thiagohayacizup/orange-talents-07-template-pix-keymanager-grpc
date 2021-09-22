package br.com.project.key.controlador

import br.com.project.KeyDeleteRequest
import br.com.project.KeyRequest
import br.com.project.KeyResponse
import br.com.project.PixKeyManagerGrpc
import br.com.project.account.AccountRepository
import br.com.project.erp.itau.ERPItau
import br.com.project.key.Errors
import br.com.project.key.controlador.delete.key.KeyDeleteTransferObject
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import br.com.project.key.service.KeyService
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Valid

@Singleton
@Validated
class KeyManagerServer(
    private val keyRepository: KeyRepository,
    private val accountRepository: AccountRepository,
    private val itauErp : ERPItau
) : PixKeyManagerGrpc.PixKeyManagerImplBase() {

    override fun registerKey(request: KeyRequest?, responseObserver: StreamObserver<KeyResponse>?) {

        try {
            request
                ?.toKeyTranferObject()
                ?.let { register( it ) }
                ?.ifHasError { responseObserver?.onError( this.error ) }
                ?.otherwiseReturnKey {
                    responseObserver?.onNext(
                        KeyResponse
                            .newBuilder()
                            .setPixKey(this.key?.id.toString())
                            .setClientId(this.key?.clientId)
                            .build()
                    )
                    responseObserver?.onCompleted()
                }
        }catch ( exception: ConstraintViolationException ){
            responseObserver?.onError(
                Errors
                    .errorConstraintViolation( exception.constraintViolations.iterator().next().message )
            )
        }

    }

    fun register( @Valid keyDto : KeyTransferObject ) : KeyResponseData {
        return KeyService.create(keyRepository, accountRepository, itauErp).register( keyDto )
    }

    override fun deleteKey(request: KeyDeleteRequest?, responseObserver: StreamObserver<KeyResponse>?) {

        try{
            request
                ?.toKeyTransferObject()
                ?.let { delete( it ) }
                ?.ifHasError { responseObserver?.onError( this.error ) }
                ?.otherwiseReturnKey {
                    responseObserver?.onNext(
                        KeyResponse
                            .newBuilder()
                            .setPixKey(this.key?.id.toString())
                            .setClientId(this.key?.clientId)
                            .build()
                    )
                    responseObserver?.onCompleted()
                }
        }catch ( exception: ConstraintViolationException ){
            responseObserver?.onError(
                Errors
                    .errorConstraintViolation( exception.constraintViolations.iterator().next().message )
            )
        }

    }

    fun delete( @Valid keyDeleteDto : KeyDeleteTransferObject ) : KeyResponseData {
        return keyDeleteDto.delete( keyRepository )
    }

}