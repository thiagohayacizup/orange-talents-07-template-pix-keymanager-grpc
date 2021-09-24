package br.com.project.key.controlador.load

import br.com.project.account.Account
import br.com.project.bcb.pix.BCBPix
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import br.com.project.key.validator.UUIDValid
import io.grpc.Status
import io.micronaut.http.HttpStatus
import java.time.Instant
import javax.validation.constraints.NotBlank

interface LoadTransferObject {

    fun load( keyRepository: KeyRepository, bcbPix: BCBPix ) : KeyResponseData

}

data class LoadTransferObjectPixId(
    @field:UUIDValid
    @field:NotBlank( message = "ClientId cannot be blank or null." )
    private val clientId : String,
    @field:UUIDValid
    @field:NotBlank( message = "PixId cannot be blank or null." )
    private val pixId : String
) : LoadTransferObject{

    override fun load( keyRepository: KeyRepository, bcbPix: BCBPix ) : KeyResponseData {
        return Key.searchByClientIdAndPixId( keyRepository, clientId, pixId )
    }

}

data class LoadTransferObjectKey(
    @field:NotBlank( message = "Key cannot be blank or null." )
    private val key : String
) : LoadTransferObject {

    override fun load( keyRepository: KeyRepository, bcbPix: BCBPix ) : KeyResponseData {
        return Key.searchByKeyOrElse( key, keyRepository ){
            val info = bcbPix.findByKey( key )
            if( info.status != HttpStatus.OK )
                return@searchByKeyOrElse KeyResponseData(
                    error = Status
                        .NOT_FOUND
                        .withDescription("Key { $key } not found.")
                        .asRuntimeException()
                )
            val body = info.body()!!
            return@searchByKeyOrElse KeyResponseData(
                key = Key.builder()
                    .withClientId("")
                    .withCpf(body.ownerBCB.taxIdNumber)
                    .withCreationDate(Instant.parse(body.createdAt))
                    .withKeyType( body.keyType.toKeyType() )
                    .withClientName( body.ownerBCB.name )
                    .withKeyValue( body.key )
                    .withAccount(
                        Account.builder()
                            .withIspb( body.bankAccount.participant )
                            .withAgency( body.bankAccount.branch )
                            .withAccountNumber( body.bankAccount.accountNumber )
                            .withAccountType( body.bankAccount.accountType.toAccountType() )
                            .build()
                    )
                    .build()
            )
        }
    }

}

class LoadTransferObjectNoneRequest : LoadTransferObject {

    override fun load( keyRepository: KeyRepository, bcbPix: BCBPix ) : KeyResponseData {
        return KeyResponseData(
            error = Status
                .INVALID_ARGUMENT
                .withDescription("Request needs pixId or Key to load data")
                .asRuntimeException()
        )
    }

}