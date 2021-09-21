package br.com.project.key.controlador

import br.com.project.key.Key
import io.grpc.StatusRuntimeException

data class KeyResponseData(
    val key : Key? = null,
    val error : StatusRuntimeException? = null
)
