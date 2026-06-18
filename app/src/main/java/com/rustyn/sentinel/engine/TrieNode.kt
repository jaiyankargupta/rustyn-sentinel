package com.rustyn.sentinel.engine

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isTerminal = false
    var ruleId: Int? = null
    var rulePattern: String? = null
}
