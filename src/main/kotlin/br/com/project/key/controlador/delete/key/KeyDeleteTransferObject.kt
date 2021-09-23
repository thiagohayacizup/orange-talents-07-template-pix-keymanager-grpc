package br.com.project.key.controlador.delete.key

import br.com.project.bcb.pix.BCBPix
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import br.com.project.key.validator.UUIDValid
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class KeyDeleteTransferObject(

    @field:UUIDValid
    @field:NotBlank(message = "ClientId cannot be null.")
    val cliendId : String,

    @field:UUIDValid
    @field:NotBlank(message = "PixKey cannot be null.")
    val pixKey : String

){

    fun delete( keyRepository: KeyRepository, bcbPix: BCBPix ) : KeyResponseData {
        return Key.delete(
            Key.findByIdAndClientId(cliendId, pixKey, keyRepository),
            keyRepository,
            bcbPix
        )
    }

}