package br.com.project.key

import br.com.project.ListKeysClientRequest
import br.com.project.PixKeyLoadManagerGrpc
import br.com.project.account.Account
import br.com.project.account.AccountRepository
import br.com.project.account.AccountType
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyType
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

@MicronautTest( transactional = false )
internal class ListIntegrationTest(
    val accountRepository: AccountRepository,
    val keyRepository: KeyRepository,
    private val loadManagerGrpc: PixKeyLoadManagerGrpc.PixKeyLoadManagerBlockingStub
) {

    @Test
    fun `client dont have keys`(){
        keyRepository.deleteAll()
        val response = loadManagerGrpc.listKeysByClient(
            ListKeysClientRequest.newBuilder()
                .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .build()
        )
        Assertions.assertEquals("c56dfef4-7901-44fb-84e2-a2cefb157890", response.clientId )
        Assertions.assertEquals( 0, response.pixKeyList.size)
    }

    @Test
    fun `client have keys`(){
        createKey()
        val response = loadManagerGrpc.listKeysByClient(
            ListKeysClientRequest.newBuilder()
                .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .build()
        )
        Assertions.assertEquals("c56dfef4-7901-44fb-84e2-a2cefb157890", response.clientId )
        Assertions.assertEquals( 1, response.pixKeyList.size)
        Assertions.assertEquals( "joao@email.com", response.pixKeyList[0].keyValue )
    }

    @Test
    fun `client id request invalid`(){
        val thrown = assertThrows<StatusRuntimeException>{
            loadManagerGrpc.listKeysByClient(
                ListKeysClientRequest.newBuilder().setClientId("").build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        if( thrown.message.equals("INVALID_ARGUMENT: ClientId cannot be blank or null.") )
            Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: ClientId cannot be blank or null.")
        else
            Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\"")
    }

    fun createKey() : String {
        return Key.builder()
            .withClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .withClientName("Joao")
            .withCpf("57519991008")
            .withKeyType(KeyType.EMAIL)
            .withKeyValue("joao@email.com")
            .withCreationDate( Instant.now() )
            .withAccount(
                Account.builder()
                    .withBankName("ITAÚ UNIBANCO S.A.")
                    .withAgency("0001")
                    .withAccountNumber("12345")
                    .withIspb("60701190")
                    .withAccountType(AccountType.CONTA_CORRENTE)
                    .build().save( accountRepository )
            )
            .build()
            .register( keyRepository )
            .id.toString()
    }

}