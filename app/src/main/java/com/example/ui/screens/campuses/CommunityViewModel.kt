package com.example.ui.screens.campuses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Result
import com.example.core.logging.AppLogger
import com.example.data.firebase.AuthRepository
import com.example.data.firebase.FirestoreService
import com.example.data.model.Community
import com.example.data.model.CommunityPost
import com.example.data.model.SouthAfricanCampus
import com.example.data.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CommunityTabFilter {
    MY_CAMPUS,
    ALL,
    JOINED
}

data class CommunityUiState(
    val currentStudent: UserProfile? = null,
    val selectedTab: CommunityTabFilter = CommunityTabFilter.MY_CAMPUS,
    val selectedCampusCode: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val allCommunities: List<Community> = emptyList(),
    val filteredCommunities: List<Community> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

data class ActiveCommunityUiState(
    val community: Community? = null,
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

class CommunityViewModel(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState(isLoading = true))
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _activeCommunityState = MutableStateFlow(ActiveCommunityUiState())
    val activeCommunityState: StateFlow<ActiveCommunityUiState> = _activeCommunityState.asStateFlow()

    private var communityPostsJob: Job? = null
    private var singleCommunityJob: Job? = null

    init {
        observeCurrentStudentAndCommunities()
    }

    private fun observeCurrentStudentAndCommunities() {
        val currentUid = authRepository.currentUserId
        if (currentUid != null) {
            firestoreService.observeUserProfile(currentUid).onEach { profile ->
                _uiState.update { state ->
                    val defaultCampus = profile?.campusCode ?: state.selectedCampusCode
                    state.copy(
                        currentStudent = profile,
                        selectedCampusCode = state.selectedCampusCode ?: defaultCampus
                    )
                }
                reapplyFilter()
            }.launchIn(viewModelScope)
        }

        firestoreService.observeCommunities().onEach { communitiesList ->
            _uiState.update { it.copy(allCommunities = communitiesList, isLoading = false) }
            reapplyFilter()
        }.launchIn(viewModelScope)
    }

    fun setTabFilter(tab: CommunityTabFilter) {
        _uiState.update { it.copy(selectedTab = tab) }
        reapplyFilter()
    }

    fun setSelectedCampus(campusCode: String?) {
        _uiState.update { it.copy(selectedCampusCode = campusCode) }
        reapplyFilter()
    }

    fun setSelectedCategory(category: String?) {
        _uiState.update { 
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
        reapplyFilter()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        reapplyFilter()
    }

    private fun reapplyFilter() {
        val state = _uiState.value
        val currentUserId = authRepository.currentUserId
        val studentCampus = state.currentStudent?.campusCode

        val filtered = state.allCommunities.filter { community ->
            // Tab filter
            val matchesTab = when (state.selectedTab) {
                CommunityTabFilter.MY_CAMPUS -> {
                    val targetCampus = state.selectedCampusCode ?: studentCampus
                    targetCampus.isNullOrBlank() || community.campusCode.equals(targetCampus, ignoreCase = true)
                }
                CommunityTabFilter.ALL -> {
                    if (state.selectedCampusCode != null) {
                        community.campusCode.equals(state.selectedCampusCode, ignoreCase = true)
                    } else true
                }
                CommunityTabFilter.JOINED -> {
                    currentUserId != null && community.isMember(currentUserId)
                }
            }

            // Category filter
            val matchesCategory = state.selectedCategory.isNullOrBlank() ||
                    community.category.equals(state.selectedCategory, ignoreCase = true)

            // Search query
            val matchesSearch = state.searchQuery.isBlank() ||
                    community.name.contains(state.searchQuery, ignoreCase = true) ||
                    community.description.contains(state.searchQuery, ignoreCase = true) ||
                    community.institutionName.contains(state.searchQuery, ignoreCase = true)

            matchesTab && matchesCategory && matchesSearch
        }

        _uiState.update { it.copy(filteredCommunities = filtered) }
    }

    fun joinCommunity(community: Community) {
        val currentUserId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            when (val result = firestoreService.joinCommunity(community.id, currentUserId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(userMessage = "You joined ${community.name}!") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.error.userMessage) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun leaveCommunity(community: Community) {
        val currentUserId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            when (val result = firestoreService.leaveCommunity(community.id, currentUserId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(userMessage = "Left ${community.name}") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.error.userMessage) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun createCommunity(
        name: String,
        category: String,
        description: String,
        campus: SouthAfricanCampus,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUserId = authRepository.currentUserId
        val student = _uiState.value.currentStudent
        if (currentUserId == null) {
            onResult(false, "You must be signed in to create a campus community.")
            return
        }

        if (name.isBlank() || name.length < 3) {
            onResult(false, "Please provide a valid community name (min 3 characters).")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val newCommunity = Community(
                name = name.trim(),
                category = category,
                description = description.trim(),
                campusCode = campus.code,
                institutionName = campus.institutionName,
                campusName = campus.shortName,
                creatorId = currentUserId,
                creatorName = student?.displayName ?: authRepository.currentUser?.email?.substringBefore("@") ?: "Student",
                memberCount = 1L,
                memberUids = listOf(currentUserId)
            )

            when (val result = firestoreService.createCommunity(newCommunity)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            userMessage = "Community '${name.trim()}' created successfully!"
                        )
                    }
                    onResult(true, null)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isCreating = false, errorMessage = result.error.userMessage)
                    }
                    onResult(false, result.error.userMessage)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadCommunityDetail(communityId: String) {
        singleCommunityJob?.cancel()
        communityPostsJob?.cancel()

        _activeCommunityState.update { it.copy(isLoading = true, errorMessage = null) }

        singleCommunityJob = firestoreService.observeCommunity(communityId).onEach { comm ->
            _activeCommunityState.update { it.copy(community = comm, isLoading = false) }
        }.launchIn(viewModelScope)

        communityPostsJob = firestoreService.observeCommunityPosts(communityId).onEach { posts ->
            _activeCommunityState.update { it.copy(posts = posts) }
        }.launchIn(viewModelScope)
    }

    fun createPost(
        communityId: String,
        title: String,
        content: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUserId = authRepository.currentUserId
        val student = _uiState.value.currentStudent
        if (currentUserId == null) {
            onResult(false, "You must be signed in to post in discussion.")
            return
        }

        if (content.isBlank()) {
            onResult(false, "Post content cannot be empty.")
            return
        }

        viewModelScope.launch {
            _activeCommunityState.update { it.copy(isPosting = true) }

            val post = CommunityPost(
                communityId = communityId,
                authorId = currentUserId,
                authorName = student?.displayName ?: "Student",
                authorPhotoUrl = student?.photoUrl ?: "",
                isAuthorVerified = student?.isStudentVerified ?: false,
                title = title.trim(),
                content = content.trim(),
                likesCount = 0L,
                likedBy = emptyList()
            )

            when (val result = firestoreService.createCommunityPost(communityId, post)) {
                is Result.Success -> {
                    _activeCommunityState.update {
                        it.copy(isPosting = false, userMessage = "Discussion post published!")
                    }
                    onResult(true, null)
                }
                is Result.Error -> {
                    _activeCommunityState.update {
                        it.copy(isPosting = false, errorMessage = result.error.userMessage)
                    }
                    onResult(false, result.error.userMessage)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun toggleLikePost(communityId: String, post: CommunityPost) {
        val currentUserId = authRepository.currentUserId ?: return
        val isLiked = post.isLikedBy(currentUserId)

        viewModelScope.launch {
            when (val result = firestoreService.togglePostLike(communityId, post.id, currentUserId, isLiked)) {
                is Result.Success -> {
                    AppLogger.d("Like toggled for post ${post.id}")
                }
                is Result.Error -> {
                    _activeCommunityState.update { it.copy(errorMessage = result.error.userMessage) }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
        _activeCommunityState.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
