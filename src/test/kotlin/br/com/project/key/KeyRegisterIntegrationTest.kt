package br.com.project.key

import br.com.project.KeyRequest
import br.com.project.KeyResponse
import br.com.project.PixKeyManagerGrpc
import br.com.project.account.AccountType
import br.com.project.bcb.pix.*
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
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.net.URI
import java.util.*

@MicronautTest( transactional = false )
internal class KeyRegisterIntegrationTest(
    private val keyManagerGrpcClient : PixKeyManagerGrpc.PixKeyManagerBlockingStub
) {

    @Inject lateinit var erpItau: ERPItau

    @Inject lateinit var bcbPix : BCBPix

    companion object{
        val UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$".toRegex()
        val ISPB = "60701190"
        val AGENCY = "00001"
        val CPF = "47457722033"
    }

    @Test
    fun `key registered with success - cpf`(){
        val clientUUID = UUID.randomUUID().toString()
        val key = "58142114070"
        val accountNumber = "123564324"
        val name = "Joao"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU", ISPB),
                    agencia = AGENCY,
                    numero = accountNumber,
                    titular = HolderResponse( name, CPF )
                )
            ))
        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.CPF,
                key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(key)) )

        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.CPF)
                .setKeyValue(key)
                .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key registered with success - cell number`(){
        val clientUUID = UUID.randomUUID().toString()
        val key = "+5511222222222"
        val accountNumber = "9878987"
        val name = "Maria"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_POUPANCA.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU", ISPB ),
                    agencia = AGENCY,
                    numero = accountNumber,
                    titular = HolderResponse( name, CPF )
                )
            ))

        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.PHONE,
                key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.SVGS),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(key)) )

        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.NUMERO_CELULAR)
                .setKeyValue(key)
                .setAccountType(KeyRequest.AccountType.CONTA_POUPANCA)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key registered with success - email`(){
        val clientUUID = UUID.randomUUID().toString()
        val key = "ana@email.com"
        val accountNumber = "998763432"
        val name = "Ana"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU", ISPB ),
                    agencia = AGENCY,
                    numero = accountNumber,
                    titular = HolderResponse( name, CPF )
                )
            ))

        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.EMAIL,
                key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(key)) )

        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.EMAIL)
                .setKeyValue(key)
                .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key registered with success - random key`(){
        val clientUUID = UUID.randomUUID().toString()
        val key = ""
        val accountNumber = "999112132"
        val name = "Lucas"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU", ISPB ),
                    agencia = AGENCY,
                    numero = accountNumber,
                    titular = HolderResponse( name, CPF )
                )
            ))

        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.RANDOM,
                key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(UUID.randomUUID().toString())) )

        val response = keyManagerGrpcClient.registerKey(
            KeyRequest
                .newBuilder()
                .setClientId(clientUUID)
                .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                .setKeyValue(key)
                .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                .build()
        )
        Assertions.assertEquals(response.clientId, clientUUID)
        Assertions.assertTrue(  UUID_REGEX.matches( response.pixKey ) )
    }

    @Test
    fun `key already registered`(){
        val clientUUID = UUID.randomUUID().toString()
        val key = "marcos@email.com"
        val accountNumber = "999112132"
        val name = "Marcos"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains(clientUUID), Mockito.contains(AccountType.CONTA_CORRENTE.toString()) ) )
            .thenReturn( HttpResponse.ok(
                AccountInfo(
                    instituicao = InstitutionResponse( "ITAU", ISPB ),
                    agencia = AGENCY,
                    numero = accountNumber,
                    titular = HolderResponse( name, CPF )
                )
            ))

        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.EMAIL,
                key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.created(CreatePixKeyResponse(key)) )

        val registerFunction : () -> KeyResponse = {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId(clientUUID)
                    .setKeyType(KeyRequest.KeyType.EMAIL)
                    .setKeyValue(key)
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
            .`when`( erpItau.findAccount(Mockito.contains("c56dfef4-7901-44fb-84e2-a2cefb157822"), Mockito.anyString()) )
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
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Response body erp itau not found.")
    }

    @Test
    fun `body response bcb not found`(){
        val key = ""
        val accountNumber = "1231"
        val name = "Sarah"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains("c56dfef4-7901-44fb-84e2-a2cefb157822"), Mockito.anyString()) )
            .thenReturn( HttpResponse.ok( AccountInfo(
                instituicao = InstitutionResponse( "ITAU", ISPB ),
                agencia = AGENCY,
                numero = accountNumber,
                titular = HolderResponse( name, CPF )
            ) ) )
        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.RANDOM, key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.created( null, URI.create("") ) )

        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157822")
                    .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                    .setKeyValue(key)
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.NOT_FOUND.code.value())
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Response body bcb not found.")
    }

    @Test
    fun `error bcb response`(){
        val key = ""
        val accountNumber = "1231"
        val name = "Sarah"
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains("c56dfef4-7901-44fb-84e2-a2cefb157822"), Mockito.anyString()) )
            .thenReturn( HttpResponse.ok( AccountInfo(
                instituicao = InstitutionResponse( "ITAU", ISPB ),
                agencia = AGENCY,
                numero = accountNumber,
                titular = HolderResponse( name, CPF )
            ) ) )
        Mockito
            .`when`( bcbPix.createKey( CreatePixKeyRequest(
                KeyTypeBCB.RANDOM, key,
                BankAccount(ISPB, AGENCY, accountNumber, AccountTypeBCB.CACC),
                OwnerBCB(OwnerType.NATURAL_PERSON, name, CPF)
            ) ) )
            .thenReturn( HttpResponse.status( HttpStatus.FORBIDDEN ) )

        val thrown = assertThrows<StatusRuntimeException> {
            keyManagerGrpcClient.registerKey(
                KeyRequest
                    .newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157822")
                    .setKeyType(KeyRequest.KeyType.CHAVE_ALEATORIA)
                    .setKeyValue(key)
                    .setAccountType(KeyRequest.AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INTERNAL.code.value())
        Assertions.assertEquals(thrown.message, "INTERNAL: Error registering key in BCB")
    }

    @Test
    fun `error response erp itau 404`(){
        Mockito
            .`when`( erpItau.findAccount(Mockito.contains("c56dfef4-7901-44fb-84e2-a2cefb157811"), Mockito.anyString()) )
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
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Resource not found erp itau.")
    }

    @MockBean(ERPItau::class)
    fun erpItau() : ERPItau? {
        return Mockito.mock(ERPItau::class.java)
    }

    @MockBean(BCBPix::class)
    fun bcbPix() : BCBPix? {
        return Mockito.mock(BCBPix::class.java)
    }

    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel : ManagedChannel ) : PixKeyManagerGrpc.PixKeyManagerBlockingStub{
            return PixKeyManagerGrpc.newBlockingStub( channel )
        }
    }

}