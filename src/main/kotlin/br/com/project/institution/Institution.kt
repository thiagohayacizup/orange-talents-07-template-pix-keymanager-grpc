package br.com.project.institution

enum class Institution(val code: String, val institution : String) {

    ITAU( "60701190", "ITAÚ UNIBANCO S.A." );

    companion object{
        fun name( code : String ) : String {
            return when (code) {
                ITAU.code -> ITAU.institution
                else -> ""
            }
        }
    }

}