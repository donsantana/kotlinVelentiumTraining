package com.velentium.android.platformv.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.velentium.android.platformv.R
import com.velentium.android.platformv.databinding.FragmentConnectionBinding
import com.velentium.android.platformv.ui.viewmodels.ConnectionViewModel
import com.velentium.android.platformv.utils.ui.navigationBarTitle

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ConnectionFragment : Fragment() {

    private var _binding: FragmentConnectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: ConnectionFragmentArgs by navArgs()
    private val viewModel: ConnectionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val deviceName = args.connectedDevice.name?.takeIf { it.isNotBlank() } ?: args.connectedDevice.address
        navigationBarTitle = getString(R.string.connected_to_device, deviceName)
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}