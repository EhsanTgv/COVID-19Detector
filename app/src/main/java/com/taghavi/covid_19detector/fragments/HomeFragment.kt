package com.taghavi.covid_19detector.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.taghavi.covid_19detector.R
import com.taghavi.covid_19detector.databinding.FragmentHomeBinding
import com.taghavi.covid_19detector.utilities.CAMERA_ID
import com.taghavi.covid_19detector.utilities.GALLERY_ID

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.homeOpenCameraButton.setOnClickListener {
            //check permission here
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_ID)
        }

        binding.homeOpenGalleryButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, GALLERY_ID)
        }

        return binding.root
    }

}
