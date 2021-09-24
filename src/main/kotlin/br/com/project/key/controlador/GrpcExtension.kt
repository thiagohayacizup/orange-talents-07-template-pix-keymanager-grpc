package br.com.project.key.controlador

import br.com.project.KeyDeleteRequest
import br.com.project.KeyRequest
import br.com.project.LoadRequest
import br.com.project.account.AccountType
import br.com.project.key.controlador.delete.key.KeyDeleteTransferObject
import br.com.project.key.controlador.load.LoadTransferObject
import br.com.project.key.controlador.load.LoadTransferObjectKey
import br.com.project.key.controlador.load.LoadTransferObjectNoneRequest
import br.com.project.key.controlador.load.LoadTransferObjectPixId
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

fun LoadRequest.toTransferObject() : LoadTransferObject {
    return when( filterCase ?: LoadRequest.FilterCase.FILTER_NOT_SET  ){
        LoadRequest.FilterCase.PIXID -> pixId.let { LoadTransferObjectPixId( it.clientId, it.pixKey ) }
        LoadRequest.FilterCase.KEY -> LoadTransferObjectKey(key)
        LoadRequest.FilterCase.FILTER_NOT_SET -> LoadTransferObjectNoneRequest()
    }
}