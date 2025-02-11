/*
Copyright 2022 The reform-org/reform contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package de.tu_darmstadt.informatik.st.reform.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*
import de.tu_darmstadt.informatik.st.reform.Globals

import java.sql.DriverManager
import scala.concurrent.Future

class SqliteDB extends IIndexedDB {
  val url = "jdbc:sqlite:../data/reform.db"

  val connection = DriverManager.getConnection(url).nn
  connection.setAutoCommit(false)
  val _ = connection.createStatement.nn.execute(
    s"CREATE TABLE IF NOT EXISTS reform_${Globals.VITE_DATABASE_VERSION} (key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL);",
  )
  connection.commit()

  val readStatement =
    connection.prepareStatement(s"SELECT value FROM reform_${Globals.VITE_DATABASE_VERSION} WHERE key = ?;").nn
  val writeStatement =
    connection
      .prepareStatement(
        s"INSERT INTO reform_${Globals.VITE_DATABASE_VERSION} (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = excluded.value;",
      )
      .nn

  def requestPersistentStorage(): Unit = {}

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    synchronized {
      readStatement.setString(1, key);
      val resultSet = readStatement.executeQuery().nn;
      val dbValue = if (resultSet.next()) {
        Some(resultSet.getString("value").nn)
      } else {
        None
      }
      connection.commit()
      val o = dbValue.map(readFromString(_))
      Future.successful(o)
    }
  }

  override def update[T](key: String, fun: Option[T] => T)(using codec: JsonValueCodec[T]): Future[T] = {
    synchronized {
      readStatement.setString(1, key);
      val resultSet = readStatement.executeQuery().nn;
      val dbValue = if (resultSet.next()) {
        Some(resultSet.getString("value").nn)
      } else {
        None
      }
      val value = fun(dbValue.map(readFromString(_)))
      writeStatement.setString(1, key)
      writeStatement.setString(2, writeToString(value))
      val _ = writeStatement.execute()
      connection.commit()
      Future.successful(value)
    }
  }
}
