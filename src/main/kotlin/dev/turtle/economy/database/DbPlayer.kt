package dev.turtle.economy.database

import dev.turtle.economy.Economy.Companion.currencies
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

class DbPlayer(val db: TEcoDatabase, val nickname: String, val uuid: String?=null) {
    fun updateBalance(change: BalanceChange, currencyName: String, amount: Long, initiator: String, via: Via, logTx: Boolean=true): Boolean {
        val balanceChange = when (change) {
            BalanceChange.INC -> "balance + ?"
            BalanceChange.DEC -> "balance - ?"
            BalanceChange.SET -> "?"
            else -> return false
        }
        val statement = db.connection.prepareStatement(
            """UPDATE balances
               SET balance = $balanceChange, currency = ?
               WHERE (uuid = ? AND uuid IS NOT NULL) OR (nickname = ?)
            """.trimIndent()
        )
        statement.setLong(1, amount)
        statement.setString(2, currencyName)
        statement.setString(3, uuid)
        statement.setString(4, nickname)

        return try {
            if (statement.executeUpdate() > 0) {
                if (logTx) logBalance(change, currencyName, amount, initiator, via)
                else true
            //Should be safe to set player balance to intended amount if its positive.
            } else initBalance(currencyName, if (change != BalanceChange.DEC) amount else currencies[currencyName.uppercase()]?.startBalance ?: 0L)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            false
        }
    }
    fun transferAmount(to: String, currencyName: String, amount: Long, initiator: String, via: Via): BalanceResult {
        try {
            db.connection.autoCommit = false
            val currency = currencies[currencyName]?:return BalanceResult.Error(BalanceError.UNKNOWN)
            if (amount <= 0) //todo: Maybe handle zero separately..
                return BalanceResult.Error(BalanceError.TRANSFER_NEGATIVE_AMOUNT)
            if (currency.hasExcessiveDecimals(currency.getAmountForHuman(amount).toDouble()))
                return BalanceResult.Error(BalanceError.TRANSFER_EXCESSIVE_DECIMALS)
            val senderBalance = getBalance(currencyName)
            if (senderBalance < amount)
                return BalanceResult.Error(BalanceError.SENDER_BALANCE_NOT_ENOUGH)

            if (!updateBalance(BalanceChange.DEC, currencyName, amount, initiator, via, logTx=false)) {
                db.connection.rollback()
                return BalanceResult.Error(BalanceError.SENDER_DEC_FAILED)
            }
            if (!DbPlayer(db, to).updateBalance(BalanceChange.INC, currencyName, amount, initiator, via, logTx=false)) {
                db.connection.rollback()
                return BalanceResult.Error(BalanceError.RECEIVER_INC_FAILED)
            }
            if (!logBalance(BalanceChange.TRANSFER, currencyName, amount, initiator, via, nickname=to)) {
                db.connection.rollback()
                return BalanceResult.Error(BalanceError.UNKNOWN)
            }
            db.connection.commit()
            return BalanceResult.OK
        } catch (ex: SQLException) {
            ex.printStackTrace()
            db.connection.rollback()
            return BalanceResult.Error(BalanceError.UNKNOWN)
        } finally {
            db.connection.autoCommit = true
        }
    }
    fun logBalance(change: BalanceChange, currencyName: String, amount: Long, initiator: String, via: Via, nickname: String=this@DbPlayer.nickname, data: String?=null, unix: Long=System.currentTimeMillis() / 1000L): Boolean {
        val st = db.logBalanceStatement
        st.setLong(1, unix)
        st.setString(2, nickname)
        st.setString(3, uuid)
        st.setString(4, change.toString())
        st.setLong(5, amount)
        st.setString(6, currencyName)
        data?.let { st.setString(7, it)
        }?: st.setNull(7, java.sql.Types.VARCHAR)
        st.setString(8, initiator)
        st.setString(9, via.toString())
        return try {
            st.executeUpdate() > 0
        } catch(ex: SQLException) {
            ex.printStackTrace()
            false
        }
    }
    fun getBalance(currency: String): Long {
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
            resultSet.getLong("balance")
        } else {
            0
        }
    }
    fun initBalance(currency: String, balance: Long): Boolean {
        return try {
            val initBalanceStatement = db.initBalanceStatement
            initBalanceStatement.setString(1, nickname)
            initBalanceStatement.setString(2, uuid)
            initBalanceStatement.setString(4, currency)
            initBalanceStatement.setLong(3, balance)
            initBalanceStatement.executeUpdate().let { initRowsUpdated ->
                if (initRowsUpdated > 0)
                    return logBalance(BalanceChange.INIT, currency, balance, "SERVER", Via.CMD)
            }
            false
        } catch(_: SQLIntegrityConstraintViolationException) {
            false
        }
    }
}
sealed class BalanceResult {
    data object OK: BalanceResult()
    data class Error(val error: BalanceError): BalanceResult()
    fun isOk(): Boolean = this == BalanceResult.OK
}
enum class BalanceError {
    SENDER_BALANCE_NOT_ENOUGH,
    TRANSFER_NEGATIVE_AMOUNT,
    TRANSFER_EXCESSIVE_DECIMALS,
    SENDER_DEC_FAILED,
    RECEIVER_INC_FAILED,
    UNKNOWN
}