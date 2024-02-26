package com.example.firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.firebase.databinding.ActivityUpdateUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class UpdateUserActivity : AppCompatActivity() {

    private lateinit var updateUserBinding: ActivityUpdateUserBinding

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myReference: DatabaseReference = database.reference.child("MyUsers")

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateUserBinding = ActivityUpdateUserBinding.inflate(layoutInflater)
        val view = updateUserBinding.root

        setContentView(view)
        supportActionBar?.title = "Update User"

        //register
        registerActivityForResult()

        getAndSetData()

        updateUserBinding.buttonUpdateUser.setOnClickListener {

            uploadPhoto()
        }

        updateUserBinding.userUpdateProfileImage.setOnClickListener {

            chooseImage()

        }

    }

    private fun getAndSetData() {

        val name = intent.getStringExtra("name")
        val age = intent.getIntExtra("age", 0).toString()
        val email = intent.getStringExtra("email")
        val imageUrl = intent.getStringExtra("imageUrl").toString()

        updateUserBinding.editTextUpdateName.setText(name)
        updateUserBinding.editTextUpdateAge.setText(age)
        updateUserBinding.editTextUpdateEmail.setText(email)
        Picasso.get().load(imageUrl).into(updateUserBinding.userUpdateProfileImage)

    }

    private fun chooseImage() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activityResultLauncher.launch(intent)

    }

    private fun registerActivityForResult() {

        activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback { result ->

                    val resultCode = result.resultCode
                    val imageData = result.data

                    if (resultCode == RESULT_OK && imageData != null) {

                        imageUri = imageData.data

                        //Picasso
                        imageUri?.let {

                            Picasso.get().load(it).into(updateUserBinding.userUpdateProfileImage)

                        }

                    }

                })

    }

    private fun updateData(imageUrl: String, imageName: String) {

        val updatedName = updateUserBinding.editTextUpdateName.text.toString()
        val updatedAge = updateUserBinding.editTextUpdateAge.text.toString().toInt()
        val updatedEmail = updateUserBinding.editTextUpdateEmail.text.toString()
        val userId = intent.getStringExtra("id").toString()

        val userMap = mutableMapOf<String, Any>()
        userMap["userId"] = userId
        userMap["userName"] = updatedName
        userMap["userAge"] = updatedAge
        userMap["userEmail"] = updatedEmail
        userMap["url"] = imageUrl
        userMap["imageName"] = imageName

        myReference.child(userId).updateChildren(userMap).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Toast.makeText(applicationContext, "The user has been updated", Toast.LENGTH_SHORT)
                    .show()

                updateUserBinding.buttonUpdateUser.isClickable = true
                updateUserBinding.progressBarUpdateUser.visibility = View.INVISIBLE

                finish()

            }

        }

    }

    private fun uploadPhoto() {

        updateUserBinding.buttonUpdateUser.isClickable = false
        updateUserBinding.progressBarUpdateUser.visibility = View.VISIBLE

        //UUID
        val imageName = intent.getStringExtra("imageName").toString()

        val imageReference = storageReference.child("images").child(imageName)

        imageUri?.let { uri ->

            imageReference.putFile(uri).addOnSuccessListener {

                Toast.makeText(applicationContext, "Image updated", Toast.LENGTH_SHORT).show()

                //downloadable url

                val myUploadedImageReference = storageReference.child("images").child(imageName)

                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->

                    val imageURL = url.toString()

                    updateData(imageURL, imageName)

                }

            }.addOnFailureListener {

                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()

            }

        }

    }

}