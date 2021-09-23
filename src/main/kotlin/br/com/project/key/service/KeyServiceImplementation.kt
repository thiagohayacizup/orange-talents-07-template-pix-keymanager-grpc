package br.com.project.key.service

import br.com.project.account.AccountRepository
import br.com.project.bcb.pix.BCBPix
import br.com.project.bcb.pix.CreatePixKeyRequest
import br.com.project.erp.itau.ERPItau
import br.com.project.key.Errors
import br.com.project.key.controlador.register.key.KeyTransferObject
import br.com.project.key.model.KeyRepository
import br.com.project.key.model.KeyResponseData
import io.micronaut.http.HttpStatus

class KeyServiceImplementation private constructor( builder : Builder ) : KeyService {

    companion object{
        fun builder() : Builder{
            return Builder()
        }
    }

    private val keyRepository: KeyRepository
    private val accountRepository: AccountRepository
    private val erpItau: ERPItau
    private val bcbPix: BCBPix

    init {
        keyRepository = builder.keyRepository
        accountRepository = builder.accountRepository
        erpItau = builder.erpItau
        bcbPix = builder.bcbPix
    }

    override fun register( keyDto: KeyTransferObject): KeyResponseData {

        val findAccount = erpItau.findAccount(keyDto.clientId, keyDto.accountType.toString())

        if( findAccount.status != HttpStatus.OK )
            return Errors.errorResponseERPItau

        val body = findAccount.body() ?: return Errors.bodyNotFoundERPItau

        val createKey = bcbPix.createKey( keyDto.toBCBRequest( body ) )

        if( createKey.status != HttpStatus.CREATED )
            return Errors.errorBcb

        val bcbResponse = createKey.body() ?: return Errors.bodyBcbNotFound

        keyDto.updateToBCBKey( bcbResponse.key )

        return KeyResponseData( key = keyDto.register( keyRepository, accountRepository, body ) )

    }

    class Builder{
        lateinit var keyRepository: KeyRepository
        lateinit var accountRepository: AccountRepository
        lateinit var erpItau: ERPItau
        lateinit var bcbPix: BCBPix

        fun withKeyRepository( keyRepository: KeyRepository ) : Builder{
            this.keyRepository = keyRepository
            return this
        }

        fun withAccountRepository( accountRepository: AccountRepository ) : Builder{
            this.accountRepository = accountRepository
            return this
        }

        fun withERPItau( erpItau: ERPItau ) : Builder{
            this.erpItau = erpItau
            return this
        }

        fun withBCBPix( bcbPix: BCBPix) : Builder{
            this.bcbPix = bcbPix
            return this
        }

        fun build() : KeyServiceImplementation{
            return KeyServiceImplementation( this )
        }

    }

}