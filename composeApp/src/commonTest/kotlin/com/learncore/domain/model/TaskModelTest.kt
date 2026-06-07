package com.learncore.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskModelTest {

    @Test
    fun `default task has DO_FIRST quadrant`() {
        val task = Task(title = "Test")
        assertEquals(EisenhowerQuadrant.DO_FIRST, task.quadrant)
    }

    @Test
    fun `default task is not completed`() {
        val task = Task(title = "Test")
        assertFalse(task.isCompleted)
    }

    @Test
    fun `default task has no deadline`() {
        val task = Task(title = "Test")
        assertNull(task.deadline)
    }

    @Test
    fun `default task has no reminderMinutes`() {
        val task = Task(title = "Test")
        assertNull(task.reminderMinutes)
    }

    @Test
    fun `task copy with isCompleted true works`() {
        val task = Task(title = "Test")
        val completed = task.copy(isCompleted = true)
        assertTrue(completed.isCompleted)
    }

    @Test
    fun `task with deadline stores epoch millis correctly`() {
        val now = Clock.System.now()
        val task = Task(title = "With deadline", deadline = now)
        assertNotNull(task.deadline)
        assertEquals(now.toEpochMilliseconds(), task.deadline!!.toEpochMilliseconds())
    }

    @Test
    fun `task with reminderMinutes 60 stores correctly`() {
        val task = Task(title = "Reminder task", reminderMinutes = 60)
        assertEquals(60, task.reminderMinutes)
    }

    @Test
    fun `task id defaults to zero for new tasks`() {
        val task = Task(title = "New task")
        assertEquals(0L, task.id)
    }

    @Test
    fun `task category defaults to empty string`() {
        val task = Task(title = "Test")
        assertEquals("", task.category)
    }

    @Test
    fun `task description defaults to empty string`() {
        val task = Task(title = "Test")
        assertEquals("", task.description)
    }

    @Test
    fun `two tasks with same data are equal`() {
        val deadline = Instant.fromEpochMilliseconds(1000000L)
        val t1 = Task(id = 1, title = "Same", deadline = deadline, reminderMinutes = 30)
        val t2 = Task(id = 1, title = "Same", deadline = deadline, reminderMinutes = 30)
        assertEquals(t1, t2)
    }

    @Test
    fun `task with DELEGATE quadrant stores correctly`() {
        val task = Task(title = "Delegate me", quadrant = EisenhowerQuadrant.DELEGATE)
        assertEquals(EisenhowerQuadrant.DELEGATE, task.quadrant)
        // DELEGATE = Urgent & Not Important
        assertTrue(task.quadrant.isUrgent)
        assertFalse(task.quadrant.isImportant)
    }
}
