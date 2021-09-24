package br.com.project.key.controlador

import br.com.project.*
import br.com.project.account.AccountType
import br.com.project.key.controlador.delete.key.KeyDeleteTransferObject
import br.com.project.key.controlador.load.*
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.Key
import br.com.project.key.model.KeyType
import javax.validation.ConstraintViolationException
import javax.validation.Validator

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

fun LoadRequest.toTransferObject( validator: Validator ) : LoadTransferObject {
    val transferObject = when( filterCase ?: LoadRequest.FilterCase.FILTER_NOT_SET  ){
        LoadRequest.FilterCase.PIXID -> pixId.let { LoadTransferObjectPixId( it.clientId, it.pixKey ) }
        LoadRequest.FilterCase.KEY -> LoadTransferObjectKey(key)
        LoadRequest.FilterCase.FILTER_NOT_SET -> LoadTransferObjectNoneRequest()
    }
    val validatorResult = validator.validate( transferObject )
    if( validatorResult.isNotEmpty() )
        throw ConstraintViolationException(validatorResult)
    return transferObject
}

fun ListKeysClientRequest.toTransferObject() : ListTransferObject {
    return ListTransferObject(
        clientId
    )
}