package com.example.tennis_score

import org.junit.Assert.assertEquals
import org.junit.Test

class DebugUnitTest {

    //love game: 4 straight points should reset points to 0 to 0
    @Test
    fun love_game() {
        var s = MatchState()
        repeat(4) {s = TennisScore.pointWon(s, Player.A) }
        assertEquals(Pair(1, 0), s.currentSetGames)
        assertEquals(Pair(0, 0), s.currentGamePoints)
    }
}