package com.example.garden1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object DatabaseTask {
    suspend fun fetchData(): Triple<Double, Double, Double> {
        return withContext(Dispatchers.IO) {
            var humidity = 0.0
            var temperature = 0.0
            var airQuality = 0.0

            // val jdbcUrl = "jdbc:mariadb://IP주소"
            // val dbUser = "root"
            // val dbPassword = "1234"

            var connection: Connection? = null
            var resultSet: ResultSet? = null

            try {
                connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)
                val statement = connection.createStatement()
                resultSet = statement.executeQuery("SELECT humidity, temperature, air_quality FROM sensors ORDER BY timestamp DESC LIMIT 1")

                if (resultSet.next()) {
                    humidity = resultSet.getDouble("humi")
                    temperature = resultSet.getDouble("temp")
                    airQuality = resultSet.getDouble("soil_humi")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                resultSet?.close()
                connection?.close()
            }

            Triple(humidity, temperature, airQuality)
        }
    }
}
