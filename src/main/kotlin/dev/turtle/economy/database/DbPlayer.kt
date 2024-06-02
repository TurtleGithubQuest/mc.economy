package dev.turtle.economy.database

import java.sql.ResultSet
import java.sql.SQLIntegrityConstraintViolationException

class DbPlayer(val db: TEcoDatabase, val nickname: String, val uuid: String?=null) {
    fun updateBalance(change: BalanceChange, currency: String, amount: Int): Boolean {
        val balanceChange = when (change) {
            BalanceChange.INC -> "balance + ?"
            BalanceChange.DEC -> "balance - ?"
            BalanceChange.SET -> "?"
        }
        val statement = db.connection.prepareStatement(
            """UPDATE balances
               SET balance = $balanceChange, currency = ?
               WHERE (uuid = ? AND uuid IS NOT NULL) OR (nickname = ?)
            """.trimIndent()
        )
        val logBalanceStatement = db.logBalanceStatement
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
            initBalance(currency, 0)
        } else {
            logBalanceStatement.execute()
            true
        }
    }
    fun getBalance(currency: String): Int {
        val statement = db.connection.prepareStatement(
            """ SELECT balance 
                FROM balances 
                WHERE (uuid = ? AND uuid IS NOT NULL) OR (nickname = ?) AND currency = ?
            """.trimIndent()
        )
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
    fun initBalance(currency: String, balance: Int): Boolean {
        return try {
            val initBalanceStatement = db.initBalanceStatement
            val logBalanceStatement = db.logBalanceStatement
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
}