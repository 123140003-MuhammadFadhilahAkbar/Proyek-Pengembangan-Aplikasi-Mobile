package com.learncore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductivityStatsExtendedTest {

    @Test
    fun `completionRate 0_25 when 1 of 4 completed`() {
        val stats = ProductivityStats(totalTasks = 4, completedTasks = 1)
        assertEquals(0.25f, stats.completionRate, absoluteTolerance = 0.001f)
    }

    @Test
    fun `completionRate 0_75 when 3 of 4 completed`() {
        val stats = ProductivityStats(totalTasks = 4, completedTasks = 3)
        assertEquals(0.75f, stats.completionRate, absoluteTolerance = 0.001f)
    }

    @Test
    fun `completionRate never exceeds 1`() {
        val stats = ProductivityStats(totalTasks = 5, completedTasks = 5)
        assertTrue(stats.completionRate <= 1f)
    }

    @Test
    fun `completionRate is always non-negative`() {
        val stats = ProductivityStats()
        assertTrue(stats.completionRate >= 0f)
    }

    @Test
    fun `totalFocusMinutes for 25 minutes pomodoro`() {
        val stats = ProductivityStats(totalFocusSeconds = 25 * 60)
        assertEquals(25L, stats.totalFocusMinutes)
    }

    @Test
    fun `totalFocusMinutes for 2 hours`() {
        val stats = ProductivityStats(totalFocusSeconds = 2 * 3600)
        assertEquals(120L, stats.totalFocusMinutes)
    }

    @Test
    fun `pomodoroSessions default is zero`() {
        assertEquals(0, ProductivityStats().pomodoroSessions)
    }

    @Test
    fun `pomodoroSessions stored correctly`() {
        val stats = ProductivityStats(pomodoroSessions = 12)
        assertEquals(12, stats.pomodoroSessions)
    }

    @Test
    fun `tasksByQuadrant all four quadrants can be stored`() {
        val map = mapOf(
            EisenhowerQuadrant.DO_FIRST to 5,
            EisenhowerQuadrant.SCHEDULE to 3,
            EisenhowerQuadrant.DELEGATE to 2,
            EisenhowerQuadrant.ELIMINATE to 1
        )
        val stats = ProductivityStats(tasksByQuadrant = map)
        assertEquals(4, stats.tasksByQuadrant.size)
        assertEquals(5, stats.tasksByQuadrant[EisenhowerQuadrant.DO_FIRST])
        assertEquals(1, stats.tasksByQuadrant[EisenhowerQuadrant.ELIMINATE])
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val stats = ProductivityStats(totalTasks = 10, pomodoroSessions = 5)
        val updated = stats.copy(completedTasks = 8)
        assertEquals(10, updated.totalTasks)
        assertEquals(5, updated.pomodoroSessions)
        assertEquals(8, updated.completedTasks)
    }

    @Test
    fun `completionRate with large numbers is correct`() {
        val stats = ProductivityStats(totalTasks = 1000, completedTasks = 999)
        assertEquals(0.999f, stats.completionRate, absoluteTolerance = 0.001f)
    }

    @Test
    fun `totalFocusMinutes truncates fractional minutes`() {
        val stats = ProductivityStats(totalFocusSeconds = 119) // 1 min 59 sec
        assertEquals(1L, stats.totalFocusMinutes)
    }
}
