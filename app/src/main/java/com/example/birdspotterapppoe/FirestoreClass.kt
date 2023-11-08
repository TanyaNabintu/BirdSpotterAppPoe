package com.example.birdspotterapppoe

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.birdspotterapppoe.Constants.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID


class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth


    fun getCurrentUserId(): String {
        val currentUser = auth.currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    suspend fun savePictureInStorage(birdPicture: Uri): String {
        return try {
            val imageName = UUID.randomUUID().toString()
            val birdPictureUrl = Firebase.storage.reference.child("bird-images/${imageName}")
                .putFile(birdPicture).await().storage.downloadUrl.await().toString()
            birdPictureUrl

        } catch (e: Exception) {
            Log.d(TAG, "saveProfileImageInStorage() error: ${e.message}")
            throw e
        }
    }

     fun addBird(bird: Bird) {
        val docRef = mFireStore.collection(Constants.FirebaseCollectionBirds).document()
        bird.id =docRef.id
        docRef.set(bird, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(TAG,"Successfull bird submited")
            }
            .addOnFailureListener { e ->
                Log.e("Error in adding bird ::", e.message.toString())
            }
    }

    fun getBirdList(orderBy: String, callback: (List<Bird>) -> Unit) {
        mFireStore.collection(Constants.FirebaseCollectionBirds)
            .orderBy(orderBy, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                var birdList = mutableListOf<Bird>()
                for (document in documents) {
                    val bird = document.toObject(Bird::class.java)
                    if(bird != null){
                        bird.id = document.id
                        birdList.add(bird)
                    }
                }
                callback(birdList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }



    fun getOneBird(birdId: String) {
        mFireStore.collection(Constants.FirebaseCollectionBirds)
            .document(birdId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }


    fun updateBird(birdId: String, birdUpdates: Map<String, Any>) {
        mFireStore.collection(Constants.FirebaseCollectionBirds)
            .document(birdId)
            .set(birdUpdates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }
    }


    fun deleteBird(birdId: String) {
        mFireStore.collection(Constants.FirebaseCollectionBirds)
            .document(birdId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
    }

/*
    fun deleteProduct(fragment: ProductsFragment, productID: String) {
        mFireStore.collection(Constants.PRODUCTS)
            .document(productID)
            .delete()
            .addOnSuccessListener {
                fragment.productDeleteSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(fragment.javaClass.simpleName, e.message.toString())
                fragment.hideProgressDialog()
            }
    }


    fun addAddress(activity: AddEditAddressActivity, addressInfo: Address) {
        mFireStore.collection(Constants.ADDRESSES)
            .document()
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("Error", "Error while adding address", e)
            }
    }

    fun getAddressList(activity: AddressListActivity) {
        mFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val addressList: ArrayList<Address> = ArrayList()

                for (i in document.documents) {
                    val addressModel = i.toObject(Address::class.java)
                    if (addressModel != null) {
                        addressModel.id = i.id

                        addressList.add(addressModel)
                    }
                }
                activity.setUpAddressInUI(addressList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("Error", "Error while getting address list", e)
            }
    }

    fun updateAddressSuccess(
        activity: AddEditAddressActivity,
        addressInfo: Address,
        addressId: String
    ) {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("Error", "Error while updating address", e)

            }
    }

    fun deleteAddress(activity: AddressListActivity, addressId: String) {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {
                activity.deleteAddressSuccess()

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("Error", "Error while deleting address.", e)
            }
    }

 */
}