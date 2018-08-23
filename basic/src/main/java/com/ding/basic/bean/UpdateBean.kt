package com.ding.basic.bean

import java.io.Serializable

class UpdateBean : Serializable {
    var fix_books: List<BookFix>? = null
    var fix_contents: List<FixContent>? = null
    var books: List<UpdateBook>? = null
}