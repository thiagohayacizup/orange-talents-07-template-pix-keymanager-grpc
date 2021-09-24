package br.com.project.key

import br.com.project.KeyDeleteRequest
import br.com.project.KeyRequest
import br.com.project.KeyType
import br.com.project.PixKeyManagerGrpc
import br.com.project.account.AccountType
import br.com.project.bcb.pix.*
import br.com.project.erp.itau.AccountInfo
import br.com.project.erp.itau.ERPItau
import br.com.project.erp.itau.HolderResponse
import br.com.project.erp.itau.InstitutionResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*

@MicronautTest( transactional = false )
internal class KeyDeleteIntegrationTest(
    private val keyManagerGrpcClient : PixKeyManagerGrpc.PixKeyManagerBlockingStub
) {

    @Inject lateinit var erpItau: ERPItau

    @Inject lateinit var bcbPix : BCBPix

    companion object{
        val ispb = "60701190"
        val key = "nome@email.com"
        val agency = "00001"
        val accountNumber = "67355756"
        val cpf = "95274473059"
        val name = "Silva"
    }

    @Test
    fun `key deleted with success`(){
        val clientId = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientId), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    InstitutionResponse( "ITAU", ispb ),
                    agency,
                    accountNumber,
                    HolderResponse( name, cpf )
                )
            ))

        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.EMAIL,
                key,
                BankAccount(ispb, agency, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, cpf)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(key)) )

        Mockito
            .`when`( bcbPix.deleteKey(key, DeletePixKeyRequest(key, ispb)) )
            .thenReturn(HttpResponse.ok(DeletePixKeyResponse(key)))

        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId( clientId )
                .setKeyType(KeyType.EMAIL)
                .setKeyValue(key)
                .setAccountType(br.com.project.AccountType.CONTA_CORRENTE)
                .build()
        )
        val responseDelete = keyManagerGrpcClient.deleteKey(
            KeyDeleteRequest
                .newBuilder()
                .setClientId( clientId )
                .setPixKey( response.pixKey )
                .build()
        )
        Assertions.assertEquals(clientId, responseDelete.clientId)
        Assertions.assertEquals(response.pixKey, responseDelete.pixKey)
    }

    @Test
    fun `error delete bcb`(){
        val clientId = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientId), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    InstitutionResponse( "ITAU", ispb ),
                    agency,
                    accountNumber,
                    HolderResponse( name, cpf )
                )
            ))

        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.EMAIL,
                key,
                BankAccount(ispb, agency, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, cpf)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(key)) )

        Mockito
            .`when`( bcbPix.deleteKey(key, DeletePixKeyRequest(key, ispb)) )
            .thenReturn(HttpResponse.status(HttpStatus.FORBIDDEN))

        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId( clientId )
                .setKeyType(KeyType.EMAIL)
                .setKeyValue(key)
                .setAccountType(br.com.project.AccountType.CONTA_CORRENTE)
                .build()
        )
        val thrown = assertThrows<StatusRuntimeException>{
            keyManagerGrpcClient.deleteKey(
                KeyDeleteRequest
                    .newBuilder()
                    .setClientId( clientId )
                    .setPixKey( response.pixKey )
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INTERNAL.code.value())
        Assertions.assertEquals(thrown.message, "INTERNAL: Key delete operation error in BCB - key not deleted")
    }

    @Test
    fun `invalid client id`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.deleteKey(
                KeyDeleteRequest
                    .newBuilder()
                    .setClientId("c56dfef4")
                    .setPixKey("feac0861-901a-4ec6-ba98-60149cad3ee1")
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\"")
    }

    @Test
    fun `invalid pix key`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.deleteKey(
                KeyDeleteRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setPixKey("feac0861")
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\"")
    }

    @Test
    fun `pix key not found`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.deleteKey(
                KeyDeleteRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setPixKey("feac0861-901a-4ec6-ba98-60149cad3ee1")
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.NOT_FOUND.code.value())
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Pix key not found.")

    }

    @MockBean(ERPItau::class)
    fun erpItau() : ERPItau? {
        return Mockito.mock(ERPItau::class.java)
    }

    @MockBean(BCBPix::class)
    fun bcbPix() : BCBPix? {
        return Mockito.mock(BCBPix::class.java)
    }

}