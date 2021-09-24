package br.com.project.key.controlador

import br.com.project.*
import br.com.project.bcb.pix.BCBPix
import br.com.project.key.Errors
import br.com.project.key.controlador.load.ListTransferObject
import br.com.project.key.controlador.load.LoadTransferObject
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.Validator

@Validated
@Singleton
class KeyManagerLoadServer(
    private val keyRepository: KeyRepository,
    private val bcbPix: BCBPix,
    private val validator: Validator
) : PixKeyLoadManagerGrpc.PixKeyLoadManagerImplBase() {

    override fun loadInfo(request: LoadRequest?, responseObserver: StreamObserver<LoadResponse>?) {
        try {
            request
                ?.toTransferObject(validator)
                ?.let { load(it) }
                ?.ifHasError { responseObserver?.onError(this.error) }
                ?.otherwiseReturnKey {
                    responseObserver?.onNext(
                        LoadResponse.newBuilder()
                            .setClientId(this.key?.clientId)
                            .setPixKeyId(this.key?.id.toString())
                            .setPixKey(
                                LoadResponse.PixKey.newBuilder()
                                    .setKeyType(this.key?.keyType?.toKeyTypeResponse())
                                    .setKeyValue(this.key?.keyValue)
                                    .setAccount(
                                        LoadResponse.PixKey.Account.newBuilder()
                                            .setAccountType(this.key?.account?.accountType?.toAccountTypeResponse())
                                            .setInstitution(this.key?.account?.bankName)
                                            .setName(this.key?.clientName)
                                            .setCpf(this.key?.cpf)
                                            .setAgency(this.key?.account?.agency)
                                            .setNumber(this.key?.account?.accountNumber)
                                            .build()
                                    )
                                    .setCreatedAt(
                                        this.key?.createdAt?.let {
                                            Timestamp.newBuilder()
                                                .setSeconds(it.epochSecond)
                                                .setNanos(it.nano)
                                                .build()
                                        })
                                    .build()
                            )
                            .build()
                    )
                    responseObserver?.onCompleted()
                }
        }catch ( exception: ConstraintViolationException){
            responseObserver?.onError(
                Errors.errorConstraintViolation( exception.constraintViolations.iterator().next().message )
            )
        }
    }

    private fun load( loadDto: LoadTransferObject) : KeyResponseData {
        return loadDto.load( keyRepository, bcbPix )
    }

    override fun listKeysByClient(request: ListKeysClientRequest?, responseObserver: StreamObserver<ListKeysClientResponse>?) {
        try{
            request
                ?.toTransferObject()
                ?.let { list( it ) }
                ?.let {
                    responseObserver?.onNext(
                        ListKeysClientResponse.newBuilder()
                            .setClientId(request.clientId)
                            .addAllPixKey(it.map { element ->
                                ListKeysClientResponse.PixKey.newBuilder()
                                    .setPixId( element.id.toString() )
                                    .setKeyType( element.keyType.toKeyTypeResponse() )
                                    .setKeyValue( element.keyValue )
                                    .setAccountType( element.account.accountType.toAccountTypeResponse() )
                                    .setCreatedAt( element.createdAt.let { date ->
                                        Timestamp.newBuilder()
                                            .setSeconds(date.epochSecond)
                                            .setNanos(date.nano)
                                            .build()
                                    })
                                    .build()
                            })
                            .build()
                    )
                    responseObserver?.onCompleted()
                }
        }catch ( exception: ConstraintViolationException){
            responseObserver?.onError(
                Errors.errorConstraintViolation( exception.constraintViolations.iterator().next().message )
            )
        }
    }

    fun list( @Valid listDto: ListTransferObject ) : List<Key> {
        return listDto.list( keyRepository )
    }

}
