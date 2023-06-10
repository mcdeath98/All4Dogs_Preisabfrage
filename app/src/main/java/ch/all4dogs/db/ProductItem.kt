package ch.all4dogs.db

data class ProductItem(
    val id: Long = 0L,
    val article: String,
    val gtin: String,
    val han: String,
    val name: String,
    val price: Double
)