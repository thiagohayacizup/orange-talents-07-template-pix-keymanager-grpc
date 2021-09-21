package br.com.project.key.model

import br.com.project.account.Account
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

    }

    @field:Id
    @field:GeneratedValue( strategy = GenerationType.AUTO )
    val id : UUID? = null

    @field:NotNull
    val clientId : String

    @field:NotNull
    private val clientName : String

    @field:NotNull
    @field:Enumerated( EnumType.STRING )
    private val keyType : KeyType

    @field:NotNull
    val keyValue : String

    @field:NotNull
    @field:ManyToOne( cascade = [CascadeType.MERGE] )
    private val account : Account

    init{
        clientId = builder.clientId
        clientName = builder.clientName
        keyType = builder.keyType
        keyValue = builder.keyValue
        account = builder.account
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

        fun withClientId( clientId : String ) : Builder {
            this.clientId = clientId
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

        fun build() : Key {
            return Key( this )
        }

    }

}