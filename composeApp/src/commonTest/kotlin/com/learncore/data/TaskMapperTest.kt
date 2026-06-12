package com.learncore.data

import com.learncore.data.local.TaskEntity
import com.learncore.data.local.entity.toDomain
import com.learncore.data.local.entity.toDomainList
import com.learncore.data.local.entity.toEntityValues
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.Task
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskMapperTest {

    private fun makeEntity(
        id: Long = 1L,
        title: String = "Test Task",
        category: String = "Kuliah",
        description: String = "Desc",
        quadrant: String = "DO_FIRST",
        isCompleted: Long = 0L,
        deadline: Long? = null,
        reminderMinutes: Long? = null,
        createdAt: Long = 1_700_000_000_000L,
        updatedAt: Long = 1_700_000_000_000L
    ) = TaskEntity(
        id = id, title = title, category = category, description = description,
        quadrant = quadrant, is_completed = isCompleted, deadline = deadline,
        reminder_minutes = reminderMinutes, created_at = createdAt, updated_at = updatedAt
    )

    @Test
    fun toDomain_maps_id_correctly() {
        assertEquals(1L, makeEntity(id = 1L).toDomain().id)
    }

    @Test
    fun toDomain_maps_title_correctly() {
        assertEquals("Belajar Kotlin", makeEntity(title = "Belajar Kotlin").toDomain().title)
    }

    @Test
    fun toDomain_maps_category_correctly() {
        assertEquals("Pekerjaan", makeEntity(category = "Pekerjaan").toDomain().category)
    }

    @Test
    fun toDomain_maps_description_correctly() {
        assertEquals("Detail", makeEntity(description = "Detail").toDomain().description)
    }

    @Test
    fun toDomain_maps_DO_FIRST_quadrant() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, makeEntity(quadrant = "DO_FIRST").toDomain().quadrant)
    }

    @Test
    fun toDomain_maps_SCHEDULE_quadrant() {
        assertEquals(EisenhowerQuadrant.SCHEDULE, makeEntity(quadrant = "SCHEDULE").toDomain().quadrant)
    }

    @Test
    fun toDomain_maps_DELEGATE_quadrant() {
        assertEquals(EisenhowerQuadrant.DELEGATE, makeEntity(quadrant = "DELEGATE").toDomain().quadrant)
    }

    @Test
    fun toDomain_maps_ELIMINATE_quadrant() {
        assertEquals(EisenhowerQuadrant.ELIMINATE, makeEntity(quadrant = "ELIMINATE").toDomain().quadrant)
    }

    @Test
    fun toDomain_unknown_quadrant_defaults_to_DO_FIRST() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, makeEntity(quadrant = "UNKNOWN").toDomain().quadrant)
    }

    @Test
    fun toDomain_isCompleted_true_when_1() {
        assertTrue(makeEntity(isCompleted = 1L).toDomain().isCompleted)
    }

    @Test
    fun toDomain_isCompleted_false_when_0() {
        assertFalse(makeEntity(isCompleted = 0L).toDomain().isCompleted)
    }

    @Test
    fun toDomain_deadline_null_when_entity_null() {
        assertNull(makeEntity(deadline = null).toDomain().deadline)
    }

    @Test
    fun toDomain_deadline_converted_from_millis() {
        val millis = 1_700_000_000_000L
        val task = makeEntity(deadline = millis).toDomain()
        assertNotNull(task.deadline)
        assertEquals(millis, task.deadline!!.toEpochMilliseconds())
    }

    @Test
    fun toDomain_reminderMinutes_null() {
        assertNull(makeEntity(reminderMinutes = null).toDomain().reminderMinutes)
    }

    @Test
    fun toDomain_reminderMinutes_converted() {
        assertEquals(30, makeEntity(reminderMinutes = 30L).toDomain().reminderMinutes)
    }

    @Test
    fun toDomain_createdAt_converted() {
        val millis = 1_700_000_000_000L
        assertEquals(millis, makeEntity(createdAt = millis).toDomain().createdAt.toEpochMilliseconds())
    }

    @Test
    fun toDomain_updatedAt_converted() {
        val millis = 1_700_100_000_000L
        assertEquals(millis, makeEntity(updatedAt = millis).toDomain().updatedAt.toEpochMilliseconds())
    }

    @Test
    fun toDomainList_empty_returns_empty() {
        assertTrue(emptyList<TaskEntity>().toDomainList().isEmpty())
    }

    @Test
    fun toDomainList_maps_all_items() {
        val entities = listOf(makeEntity(id = 1L), makeEntity(id = 2L), makeEntity(id = 3L))
        assertEquals(3, entities.toDomainList().size)
    }

    @Test
    fun toDomainList_preserves_order() {
        val entities = listOf(makeEntity(id = 10L, title = "First"), makeEntity(id = 20L, title = "Second"))
        val result = entities.toDomainList()
        assertEquals("First", result[0].title)
        assertEquals("Second", result[1].title)
    }

    @Test
    fun toEntityValues_maps_title() {
        assertEquals("Entity Task", Task(title = "Entity Task").toEntityValues().title)
    }

    @Test
    fun toEntityValues_maps_quadrant_name() {
        assertEquals("SCHEDULE", Task(title = "T", quadrant = EisenhowerQuadrant.SCHEDULE).toEntityValues().quadrant)
    }

    @Test
    fun toEntityValues_isCompleted_0_when_false() {
        assertEquals(0L, Task(title = "T", isCompleted = false).toEntityValues().isCompleted)
    }

    @Test
    fun toEntityValues_isCompleted_1_when_true() {
        assertEquals(1L, Task(title = "T", isCompleted = true).toEntityValues().isCompleted)
    }

    @Test
    fun toEntityValues_deadline_null() {
        assertNull(Task(title = "T", deadline = null).toEntityValues().deadline)
    }

    @Test
    fun toEntityValues_deadline_to_millis() {
        val millis = 1_700_000_000_000L
        assertEquals(millis, Task(title = "T", deadline = Instant.fromEpochMilliseconds(millis)).toEntityValues().deadline)
    }

    @Test
    fun toEntityValues_reminderMinutes_null() {
        assertNull(Task(title = "T", reminderMinutes = null).toEntityValues().reminderMinutes)
    }

    @Test
    fun toEntityValues_reminderMinutes_to_Long() {
        assertEquals(60L, Task(title = "T", reminderMinutes = 60).toEntityValues().reminderMinutes)
    }

    @Test
    fun roundtrip_entity_to_domain_preserves_data() {
        val entity = makeEntity(id = 5L, title = "Roundtrip", category = "Test", quadrant = "ELIMINATE", isCompleted = 1L)
        val task = entity.toDomain()
        assertEquals(5L, task.id)
        assertEquals("Roundtrip", task.title)
        assertEquals(EisenhowerQuadrant.ELIMINATE, task.quadrant)
        assertTrue(task.isCompleted)
    }
}
