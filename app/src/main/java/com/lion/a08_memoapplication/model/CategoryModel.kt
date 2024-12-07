package com.lion.a08_memoapplication.model

import androidx.room.PrimaryKey

data class CategoryModel (
    // 카테고리 번호
    var categoryIdx: Int = 0,
    // 카테고리 이름
    var categoryName: String = ""
)