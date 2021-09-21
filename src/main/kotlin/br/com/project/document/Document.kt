package br.com.project.document

class Document private constructor( document : String ){

    companion object{

        private val WEIGHTS_CPF = arrayListOf(
            intArrayOf( 10,9,8,7,6,5,4,3,2 ),
            intArrayOf( 11,10,9,8,7,6,5,4,3,2 )
        )

        private val VERIFY_DIGITS_CPF =  intArrayOf(9, 10)

        private val REGEX : Regex = "^[0-9]{11}\$".toRegex()

        fun de( document : String ) : Document = Document( document )

    }

    private var valid : Boolean = true

    private val document : String

    init{
        this.document = document
    }

    fun match() : Document {
        valid = REGEX.matches( document )
        when(document){
            "00000000000", "11111111111", "22222222222", "33333333333", "44444444444",
            "55555555555", "66666666666", "77777777777", "88888888888", "99999999999"
            -> valid = false
        }
        return this
    }

    fun validateFirstDigit() : Document {
        if( valid ) {
            var sum = 0
            for (i in 0 until WEIGHTS_CPF[0].size) {
                sum += Character.getNumericValue(document[i]) * WEIGHTS_CPF[0][i]
            }
            var module: Int = (sum * 10) % 11
            if (module == 10) module = 0
            if (Character.getNumericValue(document[VERIFY_DIGITS_CPF[0]]) != module) valid = false
        }
        return this
    }

    fun validateSecondDigit() : Document {
        if( valid ) {
            var sum = 0
            for (i in 0 until WEIGHTS_CPF[1].size) {
                sum += Character.getNumericValue(document[i]) * WEIGHTS_CPF[1][i]
            }
            var module: Int = (sum * 10) % 11
            if (module == 10) module = 0
            if (Character.getNumericValue(document[VERIFY_DIGITS_CPF[1]]) != module) valid = false
        }
        return this
    }

    fun isValid() : Boolean = valid

}