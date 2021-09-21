package br.com.project.key.controlador

import br.com.project.account.Account
import br.com.project.account.AccountRepository
import br.com.project.account.AccountType
import br.com.project.erp.itau.AccountInfo
import br.com.project.key.Key
import br.com.project.key.KeyRepository
import br.com.project.key.KeyType
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.core.annotation.Introspected
import jdk.net.SocketFlow
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class KeyTransferObject(
    //@field:UUIDValid
    @field:NotBlank( message = "ClientId must not be blank." )
    val clientId : String,
    @field:NotNull( message = "KeyType must not be null." )
    val keyType : KeyType,
    @field:NotBlank( message = "KeyValue must not be blank." )
    @field:Size( max = 77, message = "KeyValue size must have max 77 characters." )
    val keyValue : String,
    @field:NotNull( message = "AccountType must not be null." )
    val accountType: AccountType
){

    fun register( keyRepository : KeyRepository, accountRepository: AccountRepository, accountInfo : AccountInfo ) : Key {
        return Key.builder()
            .withClientId( clientId )
            .withClientName( accountInfo.titular.nome )
            .withKeyType( keyType )
            .withKeyValue( keyValue )
            .withAccount(
                Account.builder()
                    .withAgency(accountInfo.agencia )
                    .withAccountNumber(accountInfo.numero )
                    .withAccountType( accountType )
                    .withBankName( accountInfo.instituicao.nome )
                    .build()
                    .save( accountRepository )
            ).build()
            .register( keyRepository )
    }

}
