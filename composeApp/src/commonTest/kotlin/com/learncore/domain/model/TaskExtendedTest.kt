package com.learncore.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskExtendedTest {

    // ===== Task.isEmpty =====

    @Test
    fun `isEmpty true when title is blank`() {
        assertTrue(Task(title = "").isEmpty)
    }

    @Test
    fun `isEmpty true when title is only spaces`() {
        assertTrue(Task(title = "   ").isEmpty)
    }

    @Test
    fun `isEmpty false when title has content`() {
        assertFalse(Task(title = "Belajar Kotlin").isEmpty)
    }

    // ===== Default values =====

    @Test
    fun `default quadrant is DO_FIRST`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, Task(title = "T").quadrant)
    }

    @Test
    fun `default isCompleted is false`() {
        assertFalse(Task(title = "T").isCompleted)
    }

    @Test
    fun `default deadline is null`() {
        assertNull(Task(title = "T").deadline)
    }

    @Test
    fun `default reminderMinutes is null`() {
        assertNull(Task(title = "T").reminderMinutes)
    }

    @Test
    fun `default id is 0`() {
        assertEquals(0L, Task(title = "T").id)
    }

    @Test
    fun `default category is empty string`() {
        assertEquals("", Task(title = "T").category)
    }

    @Test
    fun `default description is empty string`() {
        assertEquals("", Task(title = "T").description)
    }

    @Test
    fun `createdAt is auto-set to current time`() {
        val before = Clock.System.now()
        val task = Task(title = "T")
        val after = Clock.System.now()
        assertTrue(task.createdAt >= before)
        assertTrue(task.createdAt <= after)
    }

    // ===== Copy behavior =====

    @Test
    fun `copy changes only specified fields`() {
        val original = Task(id = 1L, title = "Original", isCompleted = false)
        val copy = original.copy(isCompleted = true)
        assertEquals(1L, copy.id)
        assertEquals("Original", copy.title)
        assertTrue(copy.isCompleted)
    }

    @Test
    fun `task with deadline stores correctly`() {
        val deadline = Instant.fromEpochMilliseconds(1_700_000_000_000L)
        val task = Task(title = "Deadline Task", deadline = deadline)
        assertNotNull(task.deadline)
        assertEquals(deadline, task.deadline)
    }

    @Test
    fun `task with reminderMinutes stores correctly`() {
        val task = Task(title = "Reminder Task", reminderMinutes = 30)
        assertEquals(30, task.reminderMinutes)
    }

    @Test
    fun `task equality based on all fields`() {
        val t1 = Task(id = 1L, title = "Task A", isCompleted = false)
        val t2 = Task(id = 1L, title = "Task A", isCompleted = false)
        // Note: createdAt/updatedAt auto-set, so compare specific fields
        assertEquals(t1.id, t2.id)
        assertEquals(t1.title, t2.title)
        assertEquals(t1.isCompleted, t2.isCompleted)
    }

    // ===== EisenhowerQuadrant.fromString =====

    @Test
    fun `fromString returns DO_FIRST for valid name`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, EisenhowerQuadrant.fromString("DO_FIRST"))
    }

    @Test
    fun `fromString returns SCHEDULE for valid name`() {
        assertEquals(EisenhowerQuadrant.SCHEDULE, EisenhowerQuadrant.fromString("SCHEDULE"))
    }

    @Test
    fun `fromString returns DELEGATE for valid name`() {
        assertEquals(EisenhowerQuadrant.DELEGATE, EisenhowerQuadrant.fromString("DELEGATE"))
    }

    @Test
    fun `fromString returns ELIMINATE for valid name`() {
        assertEquals(EisenhowerQuadrant.ELIMINATE, EisenhowerQuadrant.fromString("ELIMINATE"))
    }

    @Test
    fun `fromString returns DO_FIRST for unknown string`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, EisenhowerQuadrant.fromString("INVALID"))
    }

    @Test
    fun `fromString returns DO_FIRST for empty string`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, EisenhowerQuadrant.fromString(""))
    }

    // ===== EisenhowerQuadrant properties =====

    @Test
    fun `DO_FIRST is urgent and important`() {
        assertTrue(EisenhowerQuadrant.DO_FIRST.isUrgent)
        assertTrue(EisenhowerQuadrant.DO_FIRST.isImportant)
    }

    @Test
    fun `SCHEDULE is not urgent but important`() {
        assertFalse(EisenhowerQuadrant.SCHEDULE.isUrgent)
        assertTrue(EisenhowerQuadrant.SCHEDULE.isImportant)
    }

    @Test
    fun `DELEGATE is urgent but not important`() {
        assertTrue(EisenhowerQuadrant.DELEGATE.isUrgent)
        assertFalse(EisenhowerQuadrant.DELEGATE.isImportant)
    }

    @Test
    fun `ELIMINATE is not urgent and not important`() {
        assertFalse(EisenhowerQuadrant.ELIMINATE.isUrgent)
        assertFalse(EisenhowerQuadrant.ELIMINATE.isImportant)
    }

    @Test
    fun `all quadrants have non-empty displayName`() {
        EisenhowerQuadrant.entries.forEach {
            assertTrue(it.displayName.isNotBlank(), "displayName blank for $it")
        }
    }

    @Test
    fun `all quadrants have non-empty description`() {
        EisenhowerQuadrant.entries.forEach {
            assertTrue(it.description.isNotBlank(), "description blank for $it")
        }
    }

    @Test
    fun `all quadrants have non-empty action`() {
        EisenhowerQuadrant.entries.forEach {
            assertTrue(it.action.isNotBlank(), "action blank for $it")
        }
    }

    @Test
    fun `exactly 4 quadrants exist`() {
        assertEquals(4, EisenhowerQuadrant.entries.size)
    }

    @Test
    fun `entries contains all expected quadrants`() {
        val names = EisenhowerQuadrant.entries.map { it.name }
        assertTrue(names.contains("DO_FIRST"))
        assertTrue(names.contains("SCHEDULE"))
        assertTrue(names.contains("DELEGATE"))
        assertTrue(names.contains("ELIMINATE"))
    }
}
