package br.com.project.key

import br.com.project.LoadRequest
import br.com.project.PixKeyLoadManagerGrpc
import br.com.project.account.Account
import br.com.project.account.AccountRepository
import br.com.project.account.AccountType
import br.com.project.bcb.pix.*
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyType
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
import org.junit.jupiter.api.*
import org.mockito.Mockito
import java.time.Instant
import java.util.*

@MicronautTest( transactional = false )
internal class LoadIntegrationTest(
    val accountRepository: AccountRepository,
    val keyRepository: KeyRepository,
    val pixKeyLoadManagerGrpc: PixKeyLoadManagerGrpc.PixKeyLoadManagerBlockingStub
) {

    @Inject lateinit var bcbPix : BCBPix

    companion object{
        private lateinit var PIX_ID : String
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

    @Test
    fun `load pix info by clientId and pixId`(){
        val response = pixKeyLoadManagerGrpc.loadInfo(
            LoadRequest.newBuilder()
                .setPixId( LoadRequest.PixId.newBuilder()
                    .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setPixKey(PIX_ID)
                    .build()
                )
                .build()
        )
        Assertions.assertEquals( "c56dfef4-7901-44fb-84e2-a2cefb157890", response.clientId )
        Assertions.assertEquals( PIX_ID, response.pixKeyId )
        Assertions.assertEquals( br.com.project.AccountType.CONTA_CORRENTE,response.pixKey.account.accountType)
        Assertions.assertEquals(br.com.project.KeyType.EMAIL, response.pixKey.keyType)
        Assertions.assertEquals("joao@email.com", response.pixKey.keyValue)
        Assertions.assertEquals( "ITAÚ UNIBANCO S.A.", response.pixKey.account.institution)
        Assertions.assertEquals("Joao", response.pixKey.account.name)
        Assertions.assertEquals("57519991008", response.pixKey.account.cpf)
        Assertions.assertEquals("0001", response.pixKey.account.agency)
        Assertions.assertEquals("12345", response.pixKey.account.number)
    }

    @Test
    fun `load pix by key`(){
        PIX_ID = createKey()
        val key = "joao@email.com"
        val response = pixKeyLoadManagerGrpc.loadInfo(
            LoadRequest.newBuilder().setKey(key).build()
        )
        Assertions.assertEquals( "c56dfef4-7901-44fb-84e2-a2cefb157890", response.clientId )
        Assertions.assertEquals( PIX_ID, response.pixKeyId )
        Assertions.assertEquals( br.com.project.AccountType.CONTA_CORRENTE,response.pixKey.account.accountType)
        Assertions.assertEquals(br.com.project.KeyType.EMAIL, response.pixKey.keyType)
        Assertions.assertEquals(key, response.pixKey.keyValue)
        Assertions.assertEquals( "ITAÚ UNIBANCO S.A.", response.pixKey.account.institution)
        Assertions.assertEquals("Joao", response.pixKey.account.name)
        Assertions.assertEquals("57519991008", response.pixKey.account.cpf)
        Assertions.assertEquals("0001", response.pixKey.account.agency)
        Assertions.assertEquals("12345", response.pixKey.account.number)
    }

    @Test
    fun `load pix by key BCB - email`(){
        val key = "nome@email.com"
        val agency = "0002"
        val ispb = "60701190"
        val number = "12345"
        val cpf = "25104089096"
        Mockito
            .`when`( bcbPix.findByKey(Mockito.contains(key)) )
            .thenReturn( HttpResponse.ok(
                PixKeyDetailsResponse(
                    KeyTypeBCB.EMAIL,
                    key,
                    BankAccount(ispb, agency, number, AccountTypeBCB.CACC),
                    OwnerBCB(OwnerType.NATURAL_PERSON, "Nome", cpf),
                    "2021-02-01T00:00:00"
                )
            ) )
        val response = pixKeyLoadManagerGrpc.loadInfo(
            LoadRequest.newBuilder().setKey(key).build()
        )
        Assertions.assertEquals( "", response.clientId )
        Assertions.assertEquals( "null", response.pixKeyId )
        Assertions.assertEquals( br.com.project.AccountType.CONTA_CORRENTE,response.pixKey.account.accountType)
        Assertions.assertEquals(br.com.project.KeyType.EMAIL, response.pixKey.keyType)
        Assertions.assertEquals(key, response.pixKey.keyValue)
        Assertions.assertEquals( "ITAÚ UNIBANCO S.A.", response.pixKey.account.institution)
        Assertions.assertEquals("Nome", response.pixKey.account.name)
        Assertions.assertEquals(cpf, response.pixKey.account.cpf)
        Assertions.assertEquals(agency, response.pixKey.account.agency)
        Assertions.assertEquals(number, response.pixKey.account.number)
    }

    @Test
    fun `load pix by key BCB - cpf`(){
        val key = "25104089096"
        val agency = "0002"
        val ispb = "607011901"
        val number = "12345"
        val cpf = "25104089096"
        Mockito
            .`when`( bcbPix.findByKey(Mockito.contains(key)) )
            .thenReturn( HttpResponse.ok(
                PixKeyDetailsResponse(
                    KeyTypeBCB.CPF,
                    key,
                    BankAccount(ispb, agency, number, AccountTypeBCB.SVGS),
                    OwnerBCB(OwnerType.NATURAL_PERSON, "Nome", cpf),
                    "2021-02-01T00:00:00"
                )
            ) )
        val response = pixKeyLoadManagerGrpc.loadInfo(
            LoadRequest.newBuilder().setKey(key).build()
        )
        Assertions.assertEquals( "", response.clientId )
        Assertions.assertEquals( "null", response.pixKeyId )
        Assertions.assertEquals( br.com.project.AccountType.CONTA_POUPANCA,response.pixKey.account.accountType)
        Assertions.assertEquals(br.com.project.KeyType.CPF, response.pixKey.keyType)
        Assertions.assertEquals(key, response.pixKey.keyValue)
        Assertions.assertEquals( "", response.pixKey.account.institution)
        Assertions.assertEquals("Nome", response.pixKey.account.name)
        Assertions.assertEquals(cpf, response.pixKey.account.cpf)
        Assertions.assertEquals(agency, response.pixKey.account.agency)
        Assertions.assertEquals(number, response.pixKey.account.number)
    }

    @Test
    fun `load pix by key BCB - cellnumber`(){
        val key = "+554089096"
        val agency = "0002"
        val ispb = "60701190"
        val number = "12345"
        val cpf = "25104089096"
        Mockito
            .`when`( bcbPix.findByKey(Mockito.contains(key)) )
            .thenReturn( HttpResponse.ok(
                PixKeyDetailsResponse(
                    KeyTypeBCB.PHONE,
                    key,
                    BankAccount(ispb, agency, number, AccountTypeBCB.SVGS),
                    OwnerBCB(OwnerType.NATURAL_PERSON, "Nome", cpf),
                    "2021-02-01T00:00:00"
                )
            ) )
        val response = pixKeyLoadManagerGrpc.loadInfo(
            LoadRequest.newBuilder().setKey(key).build()
        )
        Assertions.assertEquals( "", response.clientId )
        Assertions.assertEquals( "null", response.pixKeyId )
        Assertions.assertEquals( br.com.project.AccountType.CONTA_POUPANCA,response.pixKey.account.accountType)
        Assertions.assertEquals(br.com.project.KeyType.NUMERO_CELULAR, response.pixKey.keyType)
        Assertions.assertEquals(key, response.pixKey.keyValue)
        Assertions.assertEquals( "ITAÚ UNIBANCO S.A.", response.pixKey.account.institution)
        Assertions.assertEquals("Nome", response.pixKey.account.name)
        Assertions.assertEquals(cpf, response.pixKey.account.cpf)
        Assertions.assertEquals(agency, response.pixKey.account.agency)
        Assertions.assertEquals(number, response.pixKey.account.number)
    }

    @Test
    fun `load pix by key BCB - random`(){
        val key = UUID.randomUUID().toString()
        val agency = "0002"
        val ispb = "60701190"
        val number = "12345"
        val cpf = "25104089096"
        Mockito
            .`when`( bcbPix.findByKey(Mockito.contains(key)) )
            .thenReturn( HttpResponse.ok(
                PixKeyDetailsResponse(
                    KeyTypeBCB.RANDOM,
                    key,
                    BankAccount(ispb, agency, number, AccountTypeBCB.SVGS),
                    OwnerBCB(OwnerType.NATURAL_PERSON, "Nome", cpf),
                    "2021-02-01T00:00:00"
                )
            ) )
        val response = pixKeyLoadManagerGrpc.loadInfo(
            LoadRequest.newBuilder().setKey(key).build()
        )
        Assertions.assertEquals( "", response.clientId )
        Assertions.assertEquals( "null", response.pixKeyId )
        Assertions.assertEquals( br.com.project.AccountType.CONTA_POUPANCA,response.pixKey.account.accountType)
        Assertions.assertEquals(br.com.project.KeyType.CHAVE_ALEATORIA, response.pixKey.keyType)
        Assertions.assertEquals(key, response.pixKey.keyValue)
        Assertions.assertEquals( "ITAÚ UNIBANCO S.A.", response.pixKey.account.institution)
        Assertions.assertEquals("Nome", response.pixKey.account.name)
        Assertions.assertEquals(cpf, response.pixKey.account.cpf)
        Assertions.assertEquals(agency, response.pixKey.account.agency)
        Assertions.assertEquals(number, response.pixKey.account.number)
    }

    @Test
    fun `load pix info by clientId and PixId not found`(){
        val thrown = assertThrows<StatusRuntimeException>{
            pixKeyLoadManagerGrpc.loadInfo(
                LoadRequest.newBuilder()
                    .setPixId( LoadRequest.PixId.newBuilder()
                            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                            .setPixKey("5260263c-a3c1-4727-ae32-3bdb2538841b")
                            .build()
                    )
                    .build()
            )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.NOT_FOUND.code.value())
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Pix key with id { 5260263c-a3c1-4727-ae32-3bdb2538841b } not found.")
    }

    @Test
    fun `load pix by key BCB not found`(){
        val key = "77080302005"
        Mockito
            .`when`( bcbPix.findByKey(Mockito.contains(key)) )
            .thenReturn( HttpResponse.notFound() )
        val thrown = assertThrows<StatusRuntimeException>{
            pixKeyLoadManagerGrpc.loadInfo( LoadRequest.newBuilder().setKey(key).build() )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.NOT_FOUND.code.value())
        Assertions.assertEquals(thrown.message, "NOT_FOUND: Key { 77080302005 } not found.")
    }

    @Test
    fun `invalid key`(){
        val thrown = assertThrows<StatusRuntimeException>{
            pixKeyLoadManagerGrpc.loadInfo( LoadRequest.newBuilder().setKey("").build() )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Key cannot be blank or null.")
    }

    @Test
    fun `load passing no request`(){
        val thrown = assertThrows<StatusRuntimeException>{
            pixKeyLoadManagerGrpc.loadInfo( LoadRequest.newBuilder().build() )
        }
        Assertions.assertEquals(thrown.status.code.value(), Status.INVALID_ARGUMENT.code.value())
        Assertions.assertEquals(thrown.message, "INVALID_ARGUMENT: Request needs pixId or Key to load data")
    }

    @MockBean(BCBPix::class)
    fun bcbPix() : BCBPix? {
        return Mockito.mock(BCBPix::class.java)
    }

    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel : ManagedChannel) : PixKeyLoadManagerGrpc.PixKeyLoadManagerBlockingStub{
            return PixKeyLoadManagerGrpc.newBlockingStub( channel )
        }
    }

}