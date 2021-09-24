package br.com.project.key.controlador.load

import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository
import br.com.project.key.validator.UUIDValid
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ListTransferObject(
    @field:UUIDValid
    @field:NotBlank(message = "ClientId cannot be blank or null.")
    val clientId : String
) {
    fun list(keyRepository: KeyRepository): List<Key> {
        return Key.listAllKeyByClientId( clientId, keyRepository )
    }
}
