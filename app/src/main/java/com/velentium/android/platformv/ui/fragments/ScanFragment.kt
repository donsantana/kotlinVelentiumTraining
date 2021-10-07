package com.velentium.android.platformv.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.velentium.android.platformv.R
import com.velentium.android.platformv.ble.core.common.BleManagerError
import com.velentium.android.platformv.databinding.FragmentScanBinding
import com.velentium.android.platformv.databinding.ListItemScanResultBinding
import com.velentium.android.platformv.ui.viewmodels.ConnectionViewModel
import com.velentium.android.platformv.ui.viewmodels.dto.BleScanResult
import com.velentium.android.platformv.utils.ui.navigationBarTitle
import com.velentium.android.platformv.utils.ui.showDismissSnackBar

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ScanFragment : Fragment() {

    //region Properties

    private var _binding: FragmentScanBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ConnectionViewModel by activityViewModels()
    private val scanAdapter = ScanAdapter()

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
            }
        })

    private val recyclerTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val childView = rv.findChildViewUnder(e.x, e.y)
            if (childView != null && gestureDetector.onTouchEvent(e) && childView.isEnabled) {
                (childView.tag as? Int)?.let { index ->
                    childView.playSoundEffect(SoundEffectConstants.CLICK)
                    val selectedItem = scanAdapter.results.elementAtOrNull(index)
                        ?: return@let
                    connectDevice(selectedItem)
                }
            }
            return false
        }
    }

    //endregion

    //region Lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.scanResultsRecyclerView) {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            addOnItemTouchListener(recyclerTouchListener)
            adapter = scanAdapter
        }
        with(binding.swipeRefreshLayout) {
            setColorSchemeResources(
                R.color.primary_color,
                R.color.accent_color,
                R.color.orange_red
            )
            setOnRefreshListener {
                binding.emptyResults.isVisible = false
                scanAdapter.results = setOf()
                scanForDevices()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        navigationBarTitle = getString(R.string.scanning_for_devices)
        scanAdapter.results = setOf()
        with(binding.swipeRefreshLayout) {
            isRefreshing = true
            postDelayed({
                scanForDevices()
            }, 1500L)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //endregion

    //region Private Methods

    /**
     * Starts scanning for Ble devices.
     */
    private fun scanForDevices() {
        navigationBarTitle = getString(R.string.scanning_for_devices)
        binding.swipeRefreshLayout.isRefreshing = true
        viewModel.startScanning()
            .observe(viewLifecycleOwner) { event ->
                Log.i(TAG, "Scanning results are: ${event.peekValue}")
                binding.swipeRefreshLayout.isRefreshing = false
                val results = event.unhandledValue?.takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        if (!viewModel.isScanning) {
                            binding.emptyResults.isVisible = true
                        }
                        return@observe
                    }
                viewModel.stopScanning()
                navigationBarTitle = getString(R.string.devices_found_count, results.count())
                scanAdapter.results = results
            }
    }

    /**
     * Connect to the device indicated by [scanResult].
     */
    private fun connectDevice(scanResult: BleScanResult) {
        viewModel.connect(scanResult.device)
            .observe(viewLifecycleOwner) { connectResult ->
                connectResult.getOrNull()?.let { connectedDevice ->
                    // TODO: move navigation to Coordinator
                    showDismissSnackBar(
                        message = "You have successfully connected to: ${connectedDevice.name}",
                        dismissResId = R.string.ok
                    ) {
                        val action =
                            ScanFragmentDirections.actionScanFragmentToConnectionFragment(
                                connectedDevice
                            )
                        findNavController().navigate(action)
                    }
                } ?: kotlin.run {
                    @Suppress("ThrowableNotThrown")
                    val exception =
                        BleManagerError.DeviceConnectionError("Failed to connect to the selected device.")
                    Log.e(TAG, exception.message, exception)
                    showDismissSnackBar(exception.message)
                }
            }
    }

    //endregion

    // region Nested Classes

    inner class ScanResultViewHolder(val binding: ListItemScanResultBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ScanAdapter : RecyclerView.Adapter<ScanResultViewHolder>() {
        private var scanResults = setOf<BleScanResult>()
        var results: Set<BleScanResult>
            get() = scanResults
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                scanResults = value
                notifyDataSetChanged()
            }

        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
            val itemBinding = ListItemScanResultBinding.inflate(layoutInflater, parent, false)
            return ScanResultViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
            val scanRecord = scanResults.elementAtOrNull(position) ?: return
            with(holder.binding) {
                listTitle.text = scanRecord.name
                listSubtitle.text = getString(
                    R.string.scan_detail,
                    scanRecord.dateFound,
                    scanRecord.rssi,
                    scanRecord.address
                )
            }
            holder.itemView.tag = position
        }

        override fun getItemCount(): Int = scanResults.size

        override fun getItemId(position: Int): Long {
            return scanResults.elementAtOrNull(position)?.id?.toLong()
                ?: super.getItemId(position)
        }
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = ScanFragment::class.java.simpleName
    }

    //endregion
}