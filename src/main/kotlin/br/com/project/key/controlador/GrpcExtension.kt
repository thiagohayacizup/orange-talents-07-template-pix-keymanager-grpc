package br.com.project.key.controlador

import br.com.project.KeyRequest
import br.com.project.account.AccountType
import br.com.project.key.KeyType

fun KeyRequest.toKeyTranferObject() : KeyTransferObject {
    return KeyTransferObject(
        clientId,
        KeyType.from( keyType ),
        keyValue,
        AccountType.from( accountType )
    )
}