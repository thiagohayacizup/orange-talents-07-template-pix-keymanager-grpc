package br.com.project.account

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class Account private constructor(builder: Builder) {

    companion object {
        fun builder() : Builder {
            return Builder()
        }
    }

    @field:Id
    @field:GeneratedValue( strategy = GenerationType.IDENTITY )
    val id : Long? = null

    @field:NotNull
    private val bankName : String

    @field:NotNull
    private val agency : String

    @field:NotNull
    private val accountNumber : String

    @field:NotNull
    val ispb : String

    @field:NotNull
    @field:Enumerated( EnumType.STRING )
    private val accountType: AccountType

    init {
        bankName = builder.bankName
        agency = builder.agency
        accountNumber = builder.accountNumber
        accountType = builder.accountType
        ispb = builder.ispb
    }

    fun save( accountRepository: AccountRepository ) : Account {
        return accountRepository
            .findAccountByBankNameAgencyAndNumber( bankName, agency, accountNumber )
            .orElseGet{ accountRepository.save( this ) }
    }

    class Builder {

        lateinit var bankName : String
        lateinit var agency : String
        lateinit var accountNumber: String
        lateinit var accountType: AccountType
        lateinit var ispb : String

        fun withIspb( ispb : String ) : Builder {
            this.ispb = ispb
            return this
        }

        fun withBankName( bankName : String ) : Builder {
            this.bankName = bankName
            return this
        }

        fun withAgency( agency : String ) : Builder {
            this.agency = agency
            return this
        }

        fun withAccountNumber( accountNumber : String ) : Builder {
            this.accountNumber = accountNumber
            return this
        }

        fun withAccountType( accountType: AccountType ) : Builder {
            this.accountType = accountType
            return this
        }

        fun build() : Account {
            return Account( this )
        }

    }

}