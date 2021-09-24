package br.com.project.bcb.pix

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BCBPix {

    @Post(
        value = "/api/v1/pix/keys",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
    )
    fun createKey( @Body bcbRequest : CreatePixKeyRequest ) : HttpResponse<CreatePixKeyResponse>

    @Delete(
        value = "/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
    )
    fun deleteKey( @PathVariable key: String, @Body bcbRequest : DeletePixKeyRequest ) : HttpResponse<DeletePixKeyResponse>

    @Get(
        value = "/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML]
    )
    fun findByKey( @PathVariable key: String ) : HttpResponse<PixKeyDetailsResponse>

}