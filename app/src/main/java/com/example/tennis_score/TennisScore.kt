package com.example.tennis_score

enum class Player { A, B }

/**
 * Immutable snapshot of a tennis match's score.
 * Call TennisScore.pointWon(state, winner) after each point to get the next state.
 */
data class MatchState(
    val completedSets: List<Pair<Int, Int>> = emptyList(), // games won per set, in order
    val currentSetGames: Pair<Int, Int> = 0 to 0,
    val currentGamePoints: Pair<Int, Int> = 0 to 0,
    val inTiebreak: Boolean = false,
    val tiebreakPoints: Pair<Int, Int> = 0 to 0,
    val server: Player = Player.A,
    val setsToWin: Int = 2,          // 2 = best of 3, 3 = best of 5
    val winner: Player? = null
)

object TennisScore {

    fun pointWon(state: MatchState, winner: Player): MatchState {
        if (state.winner != null) return state // match already over

        return if (state.inTiebreak) {
            handleTiebreakPoint(state, winner)
        } else {
            handleGamePoint(state, winner)
        }
    }

    fun undo(history: List<MatchState>): List<MatchState> =
        if (history.size > 1) history.dropLast(1) else history

    // --- Display helpers ---

    fun pointLabel(mine: Int, theirs: Int): String {
        if (mine < 3 || theirs < 3) {
            return listOf("0", "15", "30", "40")[mine.coerceAtMost(3)]
        }
        return when {
            mine == theirs -> "40"      // deuce
            mine == theirs + 1 -> "AD"
            else -> "40"
        }
    }

    // --- Internal logic ---

    private fun handleGamePoint(state: MatchState, winner: Player): MatchState {
        val (a, b) = state.currentGamePoints
        val newPoints = if (winner == Player.A) (a + 1) to b else a to (b + 1)

        val gameWon = when (winner) {
            Player.A -> newPoints.first >= 4 && newPoints.first - newPoints.second >= 2
            Player.B -> newPoints.second >= 4 && newPoints.second - newPoints.first >= 2
        }

        if (!gameWon) return state.copy(currentGamePoints = newPoints)

        return applyGameWin(state, winner)
    }

    private fun applyGameWin(state: MatchState, winner: Player): MatchState {
        val (ga, gb) = state.currentSetGames
        val newGames = if (winner == Player.A) (ga + 1) to gb else ga to (gb + 1)
        val nextServer = if (state.server == Player.A) Player.B else Player.A

        // 6-6: go to tiebreak instead of a normal next game
        if (newGames.first == 6 && newGames.second == 6) {
            return state.copy(
                currentSetGames = newGames,
                currentGamePoints = 0 to 0,
                inTiebreak = true,
                tiebreakPoints = 0 to 0,
                server = nextServer
            )
        }

        val setWon = when (winner) {
            Player.A -> newGames.first >= 6 && newGames.first - newGames.second >= 2
            Player.B -> newGames.second >= 6 && newGames.second - newGames.first >= 2
        }

        if (!setWon) {
            return state.copy(currentSetGames = newGames, currentGamePoints = 0 to 0, server = nextServer)
        }

        return applySetWin(state, winner, newGames)
    }

    private fun handleTiebreakPoint(state: MatchState, winner: Player): MatchState {
        val (a, b) = state.tiebreakPoints
        val newPoints = if (winner == Player.A) (a + 1) to b else a to (b + 1)

        val tiebreakWon = when (winner) {
            Player.A -> newPoints.first >= 7 && newPoints.first - newPoints.second >= 2
            Player.B -> newPoints.second >= 7 && newPoints.second - newPoints.first >= 2
        }

        // Note: real tiebreak serve alternation is 1, then every 2 points.
        // Simplified here to alternate every point
        val nextServer = if (state.server == Player.A) Player.B else Player.A

        if (!tiebreakWon) {
            return state.copy(tiebreakPoints = newPoints, server = nextServer)
        }

        // Tiebreak win takes the set 7-6
        val (ga, gb) = state.currentSetGames
        val setGames = if (winner == Player.A) (ga + 1) to gb else ga to (gb + 1)
        return applySetWin(state.copy(server = nextServer), winner, setGames)
    }

    private fun applySetWin(state: MatchState, winner: Player, finalSetGames: Pair<Int, Int>): MatchState {
        val newCompletedSets = state.completedSets + finalSetGames
        val setsWonByWinner = newCompletedSets.count {
            if (winner == Player.A) it.first > it.second else it.second > it.first
        }

        val matchWinner = if (setsWonByWinner >= state.setsToWin) winner else null

        return state.copy(
            completedSets = newCompletedSets,
            currentSetGames = 0 to 0,
            currentGamePoints = 0 to 0,
            inTiebreak = false,
            tiebreakPoints = 0 to 0,
            winner = matchWinner
        )
    }
}
