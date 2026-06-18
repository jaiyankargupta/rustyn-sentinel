package com.rustyn.sentinel.engine

import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.data.database.entity.RuleEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RuleEngineTest {

    private lateinit var ruleEngine: RuleEngine

    @Before
    fun setUp() {
        ruleEngine = RuleEngine()
    }

    @Test
    fun testSanitizeNumber() {
        assertEquals("+918904889067", ruleEngine.sanitizeNumber("+91 89048-89067"))
        assertEquals("1800123456", ruleEngine.sanitizeNumber("1800-(123)-456"))
    }

    @Test
    fun testAllowlistPrecedence() = runBlocking {
        // Rule: Block "+918904889067"
        val rule = RuleEntity(id = 1, pattern = "+918904889067", type = "EXACT")
        ruleEngine.updateRules(listOf(rule))

        // Allowlist: Allow "+918904889067"
        val allowEntry = AllowlistEntity(id = 1, pattern = "+918904889067", type = "EXACT")
        ruleEngine.updateAllowlist(listOf(allowEntry))

        // Allowlist must take priority
        val result = ruleEngine.evaluateCall("+918904889067")
        assertTrue(result is RuleEngine.MatchResult.Allowed)
    }

    @Test
    fun testExactMatchBlock() = runBlocking {
        val rule = RuleEntity(id = 2, pattern = "+918904889067", type = "EXACT")
        ruleEngine.updateRules(listOf(rule))

        val result = ruleEngine.evaluateCall("+918904889067")
        assertTrue(result is RuleEngine.MatchResult.Blocked)
        assertEquals(2, (result as RuleEngine.MatchResult.Blocked).ruleId)
    }

    @Test
    fun testPrefixTrieBlock() = runBlocking {
        val rule = RuleEntity(id = 3, pattern = "140*", type = "PREFIX")
        ruleEngine.updateRules(listOf(rule))

        // Blocks prefix
        assertTrue(ruleEngine.evaluateCall("1409890123") is RuleEngine.MatchResult.Blocked)
        // Allows other prefix
        assertTrue(ruleEngine.evaluateCall("1800123456") is RuleEngine.MatchResult.Allowed)
    }

    @Test
    fun testWildcardBlock() = runBlocking {
        val rule = RuleEntity(id = 4, pattern = "89048****67", type = "WILDCARD")
        ruleEngine.updateRules(listOf(rule))

        // Matching wildcard structure
        assertTrue(ruleEngine.evaluateCall("89048123467") is RuleEngine.MatchResult.Blocked)
        // Mismatched length
        assertTrue(ruleEngine.evaluateCall("8904812367") is RuleEngine.MatchResult.Allowed)
        // Mismatched suffix digits
        assertTrue(ruleEngine.evaluateCall("89048123468") is RuleEngine.MatchResult.Allowed)
    }

    @Test
    fun testPerformanceScalability() = runBlocking {
        // Generate 100,000 prefix rules
        val rules = ArrayList<RuleEntity>()
        for (i in 10000..110000) {
            rules.add(RuleEntity(id = i, pattern = "${i}*", type = "PREFIX"))
        }

        // Warm cache
        ruleEngine.updateRules(rules)

        // Evaluate multiple test calls and measure lookup speed
        val testNumbers = listOf("100009890", "500001234", "999991234", "200000000")
        for (number in testNumbers) {
            val startTime = System.nanoTime()
            val result = ruleEngine.evaluateCall(number)
            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
            
            assertNotNull(result)
            // Assert lookup is exceptionally fast (<10ms)
            assertTrue("Lookup execution took too long: $elapsedMs ms", elapsedMs < 10)
        }
    }
}
