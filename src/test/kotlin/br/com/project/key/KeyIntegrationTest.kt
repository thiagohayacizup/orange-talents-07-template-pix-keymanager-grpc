package br.com.project.key

import br.com.project.KeyRequest
import br.com.project.KeyResponse
import br.com.project.PixKeyManagerGrpc
import br.com.project.account.AccountType
import br.com.project.erp.itau.AccountInfo
import br.com.project.erp.itau.ERPItau
import br.com.project.erp.itau.HolderResponse
import br.com.project.erp.itau.InstitutionResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*

@MicronautTest( transactional = false )
internal class KeyIntegrationTest(
    private val keyManagerGrpcClient : PixKeyManagerGrpc.PixKeyManagerBlockingStub
) {

    @Inject lateinit var erpItau: ERPItau

    companion object{
        val UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$".toRegex()
    }

    @Test
    fun `key registered with success - cpf`(){
        val clientUUID = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU" ),
                    agencia = "00001",
                    numero = "123564324",
                    titular = HolderResponse( "João" )
                )
            ))
        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.CPF)
                .setKeyValue("58142114070")
                .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key registered with success - cell number`(){
        val clientUUID = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_POUPANCA.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU" ),
                    agencia = "00001",
                    numero = "9878987",
                    titular = HolderResponse( "Maria" )
                )
            ))
        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.NUMERO_CELULAR)
                .setKeyValue("+5511222222222")
                .setAccountType(KeyRequest.AccountType.CONTA_POUPANCA)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key registered with success - email`(){
        val clientUUID = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU" ),
                    agencia = "00001",
                    numero = "998763432",
                    titular = HolderResponse( "Ana" )
                )
            ))
        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.EMAIL)
                .setKeyValue("ana@email.com")
                .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key registered with success - random key`(){
        val clientUUID = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU" ),
                    agencia = "00001",
                    numero = "999112132",
                    titular = HolderResponse( "Lucas" )
                )
            ))
        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                .setKeyValue("")
                .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key already registered`(){
        val clientUUID = UUID.randomUUID().toString()
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU" ),
                    agencia = "00001",
                    numero = "998763432",
                    titular = HolderResponse( "Marcos" )
                )
            ))

        val registerFunction : () -> KeyResponse = {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId(clientUUID)
                    .setKeyType(KeyRequest.KeyType.EMAIL)
                    .setKeyValue("marcos@email.com")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }

        registerFunction()

        val thrown = assertThrows<StatusRuntimeException>{ registerFunction() }

        Assertions.assertEquals(thrown.status.code.value(), Status.ALREADY_EXISTS.code.value())
        Assertions.assertEquals(thrown.message, "ALREADY_EXISTS: Key { marcos@email.com } already exists.")
    }

    @Test
    fun `invalid client id`(){
        val thrown = assertThrows<StatusRuntimeException>{
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("")
                    .setKeyType(KeyRequest.KeyType.EMAIL)
                    .setKeyValue("email@email.com")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        if( thrown.message.equals("INVALID_ARGUMENT: ClientId must not be blank.") )
            Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: ClientId must not be blank.")
        else
            Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$\"")
    }

    @Test
    fun `invalid key type`(){
        val thrown = assertThrows<StatusRuntimeException>{
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.UNKNOWN_TYPE)
                    .setKeyValue("email@email.com")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: KeyType must not be null.")
    }

    @Test
    fun `invalid account type`() {
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.EMAIL)
                    .setKeyValue("email@email.com")
                    .setAccountType(KeyRequest.AccountType.UNKNOWN_ACCOUNT)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: AccountType must not be null.")
    }

    @Test
    fun `invalid value for key type`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.CPF)
                    .setKeyValue("email@email.com")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Invalid key value { email@email.com }")
    }

    @Test
    fun `invalid document - cpf`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.CPF)
                    .setKeyValue("11111111111")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Invalid key value { 11111111111 }")
    }

    @Test
    fun `invalid cellphone number`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.NUMERO_CELULAR)
                    .setKeyValue("23436765")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Invalid key value { 23436765 }")
    }

    @Test
    fun `invalid email address`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.EMAIL)
                    .setKeyValue("@")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Invalid key value { @ }")
    }

    @Test
    fun `passing value for random key`(){
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                    .setKeyValue("3364redgf67ufj")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Invalid key value { 3364redgf67ufj }")
    }

    @Test
    fun `body response erp itau not found`(){
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains("c56dfef4-7901-44fb-84e2-a2cefb157822"), Mockito.anyString()) )
            .thenReturn( HttpResponse.ok( null ) )
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157822")
                    .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                    .setKeyValue("")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.NOT_FOUND.code.value())
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Response body not found.")
    }

    @Test
    fun `error response erp itau 404`(){
        Mockito
            .`when`( erpItau.buscarConta(Mockito.contains("c56dfef4-7901-44fb-84e2-a2cefb157811"), Mockito.anyString()) )
            .thenReturn( HttpResponse.notFound() )
        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157811")
                    .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                    .setKeyValue("")
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.NOT_FOUND.code.value())
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Resource not found.")
    }

    @MockBean(ERPItau::class)
    fun erpItau() : ERPItau? {
        return Mockito.mock(ERPItau::class.java)
    }

    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel : ManagedChannel ) : PixKeyManagerGrpc.PixKeyManagerBlockingStub{
            return PixKeyManagerGrpc.newBlockingStub( channel )
        }
    }

}