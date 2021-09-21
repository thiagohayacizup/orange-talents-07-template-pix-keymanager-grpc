package br.com.project.account

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface AccountRepository : JpaRepository<Account, Long>{

    @Query("select a from Account a where a.bankName = :bankName and a.agency = :agency and a.accountNumber = :accountNumber")
    fun findAccountByBankNameAgencyAndNumber( bankName: String, agency: String, accountNumber: String ) : Optional<Account>

}