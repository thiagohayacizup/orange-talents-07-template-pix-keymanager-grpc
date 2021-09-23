package br.com.project.erp.itau

import io.micronaut.http.HttpResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ERPItauTest {

    @Test
    fun `request account`(){
        val erpItau : ERPItau = Mockito.mock( ERPItau::class.java )

        val accountInfo = AccountInfo(
            instituicao = InstitutionResponse( "BANCO XYZ", "60701190" ),
            agencia = "0001",
            numero = "123451",
            titular = HolderResponse( "Joao", "76968476002" )
        )

        Mockito
            .`when`( erpItau.findAccount(Mockito.anyString(), Mockito.anyString()) )
            .thenReturn( HttpResponse.ok(accountInfo) )

        val info = erpItau
            .findAccount("123456789-53453546-34534-534534-35345345776", "CONTA_CORRENTE")
            .body()!!

        Assertions.assertEquals(accountInfo.agencia, info.agencia)
        Assertions.assertEquals(accountInfo.numero, info.numero)
        Assertions.assertEquals(accountInfo.instituicao.nome, info.instituicao.nome)
        Assertions.assertEquals(accountInfo.titular.nome, info.titular.nome)

    }

}