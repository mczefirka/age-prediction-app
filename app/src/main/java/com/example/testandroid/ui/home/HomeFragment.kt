package com.example.testandroid.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache

import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.example.testandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    // API
    lateinit var requestQueue: RequestQueue

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // API
        val userinputText = binding.userInput

        val appnetwork = BasicNetwork(HurlStack())
        val appcache = DiskBasedCache(activity?.cacheDir, 1024 * 1024) // 1MB cap
        requestQueue = RequestQueue(appcache, appnetwork).apply {
            start()
        }

        // Search using Enter/Done button
        userinputText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Input text validation
                if (userinputText.text.toString().matches("[a-zA-Z]+".toRegex())) {
                    var input = userinputText.text.toString()
                    fetchData(input)
                } else if (userinputText.text.toString().isBlank()) {
                    Toast.makeText(context, "Please enter a name", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Only letters are allowed", Toast.LENGTH_LONG).show();
                }

                true
            } else {
                false
            }
        }
        // API

        // Share
        val shareButton = binding.shareButton

        shareButton.setOnClickListener{
            val sharingText = userinputText.text.toString() + " - " + binding.textHome.text.toString()
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, sharingText)
            startActivity(Intent.createChooser(shareIntent,"Share via"))
        }

        return root

    }

    private fun fetchData(input: String) {
        val url = "https://api.agify.io/?name=${input}"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                binding.textHome.text = response.getString("age")
            },
            { error ->
                Log.d("vol", error.toString())
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}