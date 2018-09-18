package com.ding.basic.db.migration

class StatusConverter : DBFieldConverter<Int, String> {
    override fun convert(old: Int): String {
        return when (old){
            1 ->{
                "SERIALIZE"
            }
            2 ->{
                "FINISH"
            }
            else ->{
                "SERIALIZE"
            }
        }
    }
}