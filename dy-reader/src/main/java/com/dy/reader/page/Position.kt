package com.dy.reader.page

/**
 * Created by xian on 18-3-21.
 */
data class Position(val book_id: String, var group: Int = 0, var index: Int = 0, var groupChildCount: Int = -1) {

    var offset: Int = 0

    var isBookMark = false

    fun next(): Position {
        if (index < groupChildCount - 1) {
            return Position(book_id, group, index + 1, groupChildCount)
        } else {
            return Position(book_id, group + 1, 0, -1)
        }
    }

    fun previous(): Position {
        if (index == 0) {
            return Position(book_id, group - 1, -1, -1)
        } else {
            return Position(book_id, group, index - 1, groupChildCount)
        }
    }

    override fun toString(): String {
        return "$group:$index:$groupChildCount"
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Position) {
            return group == other.group && index == other.index && groupChildCount == other.groupChildCount
        } else {
            println("Position not equal ${this} != (other)$other")
            return false
        }
    }

    override fun hashCode(): Int {
        return group.hashCode() + index.hashCode()
    }
}