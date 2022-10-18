package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Transformations.map
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.TAG
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() ,OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var pointOfInterest: PointOfInterest
    private lateinit var lassLocation: Location
    val zoomLevel = 18f
    val pyramids = LatLng(29.979320463801766, 31.134389654555914)



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.select_map) as SupportMapFragment

        mapFragment.getMapAsync(this)


//        TODO: call this function after the user confirms on the selected location

        binding.saveButton.setOnClickListener {
        onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (this::pointOfInterest.isInitialized){
            _viewModel.latitude.value = pointOfInterest.latLng.latitude
            _viewModel.longitude.value = pointOfInterest.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
            findNavController().popBackStack()
        } else {
            val toast = Toast.makeText(context, resources.getString(R.string.select_location), Toast.LENGTH_LONG)
            toast.show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {map.mapType= GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {map.mapType= GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {map.mapType= GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {map.mapType= GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
        map=p0


        map.addMarker(MarkerOptions().position(pyramids).title("Marker in pyramids"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pyramids, zoomLevel))

        map.uiSettings.isZoomControlsEnabled = true
        startleUserLocation()
        setPoiClickListener(map)

        onLocationSelected()



    }

    private fun setPoiClickListener(map: GoogleMap) {

        map.setOnPoiClickListener { poi ->

            map.clear()
            pointOfInterest = poi

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            map.addCircle(
                CircleOptions()
                    .center(poi.latLng)
                    .radius(200.0)
                    .strokeColor(Color.argb(255,255,0,0))
                    .fillColor(Color.argb(64,255,0,0)).strokeWidth(4F)

            )

            poiMarker!!.showInfoWindow()


        }
    }

    private fun startleUserLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        when {
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
             map.isMyLocationEnabled = true

                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            lassLocation = task.result!!
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lassLocation.latitude,
                                        lassLocation.longitude
                                    ),
                                    zoomLevel
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(pyramids, zoomLevel)
                        )
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }else->
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_ACCESS_REQUEST_CODE
            )

        }//when


    }//startleUserLocation()

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startleUserLocation()
        }else{
            Snackbar.make(
                binding.selectLocationFragment,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }.show()
        }
    }

}
