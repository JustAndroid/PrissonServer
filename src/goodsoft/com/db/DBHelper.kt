package goodsoft.com.db

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.table.TableUtils
import goodsoft.com.models.Account


class DBHelper {

    private constructor()

    var accountDao: Dao<Account, String>? = null
    private var connectionSource: JdbcConnectionSource? = null

    private fun createTables() {
        TableUtils.createTableIfNotExists(connectionSource, Account::class.java)
    }

    private fun createDao() {
        accountDao = DaoManager.createDao<Dao<Account, String>, Account>(connectionSource, Account::class.java)
    }

    fun close() {
        connectionSource?.close()
    }

    companion object {
        fun initDB(): DBHelper {
            val dbHelper = DBHelper()
            val databaseUrl = "jdbc:h2:mem:myDB"
            dbHelper.connectionSource = JdbcConnectionSource(databaseUrl)
            dbHelper.createDao()
            dbHelper.createTables()
            return dbHelper
        }
    }

}