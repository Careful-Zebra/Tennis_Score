package com.example.tennis_score

import org.junit.Assert.assertEquals
import org.junit.Test

class TennisScoreTest {

    //love game: 4 straight points should reset points to 0 to 0
    @Test
    fun love_game() {
        var s = MatchState()
        repeat(4) {s = TennisScore.pointWon(s, Player.A) }
        assertEquals(1 to 0, s.currentSetGames)
        assertEquals(0 to 0, s.currentGamePoints)
    }

    //deuce game: should be able to reset from deuce and win from deuce
    @Test
    fun deuce_game() {
        var s = MatchState()
        repeat(3) {s = TennisScore.pointWon(s, Player.A) }
        repeat(3) {s = TennisScore.pointWon(s, Player.B) } //now deuce
        s = TennisScore.pointWon(s, Player.A) //now advantage A
        assertEquals("AD", TennisScore.pointLabel(s.currentGamePoints.first, s.currentGamePoints.second))
        s = TennisScore.pointWon(s, Player.B) //now deuce again
        assertEquals("40", TennisScore.pointLabel(s.currentGamePoints.first, s.currentGamePoints.second))
        s = TennisScore.pointWon(s, Player.A)
        s = TennisScore.pointWon(s, Player.A) //A wins
        assertEquals(1 to 0, s.currentSetGames)
        assertEquals(0 to 0, s.currentGamePoints)
    }
}