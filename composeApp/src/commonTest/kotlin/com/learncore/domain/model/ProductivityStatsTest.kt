package com.learncore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductivityStatsTest {

    @Test
    fun `completionRate is 0 when totalTasks is 0`() {
        val stats = ProductivityStats()
        assertEquals(0f, stats.completionRate)
    }

    @Test
    fun `completionRate is 1 when all tasks are completed`() {
        val stats = ProductivityStats(totalTasks = 5, completedTasks = 5)
        assertEquals(1f, stats.completionRate)
    }

    @Test
    fun `completionRate is 0_5 when half completed`() {
        val stats = ProductivityStats(totalTasks = 10, completedTasks = 5)
        assertEquals(0.5f, stats.completionRate, absoluteTolerance = 0.001f)
    }

    @Test
    fun `totalFocusMinutes converts seconds to minutes correctly`() {
        val stats = ProductivityStats(totalFocusSeconds = 3600)
        assertEquals(60L, stats.totalFocusMinutes)
    }

    @Test
    fun `totalFocusMinutes is 0 for zero seconds`() {
        val stats = ProductivityStats(totalFocusSeconds = 0)
        assertEquals(0L, stats.totalFocusMinutes)
    }

    @Test
    fun `totalFocusMinutes rounds down for partial minutes`() {
        val stats = ProductivityStats(totalFocusSeconds = 90)
        assertEquals(1L, stats.totalFocusMinutes)
    }

    @Test
    fun `default productivity stats are all zero`() {
        val stats = ProductivityStats()
        assertEquals(0, stats.totalTasks)
        assertEquals(0, stats.completedTasks)
        assertEquals(0L, stats.totalFocusSeconds)
        assertEquals(0, stats.pomodoroSessions)
        assertTrue(stats.tasksByQuadrant.isEmpty())
    }

    @Test
    fun `tasksByQuadrant stores values correctly`() {
        val quadrantMap = mapOf(
            EisenhowerQuadrant.DO_FIRST to 3,
            EisenhowerQuadrant.SCHEDULE to 2
        )
        val stats = ProductivityStats(tasksByQuadrant = quadrantMap)
        assertEquals(3, stats.tasksByQuadrant[EisenhowerQuadrant.DO_FIRST])
        assertEquals(2, stats.tasksByQuadrant[EisenhowerQuadrant.SCHEDULE])
    }
}
