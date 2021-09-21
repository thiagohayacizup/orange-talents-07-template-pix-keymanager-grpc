package br.com.project.key.controlador

import br.com.project.KeyRequest
import br.com.project.KeyResponse
import br.com.project.PixKeyManagerGrpc
import br.com.project.account.AccountRepository
import br.com.project.erp.itau.ERPItau
import br.com.project.key.KeyRepository
import br.com.project.key.service.KeyService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class KeyManagerServer(
    private val keyRepository: KeyRepository,
    private val accountRepository: AccountRepository,
    private val itauErp : ERPItau
) : PixKeyManagerGrpc.PixKeyManagerImplBase() {

    override fun registerKey(request: KeyRequest?, responseObserver: StreamObserver<KeyResponse>?) {

        try {

            request?.toKeyTranferObject()?.let {
                KeyService
                    .create( keyRepository, accountRepository, itauErp )
                    .register( it )
            }

            responseObserver?.onNext(
                KeyResponse
                    .newBuilder()
                    .setChavePix("12345")
                    .setClientId(request?.clientId)
                    .build()
            )
            responseObserver?.onCompleted()

        }catch (exception: ConstraintViolationException ){

            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription( exception.constraintViolations.iterator().next().message )
                    .asRuntimeException()
            )

        }

    }

}