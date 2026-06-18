package com.rustyn.sentinel.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PatternEngineTest {

    private lateinit var patternEngine: PatternEngine

    @Before
    fun setUp() {
        patternEngine = PatternEngine()
    }

    @Test
    fun testDetectSpamPrefixRun() {
        val blockedCalls = listOf(
            "8904881111",
            "8904882222",
            "8904883333",
            "1234567890" // Noise call
        )

        // Threshold = 3. Should group the three "890488" calls.
        val suggestions = patternEngine.analyzeBlockedCalls(
            blockedNumbers = blockedCalls,
            minThreshold = 3,
            minPrefixLength = 4
        )

        assertEquals(1, suggestions.size)
        val candidate = suggestions.first()
        assertEquals("890488*", candidate.pattern)
        assertEquals(3, candidate.triggerCount)
        assertTrue(candidate.sourceNumbers.containsAll(listOf("8904881111", "8904882222", "8904883333")))
    }

    @Test
    fun testThresholdRequiresDistinctNumbers() {
        val blockedCalls = listOf(
            "8904881111",
            "8904881111", // Repeated call from same number
            "8904881111"  // Repeated call from same number
        )

        // Should NOT suggest prefix because it's only 1 unique caller calling 3 times
        val suggestions = patternEngine.analyzeBlockedCalls(
            blockedNumbers = blockedCalls,
            minThreshold = 3,
            minPrefixLength = 4
        )

        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun testRedundantPrefixPruning() {
        val blockedCalls = listOf(
            "8904881111",
            "8904882222",
            "8904883333"
        )

        // The algorithm could theoretically output suggestions like "890488*", "89048*", "8904*".
        // Pruning must keep only the most specific one ("890488*") since they cover the exact same group of numbers.
        val suggestions = patternEngine.analyzeBlockedCalls(
            blockedNumbers = blockedCalls,
            minThreshold = 3,
            minPrefixLength = 4
        )

        assertEquals(1, suggestions.size)
        assertEquals("890488*", suggestions.first().pattern)
    }
}
