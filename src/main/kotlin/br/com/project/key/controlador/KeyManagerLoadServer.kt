package br.com.project.key.controlador

import br.com.project.LoadRequest
import br.com.project.LoadResponse
import br.com.project.PixKeyLoadManagerGrpc
import br.com.project.bcb.pix.BCBPix
import br.com.project.key.controlador.load.LoadTransferObject
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class KeyManagerLoadServer(
    private val keyRepository: KeyRepository,
    private val bcbPix: BCBPix
) : PixKeyLoadManagerGrpc.PixKeyLoadManagerImplBase() {

    override fun loadInfo(request: LoadRequest?, responseObserver: StreamObserver<LoadResponse>?) {
        request
            ?.toTransferObject()
            ?.let { load( it ) }
            ?.ifHasError { responseObserver?.onError( this.error ) }
            ?.otherwiseReturnKey {
                responseObserver?.onNext(
                    LoadResponse.newBuilder()
                        .setClientId( this.key?.clientId )
                        .setPixKeyId( this.key?.id.toString() )
                        .setPixKey(
                            LoadResponse.PixKey.newBuilder()
                                .setKeyType( this.key?.keyType?.toKeyTypeResponse() )
                                .setKeyValue( this.key?.keyValue )
                                .setAccount(
                                    LoadResponse.PixKey.Account.newBuilder()
                                        .setAccountType( this.key?.account?.accountType?.toAccountTypeResponse() )
                                        .setInstitution( this.key?.account?.bankName )
                                        .setName( this.key?.clientName )
                                        .setCpf( this.key?.cpf )
                                        .setAgency( this.key?.account?.agency )
                                        .setNumber( this.key?.account?.accountNumber )
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
    }

    private fun load( @Valid loadDto: LoadTransferObject) : KeyResponseData {
        return loadDto.load( keyRepository, bcbPix )
    }

}