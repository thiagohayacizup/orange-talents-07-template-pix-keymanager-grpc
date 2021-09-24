package br.com.project.key.model

import br.com.project.account.Account
import br.com.project.bcb.pix.BCBPix
import br.com.project.bcb.pix.DeletePixKeyRequest
import io.grpc.Status
import io.micronaut.http.HttpStatus
import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class Key private constructor(builder: Builder){

    companion object{

        fun builder() : Builder {
            return Builder()
        }

        fun alreadyExistKey( key : String, keyRepository: KeyRepository ) : Boolean {
            return keyRepository.findByKeyValue( key ).isPresent
        }

        fun findByIdAndClientId( clientId: String, pixKey: String, keyRepository: KeyRepository ) : Optional<Key>{
            return keyRepository.findByIdAndClientId(UUID.fromString(pixKey), clientId)
        }

        fun delete( key: Optional<Key>, keyRepository: KeyRepository, bcbPix: BCBPix ): KeyResponseData {
            if( key.isEmpty )
                return KeyResponseData(
                    error = Status
                        .NOT_FOUND
                        .withDescription("Pix key not found.")
                        .asRuntimeException()
                )
            val deleteKey = bcbPix.deleteKey(key.get().keyValue, DeletePixKeyRequest(key.get().keyValue, key.get().account.ispb))
            if( deleteKey.status != HttpStatus.OK )
                return KeyResponseData(
                    error = Status
                        .INTERNAL
                        .withDescription("Key delete operation error in BCB - key not deleted")
                        .asRuntimeException()
                )
            keyRepository.delete( key.get() )
            return KeyResponseData( key = key.get() )
        }

        fun searchByClientIdAndPixId(keyRepository: KeyRepository, clientId: String, pixId: String): KeyResponseData {
            val key = keyRepository.findByIdAndClientId(UUID.fromString(pixId), clientId)
            if( key.isEmpty )
                return KeyResponseData(
                    error = Status
                        .NOT_FOUND
                        .withDescription("Pix key with id { $pixId } not found.")
                        .asRuntimeException()
                )
            return KeyResponseData( key = key.get() )
        }

        fun searchByKeyOrElse(key: String, keyRepository: KeyRepository, otherwise: () -> KeyResponseData ): KeyResponseData {
            val keyOptional = keyRepository.findByKeyValue( key )
            if( keyOptional.isPresent )
                return KeyResponseData( key = keyOptional.get() )
            return otherwise()
        }

        fun listAllKeyByClientId(clientId: String, keyRepository: KeyRepository): List<Key> {
            return keyRepository.findByClientId(clientId)
        }

    }

    @field:Id
    @field:GeneratedValue( strategy = GenerationType.AUTO )
    val id : UUID? = null

    @field:NotNull
    val clientId : String

    @field:NotNull
    val clientName : String

    @field:NotNull
    val cpf : String

    @field:NotNull
    @field:Enumerated( EnumType.STRING )
    val keyType : KeyType

    @field:NotNull
    val keyValue : String

    @field:NotNull
    @field:ManyToOne( cascade = [CascadeType.MERGE] )
    val account : Account

    @field:NotNull
    val createdAt : Instant

    init{
        clientId = builder.clientId
        clientName = builder.clientName
        keyType = builder.keyType
        keyValue = builder.keyValue
        account = builder.account
        cpf = builder.cpf
        createdAt = builder.createdAt ?: Instant.now()
    }

    fun register( keyRepository: KeyRepository) : Key {
        return keyRepository.save( this )
    }

    class Builder{

        lateinit var clientId : String
        lateinit var clientName : String
        lateinit var keyType : KeyType
        lateinit var keyValue : String
        lateinit var account : Account
        lateinit var cpf : String
        var createdAt : Instant? = null

        fun withClientId( clientId : String ) : Builder {
            this.clientId = clientId
            return this
        }

        fun withCpf( cpf : String ) : Builder {
            this.cpf = cpf
            return this
        }

        fun withClientName( clientName : String ) : Builder {
            this.clientName = clientName
            return this
        }

        fun withKeyType( keyType : KeyType) : Builder {
            this.keyType = keyType
            return this
        }

        fun withKeyValue( keyValue : String) : Builder {
            this.keyValue = keyValue
            return this
        }

        fun withAccount( account : Account ) : Builder {
            this.account = account
            return this
        }

        fun withCreationDate( instant: Instant ) : Builder {
            this.createdAt = instant
            return this
        }

        fun build() : Key {
            return Key( this )
        }

    }

}