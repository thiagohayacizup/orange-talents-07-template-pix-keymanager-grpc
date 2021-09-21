package br.com.project.key.service

import br.com.project.key.controlador.KeyResponseData
import br.com.project.key.controlador.KeyTransferObject

class KeyServiceValidator( private val keyService: KeyService) : KeyService {

    override fun register(keyDto: KeyTransferObject): KeyResponseData {
        return keyService.register( keyDto )
    }

}