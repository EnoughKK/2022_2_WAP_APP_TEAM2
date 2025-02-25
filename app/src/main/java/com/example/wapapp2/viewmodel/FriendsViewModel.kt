package com.example.wapapp2.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wapapp2.firebase.FireStoreNames
import com.example.wapapp2.model.CalcRoomParticipantDTO
import com.example.wapapp2.model.FriendDTO
import com.example.wapapp2.model.UserDTO
import com.example.wapapp2.repository.FriendsLocalRepositoryImpl
import com.example.wapapp2.repository.FriendsRepositoryImpl
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.collections.HashMap

class FriendsViewModel : ViewModel() {
    private val friendsRepositoryImpl: FriendsRepositoryImpl = FriendsRepositoryImpl.getINSTANCE()
    private val friendsLocalRepository = FriendsLocalRepositoryImpl.getINSTANCE()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    val friendsListLiveData = MutableLiveData<ArrayList<FriendDTO>>(arrayListOf())
    val friendCheckedLiveData = MutableLiveData<FriendCheckDTO>()
    val searchResultFriendsLiveData = MutableLiveData<MutableList<FriendDTO>>()
    val participantsInCalcRoom = mutableListOf<CalcRoomParticipantDTO>()

    val friendsMap = MutableLiveData(mutableMapOf<String, FriendDTO>())

    val myFriendsMapUpdatedLiveData = MutableLiveData<Boolean>(true)

    private var friendsListener: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        friendsListener?.remove()
    }

    fun checkedFriend(friendDTO: FriendDTO, isChecked: Boolean) {
        val list = friendsListLiveData.value!!
        var contains = false
        var idx = 0
        for ((index, value) in list.withIndex()) {
            if (value.friendUserId == friendDTO.friendUserId) {
                idx = index
                contains = true
                break
            }
        }

        if (isChecked) {
            if (!contains) {
                list.add(friendDTO)
                friendsListLiveData.value = list
                friendCheckedLiveData.value = FriendCheckDTO(isChecked, friendDTO)
            }
        } else
            if (contains) {
                list.removeAt(idx)
                friendsListLiveData.value = list
                friendCheckedLiveData.value = FriendCheckDTO(isChecked, friendDTO)
            }
    }

    fun findFriend(word: String) {
        viewModelScope.launch {
            val friends = friendsMap.value!!.values.toMutableList()
            val result = mutableListOf<FriendDTO>()

            if (word.isEmpty()) {
                result.addAll(friends)
            } else {
                for (friend in friends) {
                    //별명으로 검색
                    if (friend.alias.contains(word)) {
                        result.add(friend)
                    }
                }
            }

            withContext(Main) {
                searchResultFriendsLiveData.value = result
            }
        }
    }

    fun addToMyFriend(userDTO: UserDTO) {
        CoroutineScope(Dispatchers.Default).launch {
            val result = async {
                friendsRepositoryImpl.addToMyFriend(
                        FriendDTO(userDTO.id, userDTO.name, userDTO.email)
                )
            }
            result.await()
            friendsLocalRepository.insert(FriendDTO(userDTO.id, userDTO.name, userDTO.email))

            withContext(Main) {
                result.await().apply {
                    val friends = friendsMap.value!!
                    if (this) {
                        friends[userDTO.id] = FriendDTO(userDTO.id, userDTO.name, userDTO.email)
                        friendsMap.value = friends
                    }
                    myFriendsMapUpdatedLiveData.value = myFriendsMapUpdatedLiveData.value!!.not()
                }
            }
        }
    }

    fun removeMyFriend(friendId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            //myFriendsIdSet에서 삭제
            val friends = friendsMap.value!!
            friends.remove(friendId)
            withContext(Main) {
                myFriendsMapUpdatedLiveData.value = myFriendsMapUpdatedLiveData.value!!.not()
                friendsMap.value = friends
            }
            val result = async {
                friendsRepositoryImpl.removeMyFriend(friendId)
            }
            result.await()
        }
    }

    /** Must Call After Get myFriendsIDs **/
    fun getMyFriendsOptions_new(): FirestoreRecyclerOptions<UserDTO> {
        val query =
                fireStore.collection(FireStoreNames.users.name)
                        .whereIn(FieldPath.documentId(), if (friendsMap.value!!.isEmpty())
                            mutableListOf("1") else friendsMap.value!!.keys.toMutableList())

        //snapshot listener 달아줘야함 별명 거기다가 snapshot -> ob
        val option = FirestoreRecyclerOptions.Builder<UserDTO>()
                .setQuery(query, MetadataChanges.INCLUDE) {
                    val userDTO = it.toObject<UserDTO>()!!
                    userDTO.id = it.id
                    userDTO
                }
                .build()
        return option
    }


    fun getMyFriendsOptions(): FirestoreRecyclerOptions<FriendDTO> {
        val query =
                fireStore.collection(FireStoreNames.users.name)
                        .document(firebaseAuth.currentUser?.uid!!)
                        .collection(FireStoreNames.myFriends.name).orderBy("alias", Query.Direction.ASCENDING)

        val option = FirestoreRecyclerOptions.Builder<FriendDTO>()
                .setQuery(query, MetadataChanges.INCLUDE) {
                    val dto = it.toObject<FriendDTO>()!!
                    val friends = friendsMap.value!!
                    friends[dto.friendUserId] = dto
                    friendsMap.value = friends
                    dto
                }
                .build()
        return option
    }


    fun myFriends() {
        val query =
                fireStore.collection(FireStoreNames.users.name)
                        .document(firebaseAuth.currentUser?.uid!!)
                        .collection(FireStoreNames.myFriends.name).orderBy("alias", Query.Direction.ASCENDING)
        friendsListener = query.addSnapshotListener { value, error ->

        }
    }

    fun loadMyFriends() {
        viewModelScope.launch {
            //로컬db검사
            val localCount = friendsLocalRepository.count()

            localCount.collect { value ->
                if (value > 0) {
                    //로컬에 있으면 서버에서 가져오지 않음
                    val list = friendsLocalRepository.getAll()
                    list.collect { friends ->
                        val map = friendsMap.value!!
                        for (friend in friends) {
                            map[friend.friendUserId] = friend
                        }
                        withContext(Main) {
                            friendsMap.value = map
                        }
                    }
                } else {
                    //로컬에 없으면 서버에서 가져오고 로컬에 동기화
                    val myFriends = async {
                        friendsRepositoryImpl.loadMyFriends()
                    }
                    val map = friendsMap.value!!

                    for (friend in myFriends.await()) {
                        map[friend.friendUserId] = friend
                        friendsLocalRepository.insert(FriendDTO(friend.friendUserId, friend.alias, friend.email))
                    }

                    withContext(Main) {
                        friendsMap.value = map
                    }
                }
            }
            myFriendsMapUpdatedLiveData.value = myFriendsMapUpdatedLiveData.value!!.not()
        }
    }


    data class FriendCheckDTO(val isChecked: Boolean, val friendDTO: FriendDTO)
}