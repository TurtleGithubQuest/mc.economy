package dev.turtle.economy.database

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.database.mysql.MySql
import java.sql.ResultSet

class TEcoDatabase(
    private val dbName: String,
    ip: String,
    port: String?,
    sslMode: String
): MySql(dbName, ip, port, sslMode, turtle) {
    lateinit var initBalanceStatement: java.sql.PreparedStatement
    lateinit var logBalanceStatement: java.sql.PreparedStatement
    fun migrate() {
        turtle.messageFactory.newMessage("&7Preparing database..").enablePrefix().send()
        connection.autoCommit = false
        val statement = connection.createStatement()
        try {
            for (migrationName in listOf("V1_Initial.sql")) {
                val migration = this::class.java.classLoader.getResourceAsStream("migrations/mysql/$migrationName")
                val statements = migration!!.bufferedReader().use { it.readText().split(";") }.filter { it.isNotBlank() }
                for (stmt in statements) {
                    statement.execute(stmt)
                }
            }
            connection.commit()
            initBalanceStatement = connection.prepareStatement("""
                INSERT INTO balances (nickname, uuid, balance, currency) VALUES (?, ?, ?, ?)
            """.trimIndent())
            logBalanceStatement = connection.prepareStatement("""
                INSERT INTO balance_logs (nickname, uuid, type, amount, currency) VALUES (?, ?, ?, ?, ?)
            """.trimIndent())
            connection.autoCommit = true
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            statement.close()
        }
    }
    fun getPlayer(nickname: String, uuid: String?=null): DbPlayer {
        return DbPlayer(this, nickname, uuid) //todo: fetch player
    }
    fun getBalances(currencyName: String, orderColumn: Column, orderBy: OrderBy, limit: Int): Array<PlayerBalance> {
        val balances = mutableListOf<PlayerBalance>()
        currencies[currencyName]?.let { currency ->
            val blacklist = currency.blacklist.map { it }
            val placeholders = blacklist.joinToString(",") { "?" }
            val statement = connection.prepareStatement(
                """ SELECT *
                    FROM balances 
                    WHERE currency = ? AND (nickname NOT IN ($placeholders) AND (uuid NOT IN ($placeholders) OR uuid IS NULL))
                    ORDER BY ${orderColumn.toString().lowercase()} $orderBy
                    LIMIT ?
                """.trimIndent()
            )
            statement.setString(1, currencyName)
            blacklist.forEachIndexed { index, value ->
                statement.setString(index + 2, value) // +2 because we have currency at position 1
            }
            val uuidIndexStart = blacklist.size + 2
            blacklist.forEachIndexed { index, value ->
                statement.setString(uuidIndexStart + index, value)
            }
            statement.setInt(blacklist.size * 2 + 2, limit)
            val resultSet: ResultSet? = statement.executeQuery()
            while (resultSet?.next() == true) {
                balances.add(PlayerBalance(
                    resultSet.getString("nickname"),
                    resultSet.getString("uuid"),
                    currencyName,
                    resultSet.getString("balance").toInt(),
                ))
            }
        }
        return balances.toTypedArray()
    }
}
enum class BalanceChange {
    INC,
    DEC,
    SET
}
enum class OrderBy {
    ASC,
    DESC,
}
enum class Column {
    Balance,
    Currency,
    Uuid,
    Nickname
}
class PlayerBalance(
    val nickname: String,
    val uuid: String?,
    val currency: String,
    val balance: Int,
)