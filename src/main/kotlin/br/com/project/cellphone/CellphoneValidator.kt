package br.com.project.cellphone

class CellphoneValidator {

    companion object{

        private val regex : Regex = "^\\+[1-9][0-9]\\d{1,14}$".toRegex()

        fun match( cellphone : String ) : Boolean {
            return regex.matches( cellphone )
        }

    }

}