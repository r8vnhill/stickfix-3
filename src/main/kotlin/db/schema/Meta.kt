/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package db.schema

import db.schema.Meta.id
import db.schema.Meta.key
import db.schema.Meta.primaryKey
import db.schema.Meta.value
import org.jetbrains.exposed.dao.id.IdTable

/**
 * Represents a database table for metadata storage using an `IdTable` with a String-based
 * identifier. This table is designed to store metadata as key-value pairs, where each key is unique
 * and corresponds to a specific value.
 *
 * @property key
 *  A column in the database representing the metadata key. Each key can have up to 50 characters.
 * @property value
 *  A column in the database representing the metadata value. Each value can have up to 50
 *  characters.
 * @property id
 *  Specifies the identifier column of the table, which in this case is the `key` column used as an
 *  entityId.
 * @property primaryKey
 *  Defines the primary key of the table, ensuring the uniqueness of the `key` column. Named
 *  "PK_MetaKey".
 */
object Meta : IdTable<String>() {
    val key = varchar("key", 50)  // Column for the metadata key
    val value = varchar("value", 50)  // Column for the metadata value
    override val id = key.entityId()
    override val primaryKey = PrimaryKey(key, name = "PK_MetaKey")  // Primary key definition
}
