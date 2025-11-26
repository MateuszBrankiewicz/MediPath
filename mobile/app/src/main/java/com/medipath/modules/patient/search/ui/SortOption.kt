package com.medipath.modules.patient.search.ui

import com.medipath.R

enum class SortOption(val labelId: Int) {
    DEFAULT(R.string.default_label),
    RATING_DESC(R.string.rating_descending),
    RATING_ASC(R.string.rating_ascending),
    NUM_RATINGS_DESC(R.string.reviews_descending),
    NUM_RATINGS_ASC(R.string.reviews_ascending)
}