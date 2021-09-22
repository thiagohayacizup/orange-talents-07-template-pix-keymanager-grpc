package br.com.project.key

import br.com.project.key.model.KeyResponseData
import io.grpc.Status
import io.grpc.StatusRuntimeException

class Errors {

    companion object {
        val bodyNotFoundERPItau = KeyResponseData(
            error = Status
                .NOT_FOUND
                .withDescription("Response body not found.")
                .asRuntimeException()
        )

        val errorResponseERPItau = KeyResponseData(
            error = Status
                .NOT_FOUND
                .withDescription("Resource not found.")
                .asRuntimeException()
        )

        fun errorConstraintViolation(message: String) : StatusRuntimeException = Status.INVALID_ARGUMENT
            .withDescription(message)
            .asRuntimeException()

        fun alredyExisteKeyValue( value : String ) = KeyResponseData(
            error = Status
                .ALREADY_EXISTS
                .withDescription("Key { $value } already exists.")
                .asRuntimeException()
        )

        fun invalidKey( value : String ) = KeyResponseData(
            error = Status
                .INVALID_ARGUMENT
                .withDescription("Invalid key value { $value }")
                .asRuntimeException()
        )

    }

}