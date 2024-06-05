package dev.turtle.economy.database

import dev.turtle.economy.Economy.Companion.currencies
import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.database.mysql.MySql
import dev.turtle.turtlelib.util.MessageFactory
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
                INSERT INTO log_balance (unix, nickname, uuid, type, amount, currency, data, initiator, via) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent())
            connection.autoCommit = true
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            statement.close()
        }
    }
    fun getLogs(table: LogsTables, orderColumn: LogsColumn, orderBy: OrderBy, limit: Int): Array<MessageFactory.StylizedMessage> {
        val logs = mutableListOf<MessageFactory.StylizedMessage>()
        val statement = connection.prepareStatement(
                """ SELECT *
                    FROM log_${table.toString().lowercase()}
                    ORDER BY ${orderColumn.toString().lowercase()} $orderBy
                    LIMIT $limit
                """.trimIndent()
        )
        val resultSet: ResultSet? = statement.executeQuery()
        val metaData = resultSet?.metaData
        val columnCount = metaData?.columnCount
        while (resultSet?.next() == true) {
            val currencyName = resultSet.getString("currency")
            val currency = currencies[currencyName]
            val placeholders = currency?.placeholderMap?.toMutableMap<String, Any>()?:HashMap()
            for (i in 1..columnCount!!) {
                val columnName = metaData.getColumnName(i).uppercase()
                var columnValue = resultSet.getObject(i)?:"null"
                if (columnName == "AMOUNT")
                    columnValue = currency?.getAmountForHuman(columnValue.toString().toLong())?:columnValue
                placeholders[columnName] = columnValue
            }
            logs.add(turtle.messageFactory.newMessage("command.turtleeconomy.logs.entry.${placeholders["TYPE"]?.toString()?.lowercase() ?:"other"}")
                .placeholders(HashMap(placeholders)).fromConfig())
        }
        return logs.toTypedArray()
    }
    fun getPlayer(nickname: String, uuid: String?=null): DbPlayer {
        return DbPlayer(this, nickname, uuid) //todo: fetch player
    }
    fun getBalances(currencyName: String, orderColumn: BalancesColumn, orderBy: OrderBy, limit: Int): Array<PlayerBalance> {
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
            val currency = currencies[currencyName]
            while (resultSet?.next() == true) {
                balances.add(PlayerBalance(
                    resultSet.getString("nickname"),
                    resultSet.getString("uuid"),
                    currencyName,
                    currency
                        ?.getAmountForHuman(resultSet.getString("balance"))
                        ?:"0",
                ))
            }
        }
        return balances.toTypedArray()
    }
}
enum class BalanceChange {
    INC, DEC, SET, INIT, TRANSFER
}
enum class OrderBy {
    ASC,
    DESC,
}
enum class BalancesColumn {
    Balance,
    Currency,
    Uuid,
    Nickname
}
enum class LogsColumn {
    Unix, Nickname, Uuid, Type, Amount, Currency, Data, Initiator, Via
}
enum class LogsTables {
    Balance
}
enum class Via {
    CMD,
    ITEM
}
class PlayerBalance(
    val nickname: String,
    val uuid: String?,
    val currency: String,
    val balance: String,
)