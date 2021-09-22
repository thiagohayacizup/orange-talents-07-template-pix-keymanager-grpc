package br.com.project.key.model

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface KeyRepository : JpaRepository<Key, UUID> {

    fun findByKeyValue( keyValue: String ) : Optional<Key>

    fun findByIdAndClientId( id : UUID, clientId : String ) : Optional<Key>

}