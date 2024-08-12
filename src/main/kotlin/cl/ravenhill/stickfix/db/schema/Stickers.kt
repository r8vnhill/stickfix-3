package cl.ravenhill.stickfix.db.schema

import cl.ravenhill.stickfix.db.schema.Stickers.id
import cl.ravenhill.stickfix.db.schema.Stickers.stickerId
import cl.ravenhill.stickfix.db.schema.Stickers.tag
import cl.ravenhill.stickfix.db.schema.Stickers.userId
import org.jetbrains.exposed.dao.id.IdTable

private const val STICKER_ID_LENGTH = 50

private const val TAG_LENGTH = 50

/**
 * Represents the `Stickers` table in the Stickfix bot application database. This table is designed to store stickers
 * associated with specific users and categorized by tags. Each entry in the table links a sticker to a specific user
 * and assigns a tag for categorization purposes. The table uses a string-based primary key derived from the `tag`
 * field.
 *
 * @property userId A reference to the `Users` table, indicating the user who owns the sticker. This establishes a
 *   foreign key relationship with the `Users` table.
 * @property stickerId A unique identifier for the sticker. This field is used to identify each sticker associated with
 *   a user.
 * @property tag A string representing a tag used to categorize the sticker. This field is used as the primary key for
 *   the `Stickers` table.
 * @property id The custom primary key definition based on the `tag` field.
 */
object Stickers : IdTable<String>() {

    val userId = reference("userId", Users)

    val stickerId = varchar("stickerId", STICKER_ID_LENGTH)

    val tag = varchar("tag", TAG_LENGTH)

    override val id = tag.entityId()
}
