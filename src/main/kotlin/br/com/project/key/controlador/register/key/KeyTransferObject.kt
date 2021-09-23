package br.com.project.key.controlador.register.key

import br.com.project.account.Account
import br.com.project.account.AccountRepository
import br.com.project.account.AccountType
import br.com.project.bcb.pix.BankAccount
import br.com.project.bcb.pix.CreatePixKeyRequest
import br.com.project.bcb.pix.OwnerBCB
import br.com.project.bcb.pix.OwnerType
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

    companion object {
        private val builder = Key.builder()
    }

    fun register(keyRepository : KeyRepository, accountRepository: AccountRepository, accountInfo : AccountInfo ) : Key {
        return builder
            .withClientId( clientId )
            .withClientName( accountInfo.titular.nome )
            .withKeyType( keyType!! )
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

    fun toBCBRequest(body: AccountInfo): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType!!.toBCBKeyType(),
            keyValue,
            BankAccount(body.instituicao.ispb, body.agencia, body.numero, accountType!!.toBCBAccountType()),
            OwnerBCB(OwnerType.NATURAL_PERSON, body.titular.nome, body.titular.cpf)
        )
    }

    fun updateToBCBKey( key : String ){
        builder.withKeyValue( key )
    }

    fun isKeyValueNotValid(): Boolean {
        return !keyType!!.valid( keyValue )
    }

}
