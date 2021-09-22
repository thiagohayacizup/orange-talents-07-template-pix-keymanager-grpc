package br.com.project.key.model

import io.grpc.StatusRuntimeException

data class KeyResponseData(
    val key : Key? = null,
    val error : StatusRuntimeException? = null
){

    fun ifHasError( action : KeyResponseData.() -> Unit ) : KeyResponseData {
        if( error != null ) action()
        return this
    }

    fun otherwiseReturnKey( action: KeyResponseData.() -> Unit ){
        if( key != null ) action()
    }

}
