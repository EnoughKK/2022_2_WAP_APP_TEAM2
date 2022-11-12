package com.example.wapapp2.repository.interfaces

import com.example.wapapp2.commons.interfaces.NewSnapshotListener
import com.example.wapapp2.model.FriendDTO
import com.example.wapapp2.model.UserDTO
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

interface FriendsRepository {
    suspend fun getMyFriends(): MutableList<FriendDTO>
    suspend fun findUsers(email: String): MutableSet<UserDTO>
    suspend fun addToMyFriend(friendDTO: FriendDTO): Boolean
    suspend fun deleteMyFriend(friendId: String, myUid: String): Boolean
    suspend fun setAliasToMyFriend(alias: String, friendId: String, myUid: String): Boolean
    fun addMyFriendsSnapshotListener(snapShotListener: NewSnapshotListener<List<FriendDTO>>): ListenerRegistration
}