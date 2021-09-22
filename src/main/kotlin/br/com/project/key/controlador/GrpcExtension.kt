package br.com.project.key.controlador

import br.com.project.KeyDeleteRequest
import br.com.project.KeyRequest
import br.com.project.account.AccountType
import br.com.project.key.controlador.delete.key.KeyDeleteTransferObject
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyType

fun KeyRequest.toKeyTranferObject() : KeyTransferObject {
    return KeyTransferObject(
        clientId,
        KeyType.from( keyType ),
        keyValue,
        AccountType.from( accountType )
    )
}

fun KeyDeleteRequest.toKeyTransferObject() : KeyDeleteTransferObject {
    return KeyDeleteTransferObject(
        clientId,
        pixKey
    )
}