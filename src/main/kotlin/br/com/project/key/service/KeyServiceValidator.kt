package br.com.project.key.service

import br.com.project.key.Errors
import br.com.project.key.controlador.KeyResponseData
import br.com.project.key.controlador.KeyTransferObject
import br.com.project.key.model.Key
import br.com.project.key.model.KeyRepository

class KeyServiceValidator( private val keyService: KeyService, private val keyRepository: KeyRepository ) : KeyService {

    override fun register( keyDto: KeyTransferObject ): KeyResponseData {

        if( keyDto.isKeyValueNotValid() )
            return Errors.invalidKey( keyDto.keyValue )

        if( Key.alreadyExistKey( keyDto.keyValue, keyRepository ) )
            return Errors.alredyExisteKeyValue( keyDto.keyValue )

        return keyService.register( keyDto )

    }

}