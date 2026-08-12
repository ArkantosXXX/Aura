package com.example.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.NoteEntity
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val searchQuery: String = ""
)

class NotesViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<NotesUiState> = combine(
        noteRepository.allNotes,
        _searchQuery
    ) { notes, query ->
        val filtered = if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }
        NotesUiState(notes = filtered, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(id)
        }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            noteRepository.saveNote(
                NoteEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content
                )
            )
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.saveNote(note)
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(container.noteRepository) as T
        }
    }
}
