package com.inventy.plugins

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.withSuspendTransaction
import org.postgresql.ds.PGSimpleDataSource
import java.util.concurrent.TimeUnit.MINUTES


class DatabaseFactory(
    private val dbHost: String,
    private val dbPort: String,
    private val dbUser: String,
    private val dbPassword: String,
    private val databaseName: String,
    private val embedded: Boolean = false
) {

    companion object {
        suspend fun <T> dbQuery(block: suspend () -> T): T =
            withContext(Dispatchers.IO) {
                TransactionManager.currentOrNull()
                    ?.let { it.withSuspendTransaction { block() } }
                    ?: newSuspendedTransaction { block() }
            }

    }

    fun init() {
        Database.connect(hikari())
        val flyway = Flyway.configure()
            .dataSource(hikari())
            .load()
        flyway.migrate()
    }

    private fun hikari(): HikariDataSource {
        val isRunningInTest = System.getProperty("RUNNING_IN_TEST") != null
        if (isRunningInTest || embedded) {
            return HikariDataSource().apply {
                dataSourceClassName = JdbcDataSource::class.qualifiedName
                addDataSourceProperty("url", "jdbc:h2:mem:workshop;DB_CLOSE_DELAY=-1")
                addDataSourceProperty("user", "root")
                addDataSourceProperty("password", "")
                maximumPoolSize = 10
                minimumIdle = 1
                idleTimeout = 100000
                connectionTimeout = 100000
                maxLifetime = MINUTES.toMillis(30)
            }
        } else {
            return HikariDataSource().apply {
                dataSourceClassName = PGSimpleDataSource::class.qualifiedName
                addDataSourceProperty("serverName", dbHost)
                addDataSourceProperty("portNumber", dbPort)
                addDataSourceProperty("user", dbUser)
                addDataSourceProperty("password", dbPassword)
                addDataSourceProperty("databaseName", databaseName)
                maximumPoolSize = 10
                minimumIdle = 1
                idleTimeout = 100000
                connectionTimeout = 100000
                maxLifetime = MINUTES.toMillis(30)
            }
        }


    }
}

