package dev.turtle.economy.database

import dev.turtle.economy.Economy.Companion.turtle
import dev.turtle.turtlelib.database.mysql.MySql
import java.sql.ResultSet
import java.sql.SQLIntegrityConstraintViolationException

class TEcoDatabase(
    private val dbName: String,
    ip: String,
    port: String?,
    sslMode: String): MySql(dbName, ip, port, sslMode, turtle
) {
    private lateinit var initBalanceStatement: java.sql.PreparedStatement
    private lateinit var logBalanceStatement: java.sql.PreparedStatement
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
    fun initBalance(nickname: String, currency: String, balance: Int, uuid: String?=null): Boolean {
        return try {
            initBalanceStatement.setString(1, nickname)
            initBalanceStatement.setString(2, uuid)
            initBalanceStatement.setString(4, currency)
            initBalanceStatement.setInt(3, balance)
            initBalanceStatement.execute()
            logBalanceStatement.setInt(4, balance)
            logBalanceStatement.setString(3, "INIT")
            logBalanceStatement.setString(1, nickname)
            logBalanceStatement.setString(2, uuid)
            logBalanceStatement.setString(5, currency)
            logBalanceStatement.execute()
        } catch(_: SQLIntegrityConstraintViolationException) {
            false
        }
    }
    fun updateBalance(change: BalanceChange, nickname: String, currency: String, amount: Int, uuid: String?=null): Boolean {
        val balanceChange = when (change) {
            BalanceChange.INC -> "balance + ?"
            BalanceChange.DEC -> "balance - ?"
            BalanceChange.SET -> "?"
        }
        val sql = """
           UPDATE balances
           SET balance = $balanceChange, currency = ?
           WHERE (uuid = ? AND uuid IS NOT NULL) OR (nickname = ?)
        """.trimIndent()
        val statement = connection.prepareStatement(sql)
        statement.setInt(1, amount)
        statement.setString(2, currency)
        statement.setString(3, uuid)
        statement.setString(4, nickname)

        logBalanceStatement.setInt(4, amount)
        logBalanceStatement.setString(3, change.toString())
        logBalanceStatement.setString(1, nickname)
        logBalanceStatement.setString(2, uuid)
        logBalanceStatement.setString(5, currency)
        return if (!statement.execute()) {
            initBalance(nickname, currency, 0, uuid)
        } else {
            logBalanceStatement.execute()
            true
        }
    }
    fun getBalance(nickname: String, currency: String, uuid: String?=null): Int {
        val sql = """
            SELECT balance 
            FROM balances 
            WHERE (uuid = ? AND uuid IS NOT NULL) OR (nickname = ?) AND currency = ?
        """.trimIndent()
        val statement = connection.prepareStatement(sql)
        statement.setString(1, uuid)
        statement.setString(2, nickname)
        statement.setString(3, currency)
        val resultSet: ResultSet? = statement.executeQuery()
        return if (resultSet?.next() == true) {
            resultSet.getInt("balance")
        } else {
            0
        }
    }

}
enum class BalanceChange {
    INC,
    DEC,
    SET
}