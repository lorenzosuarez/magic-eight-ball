package com.example.magiceightball.core.domain.repository

import com.example.magiceightball.core.domain.model.CharacterPage
import java.io.IOException

/**
 * The characters other people made and gave to everyone. There may be any number of them, so
 * they are never asked for all at once: a few, and then the ones after those.
 */
interface CommunityCharacters {
    /** Whether there is such a thing yet: where there is not, nothing of it is shown. */
    val open: Boolean

    /**
     * At most [limit] characters, starting after the ones [after] stands for, or from the first
     * when it is null. What it stands for is the source's own business: only a
     * [CharacterPage.next] it handed out is ever passed back.
     *
     * @throws IOException when they could not be fetched; asking again may work.
     */
    @Throws(IOException::class)
    suspend fun page(after: String?, limit: Int): CharacterPage
}

/** Before there are any: nothing is open, and nothing is listed. */
object NoCommunityCharacters : CommunityCharacters {
    override val open: Boolean = false
    override suspend fun page(after: String?, limit: Int): CharacterPage = CharacterPage(emptyList(), null)
}
