package br.com.project.key.controlador.register.key

import br.com.project.account.Account
import br.com.project.account.AccountRepository
import br.com.project.account.AccountType
import br.com.project.erp.itau.AccountInfo
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyType
import br.com.project.key.validator.UUIDValid
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class KeyTransferObject(
    @field:UUIDValid( message = "ClientId UUID invalid" )
    @field:NotBlank( message = "ClientId must not be blank." )
    val clientId : String,
    @field:NotNull( message = "KeyType must not be null." )
    val keyType : KeyType?,
    @field:Size( max = 77, message = "KeyValue size must have max 77 characters." )
    val keyValue : String,
    @field:NotNull( message = "AccountType must not be null." )
    val accountType: AccountType?
){

    fun register(keyRepository : KeyRepository, accountRepository: AccountRepository, accountInfo : AccountInfo ) : Key {
        return Key.builder()
            .withClientId( clientId )
            .withClientName( accountInfo.titular.nome )
            .withKeyType( keyType!! )
            .withKeyValue( generateKey() )
            .withAccount(
                Account.builder()
                    .withAgency(accountInfo.agencia )
                    .withAccountNumber(accountInfo.numero )
                    .withAccountType( accountType!! )
                    .withBankName( accountInfo.instituicao.nome )
                    .build()
                    .save( accountRepository )
            ).build()
            .register( keyRepository )
    }

    private fun generateKey() : String {
        return if(keyType == KeyType.CHAVE_ALEATORIA) UUID.randomUUID().toString()
        else keyValue
    }

    fun isKeyValueNotValid(): Boolean {
        return !keyType!!.valid( keyValue )
    }

}
