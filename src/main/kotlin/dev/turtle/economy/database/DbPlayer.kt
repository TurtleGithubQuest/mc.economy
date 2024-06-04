package dev.turtle.economy.database

import dev.turtle.economy.Economy.Companion.currencies
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

class DbPlayer(val db: TEcoDatabase, val nickname: String, val uuid: String?=null) {
    fun updateBalance(change: BalanceChange, currencyName: String, amount: Int, adminName: String, via: Via): Boolean {
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
        statement.setString(2, currencyName)
        statement.setString(3, uuid)
        statement.setString(4, nickname)

        return try {
            if (statement.executeUpdate() > 0) {
                logBalanceStatement.setInt(5, amount)
                logBalanceStatement.setString(4, change.toString())
                logBalanceStatement.setLong(1, System.currentTimeMillis() / 1000L)
                logBalanceStatement.setString(2, nickname)
                logBalanceStatement.setString(3, uuid)
                logBalanceStatement.setString(6, currencyName)
                logBalanceStatement.setString(7, adminName)
                logBalanceStatement.setString(8, via.toString())
                !logBalanceStatement.execute()
            //Should be safe to set player balance to intended amount if its positive.
            } else initBalance(currencyName, if (change != BalanceChange.DEC) amount else currencies[currencyName.uppercase()]?.startBalance ?: 0)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            false
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
        return if (resultSet?.next()!!) {
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
            initBalanceStatement.executeUpdate().let { initRowsUpdated ->
                if (initRowsUpdated > 0) {
                    logBalanceStatement.setLong(1, System.currentTimeMillis() / 1000L)
                    logBalanceStatement.setInt(5, balance)
                    logBalanceStatement.setString(4, "INIT")
                    logBalanceStatement.setString(2, nickname)
                    logBalanceStatement.setString(3, uuid)
                    logBalanceStatement.setString(6, currency)
                    logBalanceStatement.setString(7, "SERVER")
                    logBalanceStatement.setString(8, Via.CMD.toString())
                    return logBalanceStatement.executeUpdate() > 0
                }
            }
            false
        } catch(_: SQLIntegrityConstraintViolationException) {
            false
        }
    }
}